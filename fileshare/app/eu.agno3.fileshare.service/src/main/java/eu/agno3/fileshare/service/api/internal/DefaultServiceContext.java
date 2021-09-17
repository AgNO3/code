/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.api.internal;


import javax.validation.ValidatorFactory;

import eu.agno3.fileshare.service.ConfigurationProvider;
import eu.agno3.runtime.db.orm.EntityTransactionService;
import eu.agno3.runtime.eventlog.EventLogger;


/**
 * @author mbechler
 *
 */
public interface DefaultServiceContext {

    /**
     * @return the fileshare pu entity transaction service
     */
    EntityTransactionService getFileshareEntityTS ();


    /**
     * @return the validator factory
     */
    ValidatorFactory getValidatorFactory ();


    /**
     * @return the configuration provider
     */
    ConfigurationProvider getConfigurationProvider ();


    /**
     * @return the event logger
     */
    EventLogger getEventLogger ();

}