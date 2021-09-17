/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.02.2015 by mbechler
 */
package eu.agno3.runtime.mail.internal;


import java.io.InputStream;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.mail.SMTPClientTransport;
import eu.agno3.runtime.mail.SMTPConfiguration;
import eu.agno3.runtime.mail.SMTPTransportFactory;
import eu.agno3.runtime.mail.SMTPTransportProvider;


/**
 * @author mbechler
 *
 */
@Component ( service = SMTPTransportProvider.class )
public class SMTPTransportProviderImpl implements SMTPTransportProvider {

    private static final Logger log = Logger.getLogger(SMTPTransportProviderImpl.class);

    private volatile Session session;
    private SMTPConfiguration mailConfig;

    private TLSContext tlsContext;

    private GenericObjectPool<SMTPTransportImpl> pool;

    private String instanceId;

    private SMTPTransportFactory transportFactory;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.instanceId = (String) ctx.getProperties().get("instanceId"); //$NON-NLS-1$
        try {
            this.session = this.transportFactory.makeSession(this.mailConfig, this.tlsContext);
        }
        catch (
            CryptoException |
            MessagingException e ) {
            log.error("Failed to initialize mail session", e); //$NON-NLS-1$
            return;
        }

        openPool();
    }


    /**
     * 
     */
    private void openPool () {
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
            cfg.setJmxEnabled(true);
            if ( this.instanceId != null ) {
                cfg.setJmxNamePrefix("smtp-" + this.instanceId); //$NON-NLS-1$
            }
            else {
                cfg.setJmxNamePrefix("smtp"); //$NON-NLS-1$
            }
            GenericObjectPool<SMTPTransportImpl> transportPool = new GenericObjectPool<>(new TransportObjectFactory(), cfg);
            transportPool.setTestOnBorrow(true);
            transportPool.setTestOnReturn(true);
            this.pool = transportPool;
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {

        if ( this.pool != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Closing pool with %d active, %d idle ", this.pool.getNumActive(), this.pool.getNumIdle())); //$NON-NLS-1$
            }

            this.pool.close();
        }

        this.session = null;
    }


    protected void reconnect () {
        GenericObjectPool<SMTPTransportImpl> oldPool = this.pool;
        try {
            this.session = this.transportFactory.makeSession(this.mailConfig, this.tlsContext);
            openPool();
        }
        catch (
            CryptoException |
            MessagingException e ) {
            log.error("Failed to initialize mail session", e); //$NON-NLS-1$
            return;
        }

        if ( oldPool != null ) {
            oldPool.close();
        }
    }


    @Reference (
        target = "(|(subsystem=smtp)(role=client)(role=default))",
        updated = "updatedTLSContext",
        cardinality = ReferenceCardinality.OPTIONAL,
        policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void setTLSContext ( TLSContext ctx ) {
        this.tlsContext = ctx;
    }


    protected synchronized void updatedTLSContext ( TLSContext ctx ) {
        reconnect();
    }


    protected synchronized void unsetTLSContext ( TLSContext ctx ) {
        if ( this.tlsContext == ctx ) {
            this.tlsContext = null;
        }
    }


    @Reference ( updated = "updatedSMTPConfiguration" )
    protected synchronized void setSMTPConfiguration ( SMTPConfiguration sc ) {
        this.mailConfig = sc;
    }


    protected synchronized void updatedSMTPConfiguration ( SMTPConfiguration sc ) {
        reconnect();
    }


    protected synchronized void unsetSMTPConfiguration ( SMTPConfiguration sc ) {
        if ( this.mailConfig == sc ) {
            this.mailConfig = null;
        }
    }


    @Reference
    protected synchronized void setTransportFactory ( SMTPTransportFactory tf ) {
        this.transportFactory = tf;
    }


    protected synchronized void unsetTransportFactory ( SMTPTransportFactory tf ) {
        if ( this.transportFactory == tf ) {
            this.transportFactory = null;
        }
    }


    SMTPTransportImpl createTransport () throws MessagingException {
        if ( this.session == null ) {
            return null;
        }

        if ( this.mailConfig.isUseSendmail() ) {
            return new SendmailTransportImpl(this.session, this.mailConfig);
        }

        log.debug("Creating new SMTP transport"); //$NON-NLS-1$
        SMTPTransportImpl t = new SMTPTransportImpl(this.session, this.mailConfig, this.tlsContext, this.pool);
        t.connect();

        return t;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPTransportProvider#getTransport()
     */
    @Override
    public synchronized SMTPClientTransport getTransport () throws MessagingException {

        if ( this.pool == null ) {
            throw new MessagingException("No SMTP client available"); //$NON-NLS-1$
        }

        try {
            return this.pool.borrowObject();
        }
        catch ( Exception e ) {
            throw new MessagingException("Failed to get SMTP connection", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPTransportProvider#createMimeMessage()
     */
    @Override
    public MimeMessage createMimeMessage () throws MessagingException {
        return new MimeMessage(this.session);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPTransportProvider#createMimeMessage(java.io.InputStream)
     */
    @Override
    public MimeMessage createMimeMessage ( InputStream is ) throws MessagingException {
        return new MimeMessage(this.session, is);
    }

    private class TransportObjectFactory extends BasePooledObjectFactory<SMTPTransportImpl> {

        /**
         * 
         */
        public TransportObjectFactory () {}


        /**
         * {@inheritDoc}
         *
         * @see org.apache.commons.pool2.PooledObjectFactory#destroyObject(org.apache.commons.pool2.PooledObject)
         */
        @Override
        public void destroyObject ( PooledObject<SMTPTransportImpl> pt ) throws Exception {
            super.destroyObject(pt);
            pt.getObject().closeInternal();
        }


        /**
         * {@inheritDoc}
         *
         * @see org.apache.commons.pool2.PooledObjectFactory#validateObject(org.apache.commons.pool2.PooledObject)
         */
        @Override
        public boolean validateObject ( PooledObject<SMTPTransportImpl> pt ) {
            return super.validateObject(pt) && pt.getObject().isConnected();
        }


        /**
         * {@inheritDoc}
         *
         * @see org.apache.commons.pool2.BasePooledObjectFactory#create()
         */
        @Override
        public SMTPTransportImpl create () throws Exception {
            return createTransport();
        }


        /**
         * {@inheritDoc}
         *
         * @see org.apache.commons.pool2.BasePooledObjectFactory#wrap(java.lang.Object)
         */
        @Override
        public PooledObject<SMTPTransportImpl> wrap ( SMTPTransportImpl t ) {
            return new DefaultPooledObject<>(t);
        }

    }
}
