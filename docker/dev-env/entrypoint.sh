#!/bin/bash -e

ASSETS_BACKUP_FILE=/data/shared/assets.zip
DB_BACKUP_FILE=/data/shared/dotcms_db.sql.gz

export JAVA_HOME=/usr/share/opensearch/jdk
export PATH=$PATH:$JAVA_HOME/bin:/usr/local/pgsql/bin



setup_postgres () {
    echo "Starting Postgres Database"
    if [ ! -d "/data/postgres" ]; then
        mv /var/lib/postgresql/$PG_VERSION/main /data/postgres
    fi
    rm -rf /var/lib/postgresql/$PG_VERSION/main
    ln -sf  /data/postgres /var/lib/postgresql/$PG_VERSION/main


    /etc/init.d/postgresql start

    if su -c "psql -lqt" postgres | cut -d \| -f 1 | grep -qw dotcms; then
        # database exists
        echo "- dotCMS db exists, skipping import"
        echo "- Delete the /data/postgres folder to force a re-import"
        return 0
    fi

    # creating database
    su -c "psql -c \"CREATE database dotcms;\" 1> /dev/null" postgres
    su -c "psql -c \"CREATE USER dotcmsdbuser WITH PASSWORD 'password';\" 1> /dev/null" postgres
    su -c "psql -c \"ALTER DATABASE dotcms OWNER TO dotcmsdbuser;\" 1> /dev/null" postgres

    if [  -f "$DB_BACKUP_FILE" ]; then
      echo "- Importing dotCMS db from backup"
      # import database
      cat $DB_BACKUP_FILE | gzip -d | PGPASSWORD=password psql -h 127.0.0.1 -Udotcmsdbuser dotcms
    fi

    return 0
}


setup_opensearch () {


    if [ ! -d "/data/opensearch" ]; then
        mv /usr/share/opensearch/data /data/opensearch
        chown dotcms.dotcms /data/opensearch
    fi

    rm -rf /usr/share/opensearch/data
    ln -sf  /data/opensearch /usr/share/opensearch/data
    chown dotcms.dotcms /data/opensearch

    echo "Starting OPENSEARCH"
    # Start up Elasticsearch
    su -c "ES_JAVA_OPTS=-Xmx1G /usr/share/opensearch/bin/opensearch 1> /dev/null" dotcms &
}


pull_dotcms_backups () {

    if [ -f "$ASSETS_BACKUP_FILE" ] && [ -f $DB_BACKUP_FILE ]; then
        echo "- DB and Assets backups exist, skipping"
        echo "- Delete $ASSETS_BACKUP_FILE and $DB_BACKUP_FILE to force a re-download"
        return 0
    fi

    if [ -z "$DOTCMS_SOURCE_ENVIRONMENT" ]; then
        echo "No dotCMS env to clone"
        return 0
    fi
    if [ -z "$DOTCMS_API_TOKEN" -a -z "$DOTCMS_USERNAME_PASSWORD" ]; then
        echo "Source environment specified, but no dotCMS auth available"
        return 1
    fi

    echo "Pulling Environment from $DOTCMS_SOURCE_ENVIRONMENT"

    if [ -n  "$DOTCMS_API_TOKEN"  ]; then
        echo "Using Authorization: Bearer"
        AUTH_HEADER="Authorization: Bearer $DOTCMS_API_TOKEN"
    else
        echo "Using Authorization: Basic"
        AUTH_HEADER="Authorization: Basic $(echo -n $DOTCMS_USERNAME_PASSWORD | base64)"
    fi

    mkdir -p /data/shared/assets
    chown -R dotcms.dotcms /data/shared

    if [ ! -f "$ASSETS_BACKUP_FILE" ]; then
        echo "Deleting ASSETS tmp file: $ASSETS_BACKUP_FILE.tmp"
        su -c "rm -rf $ASSETS_BACKUP_FILE.tmp"

        echo "Downloading ASSETS"
        #su -c "curl --http1.1 --keepalive-time 2 -k -H\"$AUTH_HEADER\" $DOTCMS_SOURCE_ENVIRONMENT/api/v1/maintenance/_downloadAssets | bsdtar -xvf- -C /data/shared/" dotcms
        #su -c "wget --header=\"$AUTH_HEADER\" -t 1 -O - $DOTCMS_SOURCE_ENVIRONMENT/api/v1/maintenance/_downloadAssets | bsdtar -xvkf- -C /data/shared/" dotcms
        su -c "wget --no-check-certificate --header=\"$AUTH_HEADER\" -t 1 -O $ASSETS_BACKUP_FILE.tmp  $DOTCMS_SOURCE_ENVIRONMENT/api/v1/maintenance/_downloadAssets\?oldAssets=${ALL_ASSETS:-"false"} " dotcms
        su -c "mv $ASSETS_BACKUP_FILE.tmp $ASSETS_BACKUP_FILE"
    fi

    if [ ! -f "$DB_BACKUP_FILE" ]; then
        echo "Downloading database"
        su -c "rm -rf $DB_BACKUP_FILE.tmp"
        #su -c "wget --header=\"$AUTH_HEADER\" -t 1 -O - $DOTCMS_SOURCE_ENVIRONMENT/api/v1/maintenance/_downloadDb | gzip -d | PGPASSWORD=password psql -h 127.0.0.1 -Udotcmsdbuser dotcms" dotcms
        su -c "wget --no-check-certificate --header=\"$AUTH_HEADER\" -t 1 -O $DB_BACKUP_FILE.tmp $DOTCMS_SOURCE_ENVIRONMENT/api/v1/maintenance/_downloadDb"
        su -c "mv $DB_BACKUP_FILE.tmp $DB_BACKUP_FILE"
    fi

}

unpack_assets(){
  if [ -d "/data/shared/assets/1" ]; then
    echo "Assets Already Unpacked, skipping.  If you would like to unpack them again, please delete the /assets folder"
    return 0
  fi
  if [ ! -f "$ASSETS_BACKUP_FILE" ]; then
    return 0
  fi


  echo "UnZipping assets.zip"
  su -c "unzip -u /data/shared/assets.zip -d /data/shared"
}



start_dotcms () {


    export DOTCMS_DEBUG=${DOTCMS_DEBUG:-"false"}

    if [ $DOTCMS_DEBUG == "true" ];then
      echo "Setting java debug port to 8000.  To turn debugging off, pass an env variable DOTCMS_DEBUG=false"
      export CMS_JAVA_OPTS="$CMS_JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=*:8000"
    fi
    if [ $DOTCMS_DEBUG != "true" ];then
      echo
    fi

    export DB_BASE_URL=${DB_BASE_URL:-"jdbc:postgresql://127.0.0.1/dotcms"}
    export DOT_ES_ENDPOINTS=${DOT_ES_ENDPOINTS:-"https://127.0.0.1:9200"}
    export DOT_DOTCMS_CLUSTER_ID=${DOT_DOTCMS_CLUSTER_ID:-"dotcms_dev"}

        echo "Starting dotCMS"
        echo " - CMS_JAVA_OPTS: $CMS_JAVA_OPTS"
        echo " - DB_BASE_URL: $DB_BASE_URL"
        echo " - DOT_ES_ENDPOINTS: $DOT_ES_ENDPOINTS"
        echo " - DOT_DOTCMS_CLUSTER_ID: $DOT_DOTCMS_CLUSTER_ID"
    . /srv/entrypoint.sh
}


pull_dotcms_backups
echo ""
setup_postgres
echo ""
unpack_assets
echo ""
  setup_opensearch
echo ""
start_dotcms
