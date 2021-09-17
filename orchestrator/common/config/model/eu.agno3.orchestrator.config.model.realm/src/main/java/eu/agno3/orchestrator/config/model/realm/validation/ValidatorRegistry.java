/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.validation;


import java.util.List;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 *
 */
public interface ValidatorRegistry {

    /**
     * @param type
     * @return the validators that are applicable for this object type
     */
    <T extends ConfigurationObject> List<ObjectValidator<? super T>> getValidators ( Class<T> type );

}
