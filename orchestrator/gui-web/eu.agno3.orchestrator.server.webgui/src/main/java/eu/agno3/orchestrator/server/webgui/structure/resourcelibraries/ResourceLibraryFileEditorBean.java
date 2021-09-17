/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.resourcelibraries;


import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;

import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryFileInfo;
import eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureCacheBean;


/**
 * @author mbechler
 *
 */
@Named ( "resourceLibraryFileEditorBean" )
@ViewScoped
public class ResourceLibraryFileEditorBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4412558156085944294L;
    private static final Logger log = Logger.getLogger(ResourceLibraryFileEditorBean.class);

    private FileNode selectedFile;

    private byte[] selectedFileData;

    private String dataLoadedForFile;

    private boolean inCreateMode;

    private String createFilename;

    @Inject
    private ResourceLibraryEditorBean editor;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private StructureCacheBean structureCache;

    private boolean fromUpload;


    /**
     * @return the selectedFile
     */
    public FileNode getSelectedFile () {
        return this.selectedFile;
    }


    /**
     * @param selectedFile
     *            the selectedFile to set
     */
    public void setSelectedFile ( FileNode selectedFile ) {
        this.selectedFile = selectedFile;
    }


    public String getFilename () {
        if ( this.inCreateMode ) {
            return this.getCreateFilename();
        }

        if ( this.selectedFile != null ) {
            return this.selectedFile.getData().getPath();
        }

        return null;
    }


    /**
     * @return the createFilename
     */
    public String getCreateFilename () {
        return this.createFilename;
    }


    /**
     * @param createFilename
     *            the createFilename to set
     */
    public void setCreateFilename ( String createFilename ) {
        this.createFilename = createFilename;
    }


    public byte[] getSelectedFileData () {
        loadSelectedFileData();
        return this.selectedFileData;
    }


    /**
     * @param selectedFileData
     *            the selectedFileData to set
     */
    public void setSelectedFileData ( byte[] selectedFileData ) {
        this.selectedFileData = selectedFileData;
    }


    public String getSelectedFileDataString () {
        byte[] data = getSelectedFileData();
        if ( data != null ) {
            return new String(data, StandardCharsets.UTF_8);
        }
        return null;
    }


    public void setSelectedFileDataString ( String s ) {
        if ( s != null ) {
            this.selectedFileData = s.getBytes(StandardCharsets.UTF_8);
        }
        else {
            this.selectedFileData = new byte[0];
        }
    }


    /**
     * @return the inCreateMode
     */
    public boolean getInCreateMode () {
        return this.inCreateMode;
    }


    /**
     * @param inCreateMode
     *            the inCreateMode to set
     */
    public void setInCreateMode ( boolean inCreateMode ) {
        this.inCreateMode = inCreateMode;
    }


    /**
     * 
     * @return null
     */
    public String cancelCreate () {
        this.inCreateMode = false;
        return null;
    }


    /**
     * 
     */
    private void loadSelectedFileData () {

        if ( this.selectedFile == null || this.inCreateMode ) {
            return;
        }

        String path = this.selectedFile.getData().getPath();
        if ( this.dataLoadedForFile != null && this.dataLoadedForFile.equals(path) ) {
            return;
        }

        try {

            this.dataLoadedForFile = path;
            DataHandler file = this.ssp.getService(ResourceLibraryService.class).getFile(this.editor.getSelectedLibraryId(), path);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(file.getInputStream(), bos);
            this.selectedFileData = bos.toByteArray();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
    }


    public void refresh () {
        if ( this.selectedFile == null || this.inCreateMode ) {
            return;
        }
        reloadData();
        this.editor.refresh();
    }


    public String writeFile () {

        if ( this.selectedFile == null ) {
            return null;
        }

        String path = this.selectedFile.getData().getPath();
        if ( this.dataLoadedForFile == null || !this.dataLoadedForFile.equals(path) ) {
            return null;
        }

        byte[] byteData = this.selectedFileData;
        writeFileData(path, byteData, false);
        return null;
    }


    /**
     * @param path
     * @param byteData
     */
    private void writeFileData ( String path, byte[] byteData, boolean create ) {
        try {
            if ( byteData != null && byteData.length > 0 ) {
                DataSource ds = new ByteArrayDataSource(byteData, "application/octet-stream"); //$NON-NLS-1$
                this.ssp.getService(ResourceLibraryService.class).putFile(this.editor.getSelectedLibraryId(), create, path, new DataHandler(ds));
            }
            else {
                this.ssp.getService(ResourceLibraryService.class).putEmptyFile(this.editor.getSelectedLibraryId(), create, path);
            }
            this.inCreateMode = false;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
    }


    public void returnFromDelete ( SelectEvent ev ) {
        this.selectedFile = null;
        this.selectedFileData = null;
        this.editor.refresh();
    }


    /**
     * 
     * @param name
     * @param data
     */
    public void createFromData ( String name, byte[] data ) {
        this.selectedFileData = data;
        this.createFilename = name;
        this.fromUpload = false;
        doCreate();
    }


    public void createFromUpload ( FileUploadEvent ev ) {
        this.selectedFileData = ev.getFile().getContents();
        this.fromUpload = true;

        if ( StringUtils.isBlank(this.createFilename) ) {
            this.createFilename = ev.getFile().getFileName();
        }
    }


    public void updateFromUpload ( FileUploadEvent ev ) {
        if ( this.selectedFile == null ) {
            return;
        }

        this.selectedFileData = ev.getFile().getContents();
        String path = this.selectedFile.getData().getPath();
        this.dataLoadedForFile = path;
        this.fromUpload = true;
        writeFileData(path, this.selectedFileData, false);

        FacesContext.getCurrentInstance().addMessage(
            null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, GuiMessages.get("resourceLibrary.fileReplaced"), StringUtils.EMPTY)); //$NON-NLS-1$
    }


    /**
     * @param createCopyPath
     * @return null
     */
    public String doCreateCopy ( String createCopyPath ) {
        try {
            DataHandler file = this.ssp.getService(ResourceLibraryService.class).getFile(this.editor.getSelectedLibraryId(), createCopyPath);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(file.getInputStream(), bos);
            this.selectedFileData = bos.toByteArray();
            this.fromUpload = false;
            return doCreate();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }


    public String doCreate () {
        if ( StringUtils.isBlank(this.createFilename) ) {
            return null;
        }
        writeFileData(this.createFilename, this.selectedFileData, true);
        this.editor.refresh();
        FileNode fileNode = this.editor.getFileNode(this.createFilename);
        if ( fileNode == null && log.isDebugEnabled() ) {
            log.debug("Did not find node for " + this.createFilename); //$NON-NLS-1$
        }
        this.setSelectedFile(fileNode);
        this.createFilename = null;
        this.fromUpload = false;
        return null;
    }


    public String getInheritedTitle ( ResourceLibraryFileInfo fi ) {
        if ( !fi.getInherited() ) {
            return StringUtils.EMPTY;
        }

        if ( fi.getGlobalDefault() ) {
            return GuiMessages.get("resourceLibrary.inherited.default"); //$NON-NLS-1$
        }

        String anchorName;
        try {
            anchorName = this.structureCache.getById(fi.getAnchorId()).getDisplayName();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            anchorName = fi.getAnchorId().toString();
        }

        return GuiMessages.format("resourceLibrary.inherited.from", fi.getLibraryName(), anchorName); //$NON-NLS-1$
    }


    public String reloadData () {
        this.dataLoadedForFile = null;
        this.loadSelectedFileData();
        return null;
    }


    public String createFile () {
        this.inCreateMode = true;
        return null;
    }


    /**
     * @return whether the file data is uploaded
     */
    public boolean isUpload () {
        return this.fromUpload;
    }

}
