/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.ntlm;


import java.lang.ref.WeakReference;

import eu.agno3.runtime.net.ad.ADUserInfo;


/**
 * @author mbechler
 *
 */
public class NTLMContextImpl implements NTLMContext {

    private long connectionId;
    private ADUserInfo info;
    private WeakReference<Object> connection;
    private boolean cacheable;
    private long expires;
    private int timeout;


    /**
     * @param connId
     * @param timeout
     * @param conn
     * @param cacheable
     */
    public NTLMContextImpl ( long connId, int timeout, Object conn, boolean cacheable ) {
        this.expires = System.currentTimeMillis() + timeout;
        this.timeout = timeout;
        this.connectionId = connId;
        this.cacheable = cacheable;
        this.connection = new WeakReference<>(conn);
    }


    /**
     * 
     */
    public void used () {
        this.expires = System.currentTimeMillis() + this.timeout;
    }


    /**
     * @return the expires
     */
    public long getExpires () {
        return this.expires;
    }


    /**
     * @return the connectionId
     */
    @Override
    public long getConnectionId () {
        return this.connectionId;
    }


    /**
     * @return the connection
     */
    public Object getConnection () {
        return this.connection.get();
    }


    /**
     * @return the cacheable
     */
    public boolean isCacheable () {
        return this.cacheable;
    }


    @Override
    public boolean isComplete () {
        return this.info != null;
    }


    /**
     * @param info
     */
    public void setUserInfo ( ADUserInfo info ) {
        this.info = info;
    }


    /**
     * @return the info
     */
    @Override
    public ADUserInfo getUserInfo () {
        return this.info;
    }

}
