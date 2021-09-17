/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.12.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.instance;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureUtil;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "structureRenameBean" )
public class StructureRenameBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8323847343668178090L;

    @Inject
    private StructureViewContextBean structureContext;

    @Inject
    private ServerServiceProvider ssp;

    private String newName;


    public String getNewName () {
        if ( StringUtils.isBlank(this.newName) && this.structureContext.getSelectedObjectId() != null ) {
            try {
                return this.structureContext.getSelectedObject().getDisplayName();
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
            }
        }
        return this.newName;
    }


    /**
     * @param newName
     *            the newName to set
     */
    public void setNewName ( String newName ) {
        this.newName = newName;
    }


    public String rename () {
        try {
            InstanceStructuralObjectImpl selectedInstance = (InstanceStructuralObjectImpl) this.structureContext.getSelectedInstance();
            String name = this.getNewName();
            selectedInstance.setDisplayName(name);
            this.ssp.getService(StructuralObjectService.class).update(selectedInstance);
            this.structureContext.refreshSelected();
            return StructureUtil.getOutcomeForObjectOverview(selectedInstance);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }

        return null;
    }
}
