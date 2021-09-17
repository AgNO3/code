/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.share;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.webgui.service.file.URLFileSelectionBean;
import eu.agno3.fileshare.webgui.service.file.picker.OwnedEntityPicker;
import eu.agno3.fileshare.webgui.service.tree.EntityTreeNode;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "shareEntitySelection" )
public class ShareEntitySelection {

    @Inject
    private URLFileSelectionBean urlSelection;

    @Inject
    private OwnedEntityPicker entityPicker;


    /**
     * 
     * @return whether the selection is from the picker
     */
    public boolean isFromPicker () {
        return this.entityPicker.getSelection() instanceof EntityTreeNode;
    }


    /**
     * 
     * @return the selected entity
     */
    public VFSEntity getSingleSelection () {
        if ( this.entityPicker.getSelection() instanceof EntityTreeNode ) {
            return ( (EntityTreeNode) this.entityPicker.getSelection() ).getAttachedObject();
        }
        return this.urlSelection.getSingleSelection();
    }


    /**
     * @return the selected entity id
     */
    public EntityKey getSingleSelectionId () {
        if ( this.entityPicker.getSelection() instanceof EntityTreeNode ) {
            return ( (EntityTreeNode) this.entityPicker.getSelection() ).getAttachedObject().getEntityKey();
        }
        return this.urlSelection.getSingleSelectionId();
    }


    /**
     * 
     */
    public void refreshSelection () {
        this.urlSelection.refreshSelection();
    }
}
