/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.08.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.beans.PropertyDescriptor;
import java.util.Collection;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractReferenceVisitor extends AbstractReferenceHandler implements ReferenceVisitor {

    @SuppressWarnings ( "unchecked" )
    @Override
    protected void handleReferenceValue ( ReferenceWalkerContext ctx, ConfigurationObject obj, PropertyDescriptor property, Object value )
            throws ModelServiceException, ModelObjectException {

        if ( value instanceof Collection ) {
            for ( ConfigurationObject val : (Collection<ConfigurationObject>) value ) {
                this.visitObject(ctx, val);
            }
        }
        else if ( value instanceof ConfigurationObject ) {
            this.visitObject(ctx, (ConfigurationObject) value);
        }
    }


    @Override
    public void begin ( ReferenceWalkerContext ctx, ConfigurationObject obj ) throws ModelServiceException, ModelObjectException {
        if ( obj != null ) {
            this.visitObject(ctx, obj);
        }
    }


    @Override
    public abstract void visitObject ( ReferenceWalkerContext ctx, ConfigurationObject val ) throws ModelServiceException, ModelObjectException;

}
