/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.11.2013 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.group;


import java.util.UUID;

import javax.inject.Named;

import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectType;
import eu.agno3.orchestrator.server.webgui.structure.menu.StructuralObjectContextMenuController;


/**
 * @author mbechler
 * 
 */
@Named ( "structuralGroupContextMenuController" )
public class StructuralGroupContextMenuController extends StructuralObjectContextMenuController {

    private UUID getSelectedGroupId ( boolean fromContext ) {
        StructuralObject obj = getSelectedObject(fromContext);
        if ( obj == null || obj.getType() != StructuralObjectType.GROUP ) {
            return null;
        }

        return obj.getId();
    }


    public String addGroup ( boolean fromContext ) {
        UUID selectedId = this.getSelectedGroupId(fromContext);
        if ( selectedId != null ) {
            return "/structure/group/add.xhtml?faces-redirect=true&cid=&parentGroup=" + selectedId; //$NON-NLS-1$
        }

        return null;
    }


    public String addInstance ( boolean fromContext ) {
        UUID selectedId = this.getSelectedGroupId(fromContext);
        if ( selectedId != null ) {
            return "/structure/instance/add.xhtml?faces-redirect=true&cid=&parent=" + selectedId; //$NON-NLS-1$
        }

        return null;
    }


    public String deleteGroup ( boolean fromContext ) {
        UUID selectedId = this.getSelectedGroupId(fromContext);
        if ( selectedId != null ) {
            return "/structure/group/delete.xhtml?faces-redirect=true&cid=&group=" + selectedId; //$NON-NLS-1$
        }

        return null;
    }

}
