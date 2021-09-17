/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.11.2013 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.instance;


import java.util.UUID;

import javax.inject.Named;

import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectType;
import eu.agno3.orchestrator.server.webgui.structure.menu.StructuralObjectContextMenuController;


/**
 * @author mbechler
 * 
 */
@Named ( "instanceContextMenuController" )
public class InstanceContextMenuController extends StructuralObjectContextMenuController {

    private UUID getSelectedInstanceId ( boolean fromContext ) {
        StructuralObject obj = this.getSelectedObject(fromContext);

        if ( obj.getType() != StructuralObjectType.INSTANCE ) {
            return null;
        }

        return obj.getId();
    }


    public String deleteInstance ( boolean fromContext ) {
        UUID selectedInstanceId = this.getSelectedInstanceId(fromContext);
        if ( selectedInstanceId != null ) {
            return "/structure/instance/delete.xhtml?faces-redirect=true&cid=&instance=" + selectedInstanceId; //$NON-NLS-1$
        }
        return null;
    }


    public String addService ( boolean fromContext ) {
        UUID selectedInstanceId = this.getSelectedInstanceId(fromContext);
        if ( selectedInstanceId != null ) {
            return "/structure/service/add.xhtml?faces-redirect=true&cid=&instance=" + selectedInstanceId; //$NON-NLS-1$
        }
        return null;
    }


    public String rebootInstance ( boolean fromContext ) {
        UUID selectedInstanceId = this.getSelectedInstanceId(fromContext);
        if ( selectedInstanceId != null ) {
            return "/structure/instance/reboot.xhtml?faces-redirect=true&cid=&instance=" + selectedInstanceId; //$NON-NLS-1$
        }
        return null;
    }


    public String shutdownInstance ( boolean fromContext ) {
        UUID selectedInstanceId = this.getSelectedInstanceId(fromContext);
        if ( selectedInstanceId != null ) {
            return "/structure/instance/shutdown.xhtml?faces-redirect=true&cid=&instance=" + selectedInstanceId; //$NON-NLS-1$
        }
        return null;
    }


    public String renameInstance ( boolean fromContext ) {
        UUID selectedInstanceId = this.getSelectedInstanceId(fromContext);
        if ( selectedInstanceId != null ) {
            return "/structure/instance/rename.xhtml?faces-redirect=true&cid=&instance=" + selectedInstanceId; //$NON-NLS-1$
        }
        return null;
    }


    public String changeShellPassword ( boolean fromContext ) {
        UUID selectedInstanceId = this.getSelectedInstanceId(fromContext);
        if ( selectedInstanceId != null ) {
            return "/structure/instance/changeShellPassword.xhtml?faces-redirect=true&cid=&instance=" + selectedInstanceId; //$NON-NLS-1$
        }
        return null;
    }


    public String forceApplyConfig ( boolean fromContext ) {
        UUID selectedInstanceId = this.getSelectedInstanceId(fromContext);
        if ( selectedInstanceId != null ) {
            return "/structure/instance/applyConfig.xhtml?faces-redirect=true&cid=&instance=" + selectedInstanceId; //$NON-NLS-1$
        }
        return null;
    }

}
