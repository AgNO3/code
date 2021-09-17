/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file;


import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.webgui.service.tree.ui.FileRootSelectionBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "urlFileSelectionUtil" )
public class URLFileSelectionUtil {

    @Inject
    private FileSelectionBean selectionBean;

    @Inject
    private FileRootSelectionBean rootSelectionBean;


    /**
     * @return encoded file selection
     */
    public String getEncodedMultiSelectionFromSelection () {
        Set<VFSEntity> selection = this.selectionBean.getSelectedEntities();
        StringBuilder sb = new StringBuilder();

        for ( VFSEntity e : selection ) {
            sb.append(e.getEntityKey());
            sb.append('/');
        }

        return sb.toString();
    }


    /**
     * @return encoded file selection
     */
    public String getEncodedSingleSelectionFromSelection () {
        if ( this.selectionBean.isSingleSelection() ) {
            return this.selectionBean.getSingleSelectionEntity().getEntityKey().toString();
        }
        return null;
    }


    /**
     * @return encoded file selection
     */
    public String getEncodedSingleSelectionFromSelectionOrRoot () {
        if ( this.selectionBean.isEmptySelection() || this.selectionBean.isEmptyDirSelection() ) {
            VFSContainerEntity rootContainer = this.rootSelectionBean.getRootContainer();
            if ( rootContainer == null ) {
                return null;
            }
            return rootContainer.getEntityKey().toString();
        }
        VFSContainerEntity selectedContainer = this.selectionBean.getSingleSelectionContainer();
        if ( selectedContainer == null ) {
            return null;
        }
        return selectedContainer.getEntityKey().toString();
    }

}
