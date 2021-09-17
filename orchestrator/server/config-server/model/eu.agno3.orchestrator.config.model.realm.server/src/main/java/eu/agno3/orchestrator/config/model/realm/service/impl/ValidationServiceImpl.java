/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.List;
import java.util.Map;

import javax.jws.WebService;
import javax.persistence.EntityManager;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.ServiceServerService;
import eu.agno3.orchestrator.config.model.realm.server.util.ModelObjectValidationUtil;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.config.model.realm.service.ValidationService;
import eu.agno3.orchestrator.config.model.realm.service.ValidationServiceDescriptor;
import eu.agno3.orchestrator.config.model.validation.ViolationEntry;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    ValidationService.class, SOAPWebService.class,
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.config.model.realm.service.ValidationService",
    targetNamespace = ValidationServiceDescriptor.NAMESPACE,
    serviceName = ValidationServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/realm/validation" )
public class ValidationServiceImpl implements ValidationService {

    private DefaultServerServiceContext sctx;
    private PersistenceUtil persistenceUtil;
    private ModelObjectValidationUtil validationUtil;
    private ServiceServerService serviceService;


    @Reference
    protected synchronized void setContext ( DefaultServerServiceContext ctx ) {
        this.sctx = ctx;
    }


    protected synchronized void unsetContext ( DefaultServerServiceContext ctx ) {
        if ( this.sctx == ctx ) {
            this.sctx = null;
        }
    }


    @Reference
    protected synchronized void setValidationUtil ( ModelObjectValidationUtil vu ) {
        this.validationUtil = vu;
    }


    protected synchronized void unsetValidationUtil ( ModelObjectValidationUtil vu ) {
        if ( this.validationUtil == vu ) {
            this.validationUtil = null;
        }
    }


    @Reference
    protected synchronized void setPersistenceUtil ( PersistenceUtil pu ) {
        this.persistenceUtil = pu;
    }


    protected synchronized void unsetPersistenceUtil ( PersistenceUtil pu ) {
        if ( this.persistenceUtil == pu ) {
            this.persistenceUtil = null;
        }
    }


    @Reference
    protected synchronized void setServiceService ( ServiceServerService ss ) {
        this.serviceService = ss;
    }


    protected synchronized void unsetServiceService ( ServiceServerService ss ) {
        if ( this.serviceService == ss ) {
            this.serviceService = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelObjectException
     * @throws ModelObjectValidationException
     *
     * @see eu.agno3.orchestrator.config.model.realm.service.ValidationService#validateObject(eu.agno3.orchestrator.config.model.realm.ConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.StructuralObject)
     */
    @Override
    @RequirePermissions ( "config:view:forValidation" )
    public <T extends ConfigurationObject> List<ViolationEntry> validateObject ( T obj, StructuralObject anchor )
            throws ModelServiceException, ModelObjectValidationException, ModelObjectException {
        EntityManager em = this.sctx.createConfigEM();
        AbstractStructuralObjectImpl persistentAnchor = this.persistenceUtil.fetch(em, AbstractStructuralObjectImpl.class, anchor);
        Map<ServiceStructuralObject, ConfigurationInstance> contextConfigs = this.serviceService.getEffectiveContextConfigs(em, persistentAnchor);
        return this.validationUtil.validateObject(em, obj, persistentAnchor, contextConfigs);
    }

}
