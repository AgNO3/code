/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.11.2013 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.service;


import java.util.UUID;

import javax.inject.Named;

import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectType;
import eu.agno3.orchestrator.server.webgui.structure.menu.StructuralObjectContextMenuController;


/**
 * @author mbechler
 * 
 */
@Named ( "serviceContextMenuController" )
public class ServiceContextMenuController extends StructuralObjectContextMenuController {

    private UUID getSelectedServiceId ( boolean fromContext ) {
        StructuralObject obj = this.getSelectedObject(fromContext);

        if ( obj == null ) {
            return null;
        }

        if ( obj.getType() != StructuralObjectType.SERVICE ) {
            return null;
        }

        return obj.getId();
    }


    public String configure ( boolean fromContext ) {
        UUID selectedId = this.getSelectedServiceId(fromContext);
        if ( selectedId != null ) {
            return "/structure/service/config.xhtml?faces-redirect=true&cid=&service=" + selectedId; //$NON-NLS-1$
        }

        return null;
    }


    public String deleteService ( boolean fromContext ) {
        UUID selectedId = this.getSelectedServiceId(fromContext);
        if ( selectedId != null ) {
            return "/structure/service/delete.xhtml?faces-redirect=true&cid=&service=" + selectedId; //$NON-NLS-1$
        }

        return null;
    }

}
