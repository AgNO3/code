/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.menu;


import java.util.UUID;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.server.webgui.config.template.TemplateController;
import eu.agno3.orchestrator.server.webgui.menu.StructuralObjectAdapter;
import eu.agno3.orchestrator.server.webgui.menu.TreeMenuStateBean;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 * 
 */
public class StructuralObjectContextMenuController {

    private static final Logger log = Logger.getLogger(StructuralObjectContextMenuController.class);

    @Inject
    private TreeMenuStateBean state;

    @Inject
    private StructureViewContextBean structureContext;

    @Inject
    private TemplateController tplController;


    /**
     * 
     */
    public StructuralObjectContextMenuController () {
        super();
    }


    protected StructuralObject getSelectedObject ( boolean fromContext ) {
        if ( fromContext ) {
            try {
                return this.structureContext.getSelectedAnchor();
            }
            catch ( Exception e ) {
                log.debug("Failed to get anchor object", e); //$NON-NLS-1$
                return null;
            }
        }

        StructuralObjectAdapter<?> node = (StructuralObjectAdapter<?>) this.state.getSelectedNode();

        if ( node == null ) {
            log.debug("No node selected"); //$NON-NLS-1$
            return null;
        }

        return node.getAttachedObject();
    }


    /**
     * @param fromContext
     * @return
     */
    protected UUID getSelectedObjectId ( boolean fromContext ) {

        if ( fromContext ) {
            return this.structureContext.getSelectedObjectId();
        }

        StructuralObject obj = this.getSelectedObject(fromContext);

        if ( obj == null ) {
            return null;
        }

        return obj.getId();
    }


    public String objects () {
        return this.objects(false);
    }


    public String addTemplate ( boolean fromContext ) {
        return this.tplController.addTemplate(this.getSelectedObject(fromContext));
    }


    public String objects ( boolean fromContext ) {
        UUID selectedId = this.getSelectedObjectId(fromContext);
        if ( selectedId != null ) {
            return "/structure/objects.xhtml?faces-redirect=true&cid=&anchor=" + selectedId; //$NON-NLS-1$
        }

        return null;
    }

}