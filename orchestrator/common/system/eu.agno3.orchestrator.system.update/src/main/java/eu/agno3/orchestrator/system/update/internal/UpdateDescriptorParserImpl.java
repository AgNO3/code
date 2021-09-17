/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.update.internal;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.system.update.AbstractServiceUpdateUnit;
import eu.agno3.orchestrator.system.update.ServiceUpdateDescriptor;
import eu.agno3.orchestrator.system.update.UpdateDescriptor;
import eu.agno3.orchestrator.system.update.UpdateDescriptorLoader;
import eu.agno3.orchestrator.system.update.UpdateDescriptorParser;
import eu.agno3.orchestrator.system.update.UpdateDescriptorRef;
import eu.agno3.orchestrator.system.update.UpdateException;


/**
 * @author mbechler
 *
 */
@Component ( service = UpdateDescriptorParser.class )
public class UpdateDescriptorParserImpl implements UpdateDescriptorParser {

    private UpdateDescriptorLoader loader;


    @Reference
    protected synchronized void setUpdateDescriptorLoader ( UpdateDescriptorLoader udl ) {
        this.loader = udl;
    }


    protected synchronized void unsetUpdateDescriptorLoader ( UpdateDescriptorLoader udl ) {
        if ( this.loader == udl ) {
            this.loader = null;
        }
    }


    @Override
    public UpdateDescriptor getLatestEffective ( String stream, String imageType, UpdateDescriptor cached ) throws UpdateException {
        return getEffective(stream, this.loader.getLatest(stream, imageType, cached));
    }


    @Override
    public UpdateDescriptor getEffective ( UpdateDescriptorRef ref ) throws UpdateException {
        return getEffective(ref.getStream(), this.loader.getReference(ref));
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.update.UpdateDescriptorParser#getEffective(java.lang.String,
     *      eu.agno3.orchestrator.system.update.UpdateDescriptor)
     */
    @Override
    public UpdateDescriptor getEffective ( String stream, UpdateDescriptor desc ) throws UpdateException {
        if ( desc == null ) {
            return null;
        }
        List<UpdateDescriptor> includes = new ArrayList<>();
        loadIncludes(includes, desc.getIncludes(), new HashSet<>(Collections.singleton(desc.getReference(stream))), 5);
        includes.add(desc);
        return mergeDescriptors(desc, includes);
    }


    /**
     * @param includes
     * @return
     */
    private static UpdateDescriptor mergeDescriptors ( UpdateDescriptor root, List<UpdateDescriptor> includes ) {
        Map<String, List<ServiceUpdateDescriptor>> foundServiceDescriptors = new HashMap<>();
        for ( UpdateDescriptor desc : includes ) {

            for ( ServiceUpdateDescriptor e : desc.getDescriptors() ) {

                List<ServiceUpdateDescriptor> descs = foundServiceDescriptors.get(e.getServiceType());
                if ( descs == null ) {
                    descs = new ArrayList<>();
                    foundServiceDescriptors.put(e.getServiceType(), descs);
                }
                descs.add(e);
            }
        }

        List<ServiceUpdateDescriptor> merged = new ArrayList<>();
        for ( Entry<String, List<ServiceUpdateDescriptor>> e : foundServiceDescriptors.entrySet() ) {
            merged.add(mergeServiceUpdateDescriptors(e.getKey(), e.getValue()));
        }

        UpdateDescriptor mergedDesc = new UpdateDescriptor(root);
        mergedDesc.setIncludes(Collections.EMPTY_LIST);
        mergedDesc.setDescriptors(merged);
        return mergedDesc;
    }


    /**
     * @param serviceDescs
     * @return
     */
    private static ServiceUpdateDescriptor mergeServiceUpdateDescriptors ( String serviceType, List<ServiceUpdateDescriptor> serviceDescs ) {
        ServiceUpdateDescriptor merged = new ServiceUpdateDescriptor();
        merged.setServiceType(serviceType);
        Map<Class<?>, AbstractServiceUpdateUnit<?>> byType = new LinkedHashMap<>();
        for ( ServiceUpdateDescriptor desc : serviceDescs ) {
            if ( desc.getUnits() != null ) {
                for ( AbstractServiceUpdateUnit<?> unit : desc.getUnits() ) {
                    mergeUnit(byType, unit.getType(), unit);
                }
            }
        }
        merged.setUnits(new ArrayList<>(byType.values()));
        return merged;
    }


    /**
     * @param byType
     * @param unit
     */
    @SuppressWarnings ( "unchecked" )
    private static <T extends AbstractServiceUpdateUnit<T>> void mergeUnit ( Map<Class<?>, AbstractServiceUpdateUnit<?>> byType, Class<T> type,
            AbstractServiceUpdateUnit<?> unit ) {
        AbstractServiceUpdateUnit<T> existing = (AbstractServiceUpdateUnit<T>) byType.get(type);
        if ( existing != null ) {
            byType.put(unit.getType(), existing.merge((T) unit));
        }
        else {
            byType.put(unit.getType(), unit);
        }
    }


    /**
     * @param includeRefs
     * @param loadedRefs
     * @param depthLimit
     * @throws UpdateException
     */
    private void loadIncludes ( List<UpdateDescriptor> includes, List<UpdateDescriptorRef> includeRefs, HashSet<UpdateDescriptorRef> loadedRefs,
            int depthLimit ) throws UpdateException {

        if ( depthLimit == 0 ) {
            throw new UpdateException("Include depth limit exceeded"); //$NON-NLS-1$
        }

        for ( UpdateDescriptorRef includeRef : includeRefs ) {
            if ( loadedRefs.contains(includeRef) ) {
                continue;
            }
            loadedRefs.add(includeRef);
            UpdateDescriptor include = this.loader.getReference(includeRef);
            List<UpdateDescriptorRef> includeIncludes = include.getIncludes();
            loadIncludes(includes, includeIncludes, loadedRefs, depthLimit - 1);
            includes.add(include);
        }
    }
}
