/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.orchestrator.system.cfgfiles.internal;


import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.config.model.realm.StructuralObjectReferenceImpl;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryEntry;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryException;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrarySynchronizeRequest;
import eu.agno3.orchestrator.system.cfgfiles.ResourceLibrarySynchronizationHandler;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;


/**
 * @author mbechler
 *
 */
@Component (
    service = RequestEndpoint.class,
    property = "msgType=eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrarySynchronizeRequest" )
public class ResourceLibrarySynchronizeRequestEndpoint extends AbstractResourceLibraryRequestEndpoint<ResourceLibrarySynchronizeRequest> {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.internal.AbstractResourceLibraryRequestEndpoint#setMessageSource(eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource)
     */
    @Override
    @Reference
    protected synchronized void setMessageSource ( @NonNull MessageSource source ) {
        super.setMessageSource(source);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.internal.AbstractResourceLibraryRequestEndpoint#unsetMessageSource(eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource)
     */
    @Override
    protected synchronized void unsetMessageSource ( MessageSource source ) {
        super.unsetMessageSource(source);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.internal.AbstractResourceLibraryRequestEndpoint#bindSynchronizationHandler(eu.agno3.orchestrator.system.cfgfiles.ResourceLibrarySynchronizationHandler)
     */
    @Override
    @Reference ( cardinality = ReferenceCardinality.AT_LEAST_ONE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindSynchronizationHandler ( ResourceLibrarySynchronizationHandler rls ) {
        super.bindSynchronizationHandler(rls);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.internal.AbstractResourceLibraryRequestEndpoint#unbindSynchronizationHandler(eu.agno3.orchestrator.system.cfgfiles.ResourceLibrarySynchronizationHandler)
     */
    @Override
    protected synchronized void unbindSynchronizationHandler ( ResourceLibrarySynchronizationHandler rls ) {
        super.unbindSynchronizationHandler(rls);
    }


    @Override
    protected List<ResourceLibraryEntry> getResults ( @NonNull ResourceLibrarySynchronizeRequest msg ) throws ResourceLibraryException {
        return getSynchronizationHandler(msg.getLibraryType()).synchronize(
            StructuralObjectReferenceImpl.fromObject(msg.getServiceTarget()),
            msg.getHint(),
            msg.getUpdate(),
            msg.getAdd(),
            msg.getDelete());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#getMessageType()
     */
    @Override
    public Class<ResourceLibrarySynchronizeRequest> getMessageType () {
        return ResourceLibrarySynchronizeRequest.class;
    }

}
