/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.validation;


import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 * @param <T>
 *
 */
public interface ObjectValidator <T extends ConfigurationObject> {

    /**
     * 
     * @return the object type under validation
     */
    Class<T> getObjectType ();


    /**
     * @param ctx
     * @param obj
     */
    void validate ( ObjectValidationContext ctx, T obj );
}
