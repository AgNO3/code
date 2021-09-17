/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.server.context.internal;


import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.validation.ValidatorFactory;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.descriptors.ImageTypeRegistry;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry;
import eu.agno3.orchestrator.server.config.ServerConfiguration;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.client.MessagingClient;
import eu.agno3.runtime.transaction.TransactionService;


/**
 * @author mbechler
 * 
 */
@Component ( service = DefaultServerServiceContext.class )
public class DefaultServerServiceContextImpl implements DefaultServerServiceContext {

    private static final Logger log = Logger.getLogger(DefaultServerServiceContextImpl.class);

    private Optional<@NonNull EntityManagerFactory> configEMF = Optional.empty();
    private Optional<@NonNull MessagingClient<ServerMessageSource>> msgClient = Optional.empty();
    private Optional<@NonNull ValidatorFactory> validatorFactory = Optional.empty();
    private Optional<@NonNull TransactionService> transactionService = Optional.empty();
    private Optional<@NonNull ImageTypeRegistry> imageTypeRegistry = Optional.empty();
    private Optional<@NonNull ServiceTypeRegistry> serviceTypeRegistry = Optional.empty();
    private Optional<@NonNull ObjectTypeRegistry> objectTypeRegistry = Optional.empty();
    private Optional<@NonNull ServerConfiguration> serverConfig = Optional.empty();
    private Optional<@NonNull EntityManagerFactory> orchEMF = Optional.empty();


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        // make sure the entity manager factory is initialized

        try {
            this.configEMF.get().createEntityManager();
            this.orchEMF.get().createEntityManager();
        }
        catch ( Exception e ) {
            log.error("Failed to create entity manager", e); //$NON-NLS-1$
            this.configEMF = Optional.empty();
            this.orchEMF = Optional.empty();
            ctx.disableComponent((String) ctx.getProperties().get(ComponentConstants.COMPONENT_NAME));
            throw e;
        }
    }


    @Reference ( service = EntityManagerFactory.class, target = "(persistenceUnit=config)" )
    protected synchronized void setEMF ( @NonNull EntityManagerFactory emf ) {
        this.configEMF = Optional.of(emf);
    }


    protected synchronized void unsetEMF ( EntityManagerFactory emf ) {
        if ( this.configEMF.equals(emf) ) {
            this.configEMF = Optional.empty();
        }
    }


    @Reference ( service = EntityManagerFactory.class, target = "(persistenceUnit=orchestrator)" )
    protected synchronized void setOrchEMF ( @NonNull EntityManagerFactory emf ) {
        this.orchEMF = Optional.of(emf);
    }


    protected synchronized void unsetOrchEMF ( EntityManagerFactory emf ) {
        if ( this.orchEMF.equals(emf) ) {
            this.orchEMF = Optional.empty();
        }
    }


    @Reference
    protected synchronized void setValidatorFactory ( @NonNull ValidatorFactory vf ) {
        this.validatorFactory = Optional.of(vf);
    }


    protected synchronized void unsetValidatorFactory ( ValidatorFactory vf ) {
        if ( this.validatorFactory.equals(vf) ) {
            this.validatorFactory = Optional.empty();
        }
    }


    @Reference
    protected synchronized void setTransactionService ( @NonNull TransactionService ts ) {
        this.transactionService = Optional.of(ts);
    }


    protected synchronized void unsetTransactionService ( TransactionService ts ) {
        if ( this.transactionService.equals(ts) ) {
            this.transactionService = Optional.empty();
        }
    }


    @Reference
    protected synchronized void setImageTypeRegistry ( @NonNull ImageTypeRegistry reg ) {
        this.imageTypeRegistry = Optional.of(reg);
    }


    protected synchronized void unsetImageTypeRegistry ( ImageTypeRegistry reg ) {
        if ( this.imageTypeRegistry.equals(reg) ) {
            this.imageTypeRegistry = Optional.empty();
        }
    }


    @Reference
    protected synchronized void setServiceTypeRegistry ( @NonNull ServiceTypeRegistry reg ) {
        this.serviceTypeRegistry = Optional.of(reg);
    }


    protected synchronized void unsetServiceTypeRegistry ( ServiceTypeRegistry reg ) {
        if ( this.serviceTypeRegistry.equals(reg) ) {
            this.serviceTypeRegistry = Optional.empty();
        }
    }


    @Reference
    protected synchronized void setObjectTypeRegistry ( @NonNull ObjectTypeRegistry reg ) {
        this.objectTypeRegistry = Optional.of(reg);
    }


    protected synchronized void unsetObjectTypeRegistry ( ObjectTypeRegistry reg ) {
        if ( this.objectTypeRegistry.equals(reg) ) {
            this.objectTypeRegistry = Optional.empty();
        }
    }


    @Reference
    protected synchronized void setMessagingClient ( @NonNull MessagingClient<ServerMessageSource> mc ) {
        this.msgClient = Optional.of(mc);
    }


    protected synchronized void unsetMessagingClient ( MessagingClient<ServerMessageSource> mc ) {
        if ( this.msgClient.equals(mc) ) {
            this.msgClient = Optional.empty();
        }
    }


    @Reference
    protected synchronized void setServerConfig ( @NonNull ServerConfiguration sc ) {
        this.serverConfig = Optional.of(sc);
    }


    protected synchronized void unsetServerConfig ( ServerConfiguration sc ) {
        if ( this.serverConfig.equals(sc) ) {
            this.serverConfig = Optional.empty();
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     *
     * @see eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext#createConfigEM()
     */
    @Override
    public @NonNull EntityManager createConfigEM () throws ModelServiceException {
        EntityManager em = getConfigEMF().createEntityManager();
        if ( em == null ) {
            throw new ModelServiceException();
        }
        return em;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext#getConfigEMF()
     */
    @Override
    public synchronized @NonNull EntityManagerFactory getConfigEMF () {
        return this.configEMF.get();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext#getOrchestratorEMF()
     */
    @Override
    public @NonNull EntityManagerFactory getOrchestratorEMF () {
        return this.orchEMF.get();
    }


    @Override
    public @NonNull EntityManager createOrchEM () throws ModelServiceException {
        EntityManager em = getOrchestratorEMF().createEntityManager();
        if ( em == null ) {
            throw new ModelServiceException();
        }
        return em;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext#getValidatorFactory()
     */
    @Override
    public synchronized @NonNull ValidatorFactory getValidatorFactory () {
        return this.validatorFactory.get();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext#getTransactionService()
     */
    @Override
    public synchronized @NonNull TransactionService getTransactionService () {
        return this.transactionService.get();
    }


    /**
     * @return the imageTypeRegistry
     */
    @Override
    public synchronized @NonNull ImageTypeRegistry getImageTypeRegistry () {
        return this.imageTypeRegistry.get();
    }


    /**
     * @return the objectTypeRegistry
     */
    @Override
    public synchronized @NonNull ObjectTypeRegistry getObjectTypeRegistry () {
        return this.objectTypeRegistry.get();
    }


    /**
     * @return the serviceTypeRegistry
     */
    @Override
    public synchronized @NonNull ServiceTypeRegistry getServiceTypeRegistry () {
        return this.serviceTypeRegistry.get();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext#getMessageClient()
     */
    @Override
    public synchronized @NonNull MessagingClient<ServerMessageSource> getMessageClient () {
        return this.msgClient.get();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext#getServerConfig()
     */
    @Override
    public synchronized @NonNull ServerConfiguration getServerConfig () {
        return this.serverConfig.get();
    }
}
