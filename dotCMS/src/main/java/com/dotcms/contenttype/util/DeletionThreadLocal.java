package com.dotcms.contenttype.util;

import com.dotcms.contenttype.model.type.ContentType;

/**
 * This class is used to store the content type that is being deleted in a thread local variable.
 */
public class DeletionThreadLocal {

    private static final ThreadLocal<ContentType> threadLocal = new ThreadLocal<>();

    /**
     * This method is used to set the content type that is being deleted in a thread local variable.
     * @param type
     */
    public void set(final ContentType type) {
        threadLocal.set(type);
    }

    /**
     * This method is used to get the content type that is being deleted in a thread local variable.
     * @return
     */
    public ContentType get() {
        return threadLocal.get();
    }

    /**
     * This method is used to remove the content type that is being deleted in a thread local variable.
     */
    public  void remove() {
        threadLocal.remove();
    }

    /**
     * This method is used to get the instance of the DeletionThreadLocal class.
     * @return
     */
    public enum INSTANCE {
        INSTANCE;
        private final DeletionThreadLocal helper = new DeletionThreadLocal();

        public static DeletionThreadLocal get() {
            return INSTANCE.helper;
        }

    }

}
