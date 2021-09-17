/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 14, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 *
 */
public class OneOffUtil {

    /**
     * 
     * @param obj
     * @return whether any change was made
     * @throws ModelObjectException
     * @throws ModelServiceException
     */
    public static boolean clear ( ConfigurationObject obj ) throws ModelServiceException, ModelObjectException {
        OneOffClearingReferenceVisitor handler = new OneOffClearingReferenceVisitor();
        ReferenceWalker.walk(new EmptyReferenceWalkerContext(), obj, handler);
        return handler.isChanged();
    }
}
