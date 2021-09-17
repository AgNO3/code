/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.08.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.beans.PropertyDescriptor;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 * 
 */
public interface ReferenceHandler {

    /**
     * @param ctx
     * @param obj
     * @param property
     * @throws ModelServiceException
     * @throws ModelObjectException
     */
    void handleReference ( ReferenceWalkerContext ctx, ConfigurationObject obj, PropertyDescriptor property )
            throws ModelServiceException, ModelObjectException;


    /**
     * @param ctx
     * @param obj
     * @throws ModelObjectException
     * @throws ModelServiceException
     */
    void begin ( ReferenceWalkerContext ctx, ConfigurationObject obj ) throws ModelServiceException, ModelObjectException;


    /**
     * @param ctx
     * @param obj
     * @param property
     * @throws ModelServiceException
     * @throws ModelObjectException
     */
    void handleValue ( ReferenceWalkerContext ctx, ConfigurationObject obj, PropertyDescriptor property )
            throws ModelServiceException, ModelObjectException;

}
