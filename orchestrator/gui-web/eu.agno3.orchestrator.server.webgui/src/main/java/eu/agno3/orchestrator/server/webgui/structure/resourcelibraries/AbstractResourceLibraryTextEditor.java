/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.resourcelibraries;


import javax.inject.Inject;

import org.primefaces.event.FileUploadEvent;


/**
 * @author mbechler
 *
 */
public abstract class AbstractResourceLibraryTextEditor {

    /**
     * 
     */
    public static final int CREATE_MODE_EMPTY = 0;


    protected abstract boolean validateSource ();


    protected abstract String getPathName ();


    protected abstract String makeDefault ();

    /**
     * 
     */
    public static final int CREATE_MODE_DEFAULT = 1;
    /**
     * 
     */
    public static final int CREATE_MODE_COPY = 2;
    public static final int CREATE_MODE_UPLOAD = 3;

    @Inject
    private ResourceLibraryFileEditorBean fileEditor;

    private String createCopyPath;
    private int createMode = CREATE_MODE_DEFAULT;


    /**
     * 
     */
    public AbstractResourceLibraryTextEditor () {
        super();
    }


    /**
     * @return the fileEditor
     */
    public ResourceLibraryFileEditorBean getFileEditor () {
        return this.fileEditor;
    }


    /**
     * 
     * @return the template text
     */
    public String getText () {
        return this.fileEditor.getSelectedFileDataString();
    }


    /**
     * 
     * @param text
     */
    public void setText ( String text ) {
        this.fileEditor.setSelectedFileDataString(text);
    }


    /**
     * @return the createMode
     */
    public int getCreateMode () {
        return this.createMode;
    }


    /**
     * @param createMode
     *            the createMode to set
     */
    public void setCreateMode ( int createMode ) {
        this.createMode = createMode;
    }


    /**
     * @return the createCopyPath
     */
    public String getCreateCopyPath () {
        return this.createCopyPath;
    }


    /**
     * @param createCopyPath
     *            the createCopyPath to set
     */
    public void setCreateCopyPath ( String createCopyPath ) {
        this.createCopyPath = createCopyPath;
    }


    /**
     * 
     * @return null
     */
    public String doCreate () {
        this.fileEditor.setCreateFilename(getPathName());

        switch ( this.createMode ) {
        case CREATE_MODE_DEFAULT:
            if ( !this.fileEditor.isUpload() ) {
                setText(makeDefault());
            }
        case CREATE_MODE_EMPTY:
            return this.fileEditor.doCreate();
        case CREATE_MODE_COPY:
            return this.fileEditor.doCreateCopy(this.createCopyPath);
        default:
            return null;
        }

    }


    public String cancelCreate () {
        return this.fileEditor.cancelCreate();
    }


    /**
     * 
     * @return null
     */
    public String save () {
        if ( !validateSource() ) {
            return null;
        }
        return this.fileEditor.writeFile();
    }


    /**
     * 
     * @param ev
     */
    public void onUpload ( FileUploadEvent ev ) {
        this.fileEditor.setSelectedFileData(ev.getFile().getContents());
    }


    /**
     * 
     * @return null
     */
    public String revert () {
        return this.fileEditor.reloadData();
    }

}