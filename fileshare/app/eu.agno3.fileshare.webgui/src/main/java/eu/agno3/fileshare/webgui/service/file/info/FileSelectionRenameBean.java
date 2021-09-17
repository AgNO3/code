/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.08.2016 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.info;


import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.fileshare.model.VFSEntity;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "fileSelectionRenameBean" )
public class FileSelectionRenameBean extends FileRenameBean {

    /**
     * 
     */
    private static final long serialVersionUID = -2235171299348547230L;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.file.info.FileRenameBean#getSelection()
     */
    @Override
    protected @Nullable VFSEntity getSelection () {
        return null;
    }
}
