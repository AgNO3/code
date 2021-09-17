/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.revocation.internal;


import java.lang.ref.SoftReference;

import org.joda.time.DateTime;


/**
 * @author mbechler
 * @param <T>
 *
 */
public class CacheEntry <T> {

    private SoftReference<T> object;
    private boolean negative;
    private DateTime expireTime;


    /**
     * @param expires
     * 
     */
    public CacheEntry ( DateTime expires ) {
        this.negative = true;
        this.expireTime = expires;
    }


    /**
     * 
     * @param object
     * @param expires
     */
    public CacheEntry ( T object, DateTime expires ) {
        this.expireTime = expires;
        this.object = new SoftReference<>(object);
    }


    /**
     * @return the negative
     */
    public boolean isNegative () {
        return this.negative;
    }


    /**
     * @return the object
     */
    public T getObject () {
        return this.object.get();
    }


    /**
     * @return whether this entry is expired
     */
    public boolean isExpired () {
        return this.expireTime.isBeforeNow() || ( this.object != null && this.object.get() == null );

    }
}
