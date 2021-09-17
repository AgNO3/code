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
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 * 
 */
public class RevisionSettingReferenceVisitor extends AbstractReferenceVisitor {

    private long revision;


    /**
     * @param revision
     * 
     */
    public RevisionSettingReferenceVisitor ( long revision ) {
        this.revision = revision;
    }


    @Override
    public void visitObject ( ReferenceWalkerContext ctx, ConfigurationObject val ) throws ModelServiceException, ModelObjectException {
        ( (AbstractConfigurationObject<?>) val ).setRevision(this.revision);
    }


    @Override
    protected void handlePropertyValue ( ReferenceWalkerContext ctx, ConfigurationObject obj, PropertyDescriptor property, Object doGetReference )
            throws ModelServiceException, ModelObjectException {
        // ignore
    }
}
