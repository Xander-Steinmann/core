package com.dotcms.cache.postgres;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.DataSource;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import com.dotcms.cluster.bean.Server;
import com.dotcms.enterprise.cluster.ClusterFactory;
import com.dotcms.enterprise.license.LicenseManager;
import com.dotcms.repackage.com.zaxxer.hikari.pool.HikariProxyConnection;
import com.dotcms.util.CloseUtils;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.CacheLocator;
import com.dotmarketing.business.ChainableCacheAdministratorImpl;
import com.dotmarketing.business.cache.transport.CacheTransport;
import com.dotmarketing.business.cache.transport.CacheTransportException;
import com.dotmarketing.exception.DotRuntimeException;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.vavr.Lazy;
import io.vavr.control.Try;

public final class PostgresCacheTransport implements CacheTransport {

    private Map<String, Map<String, Boolean>> cacheStatus = new HashMap<>();

    private final AtomicLong receivedMessages = new AtomicLong(0);
    private final AtomicLong receivedBytes = new AtomicLong(0);
    private final AtomicLong sentMessages = new AtomicLong(0);
    private final AtomicLong sentBytes = new AtomicLong(0);
    private final Lazy<String> topicName = Lazy.of(() -> "dotCMSCache_" + ClusterFactory.getClusterId());
    private final Lazy<String> serverIdPrefix = Lazy.of(() ->  APILocator.getShortyAPI().shortify(APILocator.getServerAPI().readServerId())+"::");
    private final String PG_NOTIFY_SQL = "notify " + topicName.get() + ",'{1}'";

    private final int KILL_ON_FAILURES = Config.getIntProperty("PGLISTENER_KILL_ON_FAILURES", 100);
    private final int SLEEP_BETWEEN_RUNS = Config.getIntProperty("PGLISTENER_SLEEP_BETWEEN_RUNS", 500);
    
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);

    private final static boolean PGLISTENER_DEBUG = ("true".equals(System.getenv("PGLISTENER_DEBUG")));
    
    
    private final Cache<Integer, Boolean> recentEvents = Caffeine.newBuilder()
                    .initialCapacity(10000)
                    .expireAfterWrite(Config.getIntProperty("PUBSUB_QUEUE_DEDUPE_TTL_MILLIS", 1500), TimeUnit.MILLISECONDS)
                    .maximumSize(50000).build();
    
    private final Lazy<DataSource> data = Lazy.of(() -> new PGDatasource().datasource());

    private PGListener listener;

    @Override
    public void init(Server localServer) throws CacheTransportException {
        
        if (!LicenseManager.getInstance().isEnterprise()) {
            Logger.info(getClass(), "No Enterprise License : No PostgresCacheTransport");
            return;
        }
        Logger.info(getClass(), "Starting PostgresCacheTransport");
        restartListener();
    }
    
    private synchronized void restartListener() {
        if (listener != null && isInitialized.get()) {
            return;
        }
        listener = new PGListener();
        listener.start();

    }


    class PGListener extends Thread {

        final HikariProxyConnection conn;

        
        PGListener(){
            conn = connect();
        }
        
        
        
        private HikariProxyConnection connect() {
            Logger.info(getClass(), "PGListener starting on " + topicName.get());
            try {
                HikariProxyConnection conn = (HikariProxyConnection) data.get().getConnection();
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("LISTEN " + topicName.get());
                }
                isInitialized.set(true);
                return conn;
            } catch (Exception e) {
                throw new DotRuntimeException(e);
            }

        }


        private long failures = 0;


        

        @Override
        public void run() {
            Logger.info(getClass(), "PGListener running on " + topicName.get());
            while (isInitialized.get()) {
                try {
                    if(conn==null || conn.isClosed()) {
                        isInitialized.set(false);
                        Logger.warn(PostgresCacheTransport.class, "PGListener.conn is closed, restarting");
                        break;
                    }
                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeQuery("SELECT 1");
                    }
                    PGConnection pgConn = conn.unwrap(PGConnection.class) ;
                    PGNotification[] notifications = pgConn.getNotifications();
                    processNotifications(notifications);

                    Try.run(() -> Thread.sleep(SLEEP_BETWEEN_RUNS));


                } catch (Throwable e) {
                    Logger.warnEveryAndDebug(PostgresCacheTransport.class, e.getMessage(), e, 5000);
                    Try.run(() -> Thread.sleep(SLEEP_BETWEEN_RUNS));
                    if (++failures > KILL_ON_FAILURES) {
                        CloseUtils.closeQuietly(conn);
                        Logger.fatal(PostgresCacheTransport.class, "PGListener failled " + KILL_ON_FAILURES + " times.  Dieing", e);
                        throw new DotRuntimeException(e);
                    }

                }
                
            }
            isInitialized.set(false);
            CloseUtils.closeQuietly(conn);
            
            

        }
    }

    private void processNotifications(PGNotification[] notifications) {

        if (notifications == null || notifications.length==0) {
            return;
        }
        for (int i = 0; i < notifications.length; i++) {
            String note = notifications[i].getParameter();
            if(null == note) {
                continue;
            }
            if(note.startsWith(serverIdPrefix.get())){
                debug( "throwing away: " + note.substring(serverIdPrefix.get().length()));
                continue;
            }
            debug("got:" + note.substring(serverIdPrefix.get().length()));
            receive(note.substring(serverIdPrefix.get().length()));
            
            
        }
    }
    
    
    


    @Override
    public boolean isInitialized() {
        return isInitialized.get();
    }

    @Override
    public boolean shouldReinit() {
        return !isInitialized.get();
    }

    public void receive(String msg) {

        receivedMessages.addAndGet(1);
        receivedBytes.addAndGet(msg.length());

        if (msg.equals(ChainableCacheAdministratorImpl.TEST_MESSAGE)) {

            Logger.info(this, "Received Message Ping " + new Date());
            try {
                send("ACK");
            } catch (Exception e) {
                Logger.error(PostgresCacheTransport.class, e.getMessage(), e);
            }

            // Handle when other server is responding to ping.
        } else if (msg.startsWith(ChainableCacheAdministratorImpl.VALIDATE_CACHE_RESPONSE)) {

            // Deletes the first part of the message, no longer needed.
            msg = msg.replace(ChainableCacheAdministratorImpl.VALIDATE_CACHE_RESPONSE, "");

            // Gets the part of the message that has the Data in Milli.
            String dateInMillis = msg.substring(0, msg.indexOf(ChainableCacheAdministratorImpl.VALIDATE_SEPARATOR));
            // Gets the last part of the message that has the Server ID.
            String serverID = msg.substring(msg.lastIndexOf(ChainableCacheAdministratorImpl.VALIDATE_SEPARATOR) + 1);

            synchronized (this.getClass()) {
                // Creates or updates the Map inside the Map.
                Map<String, Boolean> localMap = cacheStatus.get(dateInMillis);

                if (localMap == null) {
                    localMap = new HashMap<String, Boolean>();
                }

                localMap.put(serverID, Boolean.TRUE);

                // Add the Info with the Date in Millis and the Map with Server Info.
                cacheStatus.put(dateInMillis, localMap);
            }

            Logger.debug(this, ChainableCacheAdministratorImpl.VALIDATE_CACHE_RESPONSE + " SERVER_ID: " + serverID
                            + " DATE_MILLIS: " + dateInMillis);

            // Handle when other server is trying to ping local server.
        } else if (msg.startsWith(ChainableCacheAdministratorImpl.VALIDATE_CACHE)) {

            // Deletes the first part of the message, no longer needed.
            msg = msg.replace(ChainableCacheAdministratorImpl.VALIDATE_CACHE, "");

            // Gets the part of the message that has the Data in Milli.
            String dateInMillis = msg;
            // Sends the message back in order to alert the server we are alive.
            try {
                send(ChainableCacheAdministratorImpl.VALIDATE_CACHE_RESPONSE + dateInMillis
                                + ChainableCacheAdministratorImpl.VALIDATE_SEPARATOR + APILocator.getServerAPI().readServerId());
            } catch (CacheTransportException e) {
                Logger.error(this.getClass(), "Error sending message", e);
                throw new DotRuntimeException("Error sending message", e);
            }

            Logger.debug(this, ChainableCacheAdministratorImpl.VALIDATE_CACHE + " DATE_MILLIS: " + dateInMillis);

        } else if (msg.equals("ACK")) {
            Logger.info(this, "ACK Received " + new Date());
        } else if (msg.equals("MultiMessageResources.reload")) {

        } else if (msg.equals(ChainableCacheAdministratorImpl.DUMMY_TEXT_TO_SEND)) {
            // Don't do anything is we are only checking sending.
        } else {
            CacheLocator.getCacheAdministrator().invalidateCacheMesageFromCluster(msg);
        }
    }

    @Override
    public void send(String message) throws CacheTransportException {
        if (UtilMethods.isEmpty(message)) {
            return;
        }
        // if the same event has already been published in the last 1.5 seconds, skip it
        if (recentEvents.getIfPresent(message.hashCode()) != null) {
            debug("Skipping:" + message);
            return;
        }
        recentEvents.put(message.hashCode(), true);
        try (final Connection conn = data.get().getConnection(); final Statement statment = conn.createStatement()) {

            String sqlMessage = (serverIdPrefix.get()  + message).replace("'", "''");
            String sql = PG_NOTIFY_SQL.replace("{1}", sqlMessage);
            
            statment.execute(sql);
            sentMessages.addAndGet(1);
            sentBytes.addAndGet(message.length());
            
            PGConnection pgConn = conn.unwrap(PGConnection.class) ;
            
            
            
            org.postgresql.PGNotification[] notifications = pgConn.getNotifications();
            processNotifications(notifications);
            
            
        } catch (Exception e) {
            Logger.error(PostgresCacheTransport.class, "Unable to send message: " + e.getMessage(), e);
            throw new CacheTransportException("Unable to send message", e);
        }

        if(System.currentTimeMillis() % 100 ==0) {
            restartListener();
        }
        
    }

    @Override
    public void testCluster() throws CacheTransportException {
        try {
            send(ChainableCacheAdministratorImpl.TEST_MESSAGE);
            Logger.info(this, "Sending Ping to Cluster " + new Date());
        } catch (Exception e) {
            Logger.error(PostgresCacheTransport.class, e.getMessage(), e);
            throw new CacheTransportException("Error testing cluster", e);
        }
    }

    @Override
    public Map<String, Boolean> validateCacheInCluster(String dateInMillis, int numberServers, int maxWaitSeconds)
                    throws CacheTransportException {
        cacheStatus = new HashMap<>();

        // If we are already in Cluster.

        // Sends the message to the other servers.
        send(ChainableCacheAdministratorImpl.VALIDATE_CACHE + dateInMillis);

        // Waits for 2 seconds in order all the servers respond.
        int maxWaitTime = maxWaitSeconds * 1000;
        int passedWaitTime = 0;

        // Trying to NOT wait whole 2 seconds for returning the info.
        while (passedWaitTime <= maxWaitTime) {
            try {
                Thread.sleep(10);
                passedWaitTime += 10;

                Map<String, Boolean> ourMap = cacheStatus.get(dateInMillis);

                // No need to wait if we have all server results.
                if (ourMap != null && ourMap.size() == numberServers) {
                    passedWaitTime = maxWaitTime + 1;
                }

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                passedWaitTime = maxWaitTime + 1;
            }
        }


        // Returns the Map with all the info stored by receive() method.
        Map<String, Boolean> mapToReturn = new HashMap<String, Boolean>();

        if (cacheStatus.get(dateInMillis) != null) {
            mapToReturn = cacheStatus.get(dateInMillis);
        }

        return mapToReturn;
    }

    @Override
    public void shutdown() throws CacheTransportException {
        if (isInitialized.compareAndSet(true,false)) ;
    }


    private void debug(String message) {
        if(PGLISTENER_DEBUG) {
            Logger.info(this, message);
            
        }
        
        
    }
    
    
    
    
    

    @Override
    public CacheTransportInfo getInfo() {


        return new CacheTransportInfo() {
            @Override
            public String getClusterName() {
                return topicName.get();
            }

            @Override
            public String getAddress() {
                return "postgres";
            }

            @Override
            public int getPort() {
                return 5432;
            }


            @Override
            public boolean isOpen() {
                return listener.conn != null;
            }

            @Override
            public int getNumberOfNodes() {
                return -1;
            }


            @Override
            public long getReceivedBytes() {
                return receivedBytes.get();
            }

            @Override
            public long getReceivedMessages() {
                return receivedMessages.get();
            }

            @Override
            public long getSentBytes() {
                return sentBytes.get();
            }

            @Override
            public long getSentMessages() {
                return sentMessages.get();
            }
        };
    }
}
