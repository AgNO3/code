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
public class RecursiveReferenceVisitor extends AbstractReferenceVisitor {

    private ReferenceVisitor visitor;


    /**
     * 
     * @param visitor
     */
    public RecursiveReferenceVisitor ( ReferenceVisitor visitor ) {
        this.visitor = visitor;
    }


    @Override
    public void begin ( ReferenceWalkerContext ctx, ConfigurationObject obj ) throws ModelServiceException, ModelObjectException {
        if ( obj != null ) {
            // recursion is already handled by the outer call
            this.visitor.visitObject(ctx, obj);
        }
    }


    @Override
    public void visitObject ( ReferenceWalkerContext ctx, ConfigurationObject val ) throws ModelServiceException, ModelObjectException {
        this.visitor.visitObject(ctx, val);
        ReferenceWalker.walkReferences(ctx, val, this);
    }


    @Override
    protected void handlePropertyValue ( ReferenceWalkerContext ctx, ConfigurationObject obj, PropertyDescriptor property, Object doGetReference )
            throws ModelServiceException, ModelObjectException {
        // ignore
    }
}
