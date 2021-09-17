/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.08.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.beans.PropertyDescriptor;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectValidationException;


/**
 * @author mbechler
 * 
 */
public class ValidatingReferenceVisitor extends AbstractReferenceVisitor {

    private ModelObjectValidationUtil objectValidator;


    /**
     * @param objectValidator
     */
    public ValidatingReferenceVisitor ( ModelObjectValidationUtil objectValidator ) {
        this.objectValidator = objectValidator;
    }


    @Override
    public void visitObject ( ReferenceWalkerContext ctx, ConfigurationObject val ) throws ModelServiceException, ModelObjectException {
        try {
            this.objectValidator.validateSingleObject(ctx, (AbstractConfigurationObject<?>) val);
        }
        catch ( ObjectValidationException e ) {
            throw new ModelObjectValidationException(val.getType(), val.getId(), e);
        }
    }


    @Override
    protected void handlePropertyValue ( ReferenceWalkerContext ctx, ConfigurationObject obj, PropertyDescriptor property, Object doGetReference )
            throws ModelServiceException, ModelObjectException {}
}
