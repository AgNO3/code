/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.03.2016 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.net.URI;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.jmx.JMXConnectionPool;
import eu.agno3.runtime.jmx.JMXException;


/**
 * @author mbechler
 *
 */
public class JMXConnectionPoolImpl extends GenericObjectPool<JMXPooledClientWrapper> implements JMXConnectionPool {

    private URI serverUri;


    /**
     * @param jf
     * @param serverUri
     * @param tc
     * @param cfg
     * 
     */
    public JMXConnectionPoolImpl ( JMXClientFactoryImpl jf, URI serverUri, TLSContext tc, GenericObjectPoolConfig cfg ) {
        super(new JMXPooledObjectFactory(jf, serverUri, tc), cfg);
        this.serverUri = serverUri;
    }


    @Override
    public JMXPooledClientWrapper getConnection () throws JMXException {
        try {
            JMXPooledClientWrapper borrowObject = this.borrowObject();
            borrowObject.setPool(this);
            return borrowObject;
        }
        catch ( Exception e ) {
            throw new JMXException("Failed to get JMX connection to " + this.serverUri, e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.pool2.impl.GenericObjectPool#close()
     */
    @Override
    public void close () {
        super.close();
    }


    /**
     * @param cw
     */
    public void release ( JMXPooledClientWrapper cw ) {
        this.returnObject(cw);
    }
}
