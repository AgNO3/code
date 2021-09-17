/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.info;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.webgui.service.file.URLFileSelectionBean;


/**
 * @author mbechler
 *
 */
@Named ( "fileRenameBean" )
@ViewScoped
public class FileRenameBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6935176637760270232L;

    @Inject
    private URLFileSelectionBean fileSelection;

    private String newName;
    private String origName;


    /**
     * @return the newName
     */
    public String getNewName () {
        @Nullable
        VFSEntity singleSelection = getSelection();
        if ( this.newName == null && singleSelection != null ) {
            this.newName = singleSelection.getLocalName();
            this.origName = singleSelection.getLocalName();
        }
        return this.newName;
    }


    /**
     * @return
     */
    protected @Nullable VFSEntity getSelection () {
        return this.fileSelection.getSingleSelection();
    }


    /**
     * @param newName
     *            the newName to set
     */
    public void setNewName ( String newName ) {
        this.newName = newName;
    }


    /**
     * @return the origName
     */
    public String getOrigName () {
        return this.origName;
    }


    /**
     * 
     */
    public void clear () {
        this.newName = null;
        this.origName = null;
    }

}
