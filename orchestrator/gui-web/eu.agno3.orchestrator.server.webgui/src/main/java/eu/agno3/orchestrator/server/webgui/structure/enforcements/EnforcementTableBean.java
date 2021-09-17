/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.enforcements;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.service.EnforcementService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.config.ObjectTypeProvider;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 * 
 */
@Named ( "enforcementsTableBean" )
@ViewScoped
public class EnforcementTableBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4430859461308151720L;

    private static final Logger log = Logger.getLogger(EnforcementTableBean.class);

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private StructureViewContextBean viewContext;

    @Inject
    private ObjectTypeProvider objectTypeRegistry;

    private List<ConfigurationObject> localModel;
    private List<ConfigurationObject> inheritedModel;

    private List<ConfigurationObject> cachedEnforcements;
    private Set<String> cachedObjectTypes;

    private List<ConfigurationObject> cachedInheritedEnforcements;


    public List<ConfigurationObject> getLocalModel () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( this.localModel == null ) {
            this.localModel = this.createLocalModel();
        }
        return this.localModel;
    }


    public List<ConfigurationObject> getInheritedModel () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( this.inheritedModel == null ) {
            this.inheritedModel = this.createInheritedModel();
        }
        return this.inheritedModel;
    }


    public List<String> getUnsetObjectTypes () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        Set<String> objTypes = new HashSet<>(this.objectTypeRegistry.getObjectTypes());
        objTypes.removeAll(this.getSetObjectTypes());
        return new ArrayList<>(objTypes);
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private Set<String> getSetObjectTypes () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( this.cachedObjectTypes != null ) {
            return this.cachedObjectTypes;
        }

        Set<String> presentObjectTypes = new HashSet<>();

        for ( ConfigurationObject obj : this.getLocalEnforcements() ) {
            ObjectTypeName t = obj.getClass().getAnnotation(ObjectTypeName.class);
            if ( t != null ) {
                presentObjectTypes.add(t.value());
            }
        }

        this.cachedObjectTypes = presentObjectTypes;

        return presentObjectTypes;
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private List<ConfigurationObject> createLocalModel () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return getLocalEnforcements();
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private List<ConfigurationObject> createInheritedModel () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return getInheritedEnforcements();
    }


    private List<ConfigurationObject> getLocalEnforcements () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( this.cachedEnforcements != null ) {
            return this.cachedEnforcements;
        }
        StructuralObject selectedObject = this.viewContext.getSelectedObject();
        if ( log.isDebugEnabled() ) {
            log.debug("Fetching local enforced objects for " + selectedObject); //$NON-NLS-1$
        }
        List<ConfigurationObject> res;
        Set<ConfigurationObject> objs = this.ssp.getService(EnforcementService.class).fetchEnforcements(selectedObject);
        if ( objs == null ) {
            res = Collections.EMPTY_LIST;
        }
        else {
            res = new ArrayList<>(objs);
        }

        this.cachedEnforcements = res;

        return res;
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private List<ConfigurationObject> getInheritedEnforcements () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( this.cachedInheritedEnforcements != null ) {
            return this.cachedInheritedEnforcements;
        }
        StructuralObject selectedObject = this.viewContext.getSelectedObject();
        if ( log.isDebugEnabled() ) {
            log.debug("Fetching inherited enforced objects for " + selectedObject); //$NON-NLS-1$
        }
        List<ConfigurationObject> res;
        Set<ConfigurationObject> objs = this.ssp.getService(EnforcementService.class).fetchInheritedEnforcements(selectedObject);
        if ( objs == null ) {
            res = Collections.EMPTY_LIST;
        }
        else {
            res = new ArrayList<>(objs);
        }

        this.cachedInheritedEnforcements = res;

        return res;
    }


    public String getDisplayNameFor ( ConfigurationObject obj ) {
        if ( obj.getDisplayName() != null ) {
            return obj.getDisplayName();
        }
        else if ( obj.getName() != null ) {
            return obj.getName();
        }

        throw new IllegalArgumentException("Enforcements cannot be anonymous"); //$NON-NLS-1$
    }


    /**
     * 
     */
    public void refresh () {
        this.cachedEnforcements = null;
        this.cachedObjectTypes = null;
        this.localModel = null;
    }


    public String getSubtitle () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return GuiMessages.format("structure.enforcements.title", this.viewContext.getSelectedDisplayName()); //$NON-NLS-1$
    }
}
