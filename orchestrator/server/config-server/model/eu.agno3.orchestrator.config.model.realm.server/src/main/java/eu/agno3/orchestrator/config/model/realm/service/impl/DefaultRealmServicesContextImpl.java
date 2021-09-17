/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.realm.server.service.ConfigurationServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.DefaultRealmServicesContext;
import eu.agno3.orchestrator.config.model.realm.server.service.InheritanceServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.InstanceServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ServiceServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.StructuralObjectServerService;


/**
 * @author mbechler
 *
 */
@Component ( service = DefaultRealmServicesContext.class )
public class DefaultRealmServicesContextImpl implements DefaultRealmServicesContext {

    private Optional<@NonNull ConfigurationServerService> configurationService = Optional.empty();
    private Optional<@NonNull InstanceServerService> instanceService = Optional.empty();
    private Optional<@NonNull StructuralObjectServerService> structureService = Optional.empty();
    private Optional<@NonNull ServiceServerService> serviceService = Optional.empty();
    private Optional<@NonNull InheritanceServerService> inheritanceService = Optional.empty();


    @Reference
    protected synchronized void setConfigurationService ( @NonNull ConfigurationServerService css ) {
        this.configurationService = Optional.of(css);
    }


    protected synchronized void unsetConfigurationService ( ConfigurationServerService css ) {
        if ( this.configurationService.equals(css) ) {
            this.configurationService = Optional.empty();
        }
    }


    @Reference
    protected synchronized void setInstanceService ( @NonNull InstanceServerService iss ) {
        this.instanceService = Optional.of(iss);
    }


    protected synchronized void unsetInstanceService ( InstanceServerService iss ) {
        if ( this.instanceService.equals(iss) ) {
            this.instanceService = Optional.empty();
        }
    }


    @Reference
    protected synchronized void setStructureService ( @NonNull StructuralObjectServerService sos ) {
        this.structureService = Optional.of(sos);
    }


    protected synchronized void unsetStructureService ( StructuralObjectServerService sos ) {
        if ( this.structureService.equals(sos) ) {
            this.structureService = Optional.empty();
        }
    }


    @Reference
    protected synchronized void setServiceService ( @NonNull ServiceServerService ss ) {
        this.serviceService = Optional.of(ss);
    }


    protected synchronized void unsetServiceService ( ServiceServerService ss ) {
        if ( this.serviceService.equals(ss) ) {
            this.serviceService = Optional.empty();
        }
    }


    @Reference
    protected synchronized void setInheritanceService ( @NonNull InheritanceServerService iss ) {
        this.inheritanceService = Optional.of(iss);
    }


    protected synchronized void unsetInheritanceService ( InheritanceServerService iss ) {
        if ( this.inheritanceService.equals(iss) ) {
            this.inheritanceService = Optional.empty();
        }
    }


    /**
     * @return the configurationService
     */
    @Override
    public synchronized @NonNull ConfigurationServerService getConfigurationService () {
        return this.configurationService.get();
    }


    /**
     * @return the instanceService
     */
    @Override
    public synchronized @NonNull InstanceServerService getInstanceService () {
        return this.instanceService.get();
    }


    /**
     * @return the serviceService
     */
    @Override
    public synchronized @NonNull ServiceServerService getServiceService () {
        return this.serviceService.get();
    }


    /**
     * @return the structureService
     */
    @Override
    public synchronized @NonNull StructuralObjectServerService getStructureService () {
        return this.structureService.get();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.DefaultRealmServicesContext#getInheritanceService()
     */
    @Override
    public synchronized @NonNull InheritanceServerService getInheritanceService () {
        return this.inheritanceService.get();
    }
}
