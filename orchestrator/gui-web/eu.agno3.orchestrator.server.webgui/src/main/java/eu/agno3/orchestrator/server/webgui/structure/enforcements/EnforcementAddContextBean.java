/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.enforcements;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectReference;
import eu.agno3.orchestrator.config.model.realm.service.InheritanceService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.config.ConfigUtil;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 * 
 */
@ViewScoped
@Named ( "enforcementAddContext" )
public class EnforcementAddContextBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5422933831609318152L;
    private String objectTypeName;
    private ConfigurationObject object;

    @Inject
    private StructureViewContextBean structureContext;

    @Inject
    private ServerServiceProvider ssp;


    /**
     * @return the object
     */
    public ConfigurationObject getObject () {
        return this.object;
    }


    /**
     * @param object
     *            the object to set
     */
    public void setObject ( ConfigurationObject object ) {
        this.object = object;
    }


    /**
     * @return the objectTypeName
     */
    public String getObjectTypeName () {
        return this.objectTypeName;
    }


    /**
     * @param objectTypeName
     *            the objectTypeName to set
     */
    public void setObjectTypeName ( String objectTypeName ) {
        this.objectTypeName = objectTypeName;
    }


    public List<ConfigurationObjectReference> getTemplates () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return getTemplatesForTypeName(this.objectTypeName);
    }


    public List<ConfigurationObjectReference> getTemplatesFor ( ConfigurationObject obj ) throws ModelObjectNotFoundException, ModelServiceException,
            GuiWebServiceException {
        return getTemplatesForTypeName(ConfigUtil.getObjectTypeName(obj));
    }


    public List<ConfigurationObjectReference> getTemplatesForTypeName ( String objectType ) throws ModelObjectNotFoundException,
            ModelServiceException, GuiWebServiceException {
        if ( objectType == null ) {
            return Collections.EMPTY_LIST;
        }

        return new ArrayList<>(this.ssp.getService(InheritanceService.class).getEligibleTemplates(
            this.structureContext.getSelectedObject(),
            objectType,
            StringUtils.EMPTY));
    }

}
