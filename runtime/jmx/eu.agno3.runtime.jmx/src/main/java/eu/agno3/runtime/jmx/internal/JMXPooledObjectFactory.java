/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.03.2016 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.net.URI;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import eu.agno3.runtime.crypto.tls.TLSContext;


/**
 * @author mbechler
 *
 */
public class JMXPooledObjectFactory implements PooledObjectFactory<JMXPooledClientWrapper> {

    private JMXClientFactoryImpl jf;
    private URI serverUri;
    private TLSContext tc;


    /**
     * @param jf
     * @param serverUri
     * @param tc
     */
    public JMXPooledObjectFactory ( JMXClientFactoryImpl jf, URI serverUri, TLSContext tc ) {
        this.jf = jf;
        this.serverUri = serverUri;
        this.tc = tc;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.pool2.PooledObjectFactory#destroyObject(org.apache.commons.pool2.PooledObject)
     */
    @Override
    public void destroyObject ( PooledObject<JMXPooledClientWrapper> po ) throws Exception {
        po.getObject().close();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.pool2.PooledObjectFactory#makeObject()
     */
    @SuppressWarnings ( "resource" )
    @Override
    public PooledObject<JMXPooledClientWrapper> makeObject () throws Exception {
        return new DefaultPooledObject<>(new JMXPooledClientWrapper(this.jf.getConnection(this.serverUri, this.tc)));
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.pool2.PooledObjectFactory#activateObject(org.apache.commons.pool2.PooledObject)
     */
    @Override
    public void activateObject ( PooledObject<JMXPooledClientWrapper> po ) throws Exception {}


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.pool2.PooledObjectFactory#passivateObject(org.apache.commons.pool2.PooledObject)
     */
    @Override
    public void passivateObject ( PooledObject<JMXPooledClientWrapper> po ) throws Exception {}


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.pool2.PooledObjectFactory#validateObject(org.apache.commons.pool2.PooledObject)
     */
    @Override
    public boolean validateObject ( PooledObject<JMXPooledClientWrapper> po ) {
        return po.getObject().isValid();
    }

}
