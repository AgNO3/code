/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.internal;


import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import eu.agno3.runtime.net.ad.NetlogonConnection;


/**
 * @author mbechler
 *
 */
public class NetlogonConnectionFactory extends BasePooledObjectFactory<NetlogonConnection> {

    private AbstractADRealmImpl adRealm;


    /**
     * @param adRealm
     * 
     */
    public NetlogonConnectionFactory ( AbstractADRealmImpl adRealm ) {
        this.adRealm = adRealm;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.pool2.PooledObjectFactory#destroyObject(org.apache.commons.pool2.PooledObject)
     */
    @Override
    public void destroyObject ( PooledObject<NetlogonConnection> pt ) throws Exception {
        super.destroyObject(pt);
        pt.getObject().close();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.pool2.PooledObjectFactory#validateObject(org.apache.commons.pool2.PooledObject)
     */
    @Override
    public boolean validateObject ( PooledObject<NetlogonConnection> pt ) {
        return super.validateObject(pt) && pt.getObject().check();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.pool2.BasePooledObjectFactory#create()
     */
    @Override
    public NetlogonConnection create () throws Exception {
        return this.adRealm.createNetlogonConnection();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.pool2.BasePooledObjectFactory#wrap(java.lang.Object)
     */
    @Override
    public PooledObject<NetlogonConnection> wrap ( NetlogonConnection t ) {
        return new DefaultPooledObject<>(t);
    }

}
