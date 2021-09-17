/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.02.2015 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.management.MBeanServer;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.jmx.CredentialChecker;
import eu.agno3.runtime.jmx.JMXServer;


/**
 * @author mbechler
 *
 */
@Component ( service = JMXServer.class, immediate = true, configurationPid = "jmx.server.ssl", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class JMXSSLServerImpl extends JMXServerImpl {

    private TLSContext tlsContext;


    @Reference
    protected synchronized void setTLSContext ( TLSContext tc ) {
        this.tlsContext = tc;
    }


    protected synchronized void unsetTLSContext ( TLSContext tc ) {
        if ( this.tlsContext == tc ) {
            this.tlsContext = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmx.internal.JMXServerImpl#setMBeanServer(javax.management.MBeanServer)
     */
    @Override
    @Reference
    protected synchronized void setMBeanServer ( MBeanServer mbs ) {
        super.setMBeanServer(mbs);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmx.internal.JMXServerImpl#unsetMBeanServer(javax.management.MBeanServer)
     */
    @Override
    protected synchronized void unsetMBeanServer ( MBeanServer mbs ) {
        super.unsetMBeanServer(mbs);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmx.internal.JMXServerImpl#activate(org.osgi.service.component.ComponentContext)
     */
    @Override
    @Activate
    protected synchronized void activate ( ComponentContext ctx ) throws IOException {
        super.activate(ctx);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmx.internal.JMXServerImpl#deactivate(org.osgi.service.component.ComponentContext)
     */
    @Override
    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        super.deactivate(ctx);
    }


    @Override
    protected Map<String, Object> makeEnv ( URI listenerUri, boolean requireAuth, CredentialChecker cc ) throws IOException {
        if ( !"ssl".equals(listenerUri.getScheme()) ) { //$NON-NLS-1$
            throw new IOException("TLS server request but other protocol specified " + listenerUri); //$NON-NLS-1$
        }
        return JMXEnvironmentFactory.createServerEnv(listenerUri, this.tlsContext, requireAuth, cc);
    }

}
