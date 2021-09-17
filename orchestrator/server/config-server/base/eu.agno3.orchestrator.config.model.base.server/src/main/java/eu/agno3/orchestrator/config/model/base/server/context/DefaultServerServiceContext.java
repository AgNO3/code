/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.server.context;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.validation.ValidatorFactory;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
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
public interface DefaultServerServiceContext {

    /**
     * 
     * @return the configuration PU entity manager factory
     */
    @NonNull
    EntityManagerFactory getConfigEMF ();


    /**
     * @return the metadata PU entity manager factory
     */
    @NonNull
    EntityManagerFactory getOrchestratorEMF ();


    /**
     * 
     * @return an entity manager for the config PU
     * @throws ModelServiceException
     */
    @NonNull
    EntityManager createConfigEM () throws ModelServiceException;


    /**
     * @return an entity manager for the meta PU
     * @throws ModelServiceException
     */
    @NonNull
    EntityManager createOrchEM () throws ModelServiceException;


    /**
     * 
     * @return the validator factory
     */
    @NonNull
    ValidatorFactory getValidatorFactory ();


    /**
     * 
     * @return the transaction service
     */
    @NonNull
    TransactionService getTransactionService ();


    /**
     * @return the image type registry
     */
    @NonNull
    ImageTypeRegistry getImageTypeRegistry ();


    /**
     * @return the object type registry
     */
    @NonNull
    ObjectTypeRegistry getObjectTypeRegistry ();


    /**
     * @return the service type registry
     */
    @NonNull
    ServiceTypeRegistry getServiceTypeRegistry ();


    /**
     * @return messaging client
     * 
     */
    @NonNull
    MessagingClient<ServerMessageSource> getMessageClient ();


    /**
     * @return the server configuration
     */
    @NonNull
    ServerConfiguration getServerConfig ();

}
