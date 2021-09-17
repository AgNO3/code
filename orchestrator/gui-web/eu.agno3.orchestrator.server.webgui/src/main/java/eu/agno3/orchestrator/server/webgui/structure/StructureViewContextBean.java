/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.11.2013 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure;


import java.io.Serializable;
import java.util.UUID;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.GroupStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectType;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;


/**
 * @author mbechler
 * 
 * 
 * @see StructureCacheBean
 */
@Named ( "structureViewContext" )
@ViewScoped
public class StructureViewContextBean implements Serializable {

    private static final Logger log = Logger.getLogger(StructureViewContextBean.class);
    /**
     * 
     */
    private static final long serialVersionUID = 6703827374312257115L;

    private UUID selectedObjectId;
    private UUID selectedAnchorId;

    @Inject
    private StructureCacheBean structureCache;

    @Inject
    private StructureUtil structureUtil;

    private transient StructuralObject obj;
    private transient StructuralObject anchorObj;


    /**
     * @return the selectedObjectId
     */
    public UUID getSelectedObjectId () {
        return this.selectedObjectId;
    }


    /**
     * While the selectedObject specifies the actual object worked on,
     * anchor allows to specify that the menu sticks to another object
     * 
     * @return the selectedAnchorId
     */
    public UUID getSelectedAnchorId () {
        if ( this.selectedAnchorId != null ) {
            return this.selectedAnchorId;
        }
        return getSelectedObjectId();
    }


    /**
     * @param selectedObjectId
     *            the selectedObjectId to set
     */
    public void setSelectedObjectId ( UUID selectedObjectId ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Got object id " + selectedObjectId); //$NON-NLS-1$
        }
        this.selectedObjectId = selectedObjectId;
        this.obj = null;
    }


    /**
     * @param selectedAnchorId
     *            the selectedAnchorId to set
     */
    public void setSelectedAnchorId ( UUID selectedAnchorId ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Got anchor id " + selectedAnchorId); //$NON-NLS-1$
        }
        this.selectedAnchorId = selectedAnchorId;
    }


    /**
     * @return the selected object instance
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public StructuralObject getSelectedObject () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( this.selectedObjectId == null ) {
            return null;
        }

        if ( this.obj == null ) {
            this.obj = this.structureCache.getById(this.selectedObjectId);
            if ( log.isDebugEnabled() ) {
                log.debug("Got object " + this.obj); //$NON-NLS-1$
            }
        }
        return this.obj;
    }


    /**
     * 
     * @return if an anchor is specified, the anchor
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public StructuralObject getSelectedAnchor () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( this.selectedAnchorId == null ) {
            return getSelectedObject();
        }

        if ( this.anchorObj == null ) {
            this.anchorObj = this.structureCache.getById(this.selectedAnchorId);
        }
        return this.anchorObj;
    }


    /**
     * 
     * @return the display name for the selected object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public String getSelectedDisplayName () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return this.structureUtil.getDisplayName(this.getSelectedObject());
    }


    /**
     * 
     * @return the structural parent of the selected object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public StructuralObject getParentForSelection () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return this.structureCache.getParentFor(this.getSelectedObject());
    }


    private boolean isTypeSelected ( StructuralObjectType type ) {
        try {
            return this.getSelectedObject() != null && this.getSelectedObject().getType() == type;
        }
        catch (
            ModelServiceException |
            GuiWebServiceException |
            ModelObjectNotFoundException e ) {
            log.debug("Could not get selected object", e); //$NON-NLS-1$
            return false;
        }

    }


    /**
     * 
     * @return whether a group object is selected
     */
    public boolean isGroupSelected () {
        return isTypeSelected(StructuralObjectType.GROUP);
    }


    /**
     * 
     * @return the selected group or null if none selected
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public GroupStructuralObject getSelectedGroup () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( !isGroupSelected() ) {
            return null;
        }
        return (GroupStructuralObject) this.getSelectedObject();
    }


    /**
     * 
     * @return whether an instance object is selected
     */
    public boolean isInstanceSelected () {
        return isTypeSelected(StructuralObjectType.INSTANCE);
    }


    /**
     * 
     * @return the selected instance or null if none is selected
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public InstanceStructuralObject getSelectedInstance () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( !isInstanceSelected() ) {
            return null;
        }

        return (InstanceStructuralObject) this.getSelectedObject();
    }


    /**
     * 
     * @return whether a service object is selected
     */
    public boolean isServiceSelected () {
        return isTypeSelected(StructuralObjectType.SERVICE);
    }


    /**
     * 
     * @return the selected service or null if none is selected
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public ServiceStructuralObject getSelectedService () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( !isServiceSelected() ) {
            return null;
        }
        return (ServiceStructuralObject) this.getSelectedObject();
    }


    /**
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * 
     */
    public void refreshSelected () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        StructuralObject cachedObj = this.getSelectedObject();
        if ( cachedObj != null ) {
            this.structureCache.flush(cachedObj.getId());
            this.obj = null;
        }
        else if ( this.selectedObjectId != null ) {
            this.structureCache.flush(this.selectedObjectId);
        }

    }
}
