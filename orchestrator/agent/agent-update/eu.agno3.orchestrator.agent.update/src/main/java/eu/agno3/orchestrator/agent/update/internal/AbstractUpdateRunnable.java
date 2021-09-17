/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.10.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.internal;


import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReferenceImpl;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectType;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepository;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepositoryException;


/**
 * @author mbechler
 *
 */
public abstract class AbstractUpdateRunnable {

    private ConfigRepository configRepository;
    private ServiceTypeRegistry serviceTypeRegistry;


    /**
     * @param configRepository
     * @param serviceTypeRegistry
     * 
     */
    public AbstractUpdateRunnable ( ConfigRepository configRepository, ServiceTypeRegistry serviceTypeRegistry ) {
        super();
        this.configRepository = configRepository;
        this.serviceTypeRegistry = serviceTypeRegistry;
    }


    /**
     * @return
     * @throws ConfigRepositoryException
     */
    protected Collection<StructuralObjectReference> getServicesToUpdate () throws ConfigRepositoryException {
        Collection<StructuralObjectReference> services = new LinkedList<>(this.configRepository.getServiceReferences());
        Set<String> activeTypes = new HashSet<>();
        for ( StructuralObjectReference service : services ) {
            activeTypes.add(service.getLocalType());
        }

        Set<String> missing = new HashSet<>(this.serviceTypeRegistry.getServiceTypes());
        missing.removeAll(activeTypes);
        for ( String inactive : missing ) {
            StructuralObjectReference s = new StructuralObjectReferenceImpl(null, StructuralObjectType.SERVICE, inactive);
            services.add(s);
        }
        return services;
    }
}