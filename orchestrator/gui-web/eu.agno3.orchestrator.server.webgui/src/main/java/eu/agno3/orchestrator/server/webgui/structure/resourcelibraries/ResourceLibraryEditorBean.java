/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.resourcelibraries;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrary;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryFileInfo;
import eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 *
 */
@Named ( "resourceLibraryEditorBean" )
@ViewScoped
public class ResourceLibraryEditorBean implements Serializable {

    private static final Logger log = Logger.getLogger(ResourceLibraryEditorBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = 551797536061137890L;

    private TreeNode filesModel;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private StructureViewContextBean structureContext;

    private UUID selectedLibraryId;

    private List<ResourceLibraryFileInfo> allPaths;
    private List<String> allPathNames;

    private String createType;
    private String createName;

    private String libraryName;

    private String libraryType;


    public UUID getSelectedLibraryId () {
        return this.selectedLibraryId;
    }


    /**
     * @param selectedLibraryId
     *            the selectedLibraryId to set
     */
    public void setSelectedLibraryId ( UUID selectedLibraryId ) {
        this.selectedLibraryId = selectedLibraryId;
    }


    /**
     * @return the createName
     */
    public String getCreateName () {
        return this.createName;
    }


    /**
     * @param createName
     *            the createName to set
     */
    public void setCreateName ( String createName ) {
        this.createName = createName;
    }


    /**
     * @return the createType
     */
    public String getCreateType () {
        return this.createType;
    }


    /**
     * @param createType
     *            the createType to set
     */
    public void setCreateType ( String createType ) {
        this.createType = createType;
    }


    public void init ( ComponentSystemEvent ev ) {
        getFilesModel();
    }


    public TreeNode getFilesModel () {
        if ( this.filesModel == null ) {
            this.filesModel = makeFilesModel();
        }
        return this.filesModel;
    }


    public String getTitle () {
        getFilesModel();
        try {
            return GuiMessages.format("resourceLibrary.edit.title", this.libraryName, this.structureContext.getSelectedDisplayName()); //$NON-NLS-1$
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return this.libraryName;
        }
    }


    public String deleteFile ( String path ) {
        try {
            this.ssp.getService(ResourceLibraryService.class).removeFile(this.getSelectedLibraryId(), path);
            return DialogContext.closeDialog(true);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    /**
     * @return
     */
    private TreeNode makeFilesModel () {
        DefaultTreeNode root = new DefaultTreeNode();
        Set<ResourceLibraryFileInfo> inheritedPaths;
        try {
            StructuralObject selectedAnchor = this.structureContext.getSelectedObject();
            if ( selectedAnchor == null ) {
                return root;
            }

            if ( this.getSelectedLibraryId() == null && this.createName != null && this.createType != null ) {
                try {

                    ResourceLibrary found = this.ssp.getService(ResourceLibraryService.class)
                            .getByName(selectedAnchor, this.createName, this.createType);

                    if ( found == null ) {
                        ResourceLibrary foundParent = this.ssp.getService(ResourceLibraryService.class)
                                .getClosestByName(this.structureContext.getSelectedObject(), this.createName, this.createType);

                        if ( foundParent != null ) {
                            log.debug("Creating with parent " + foundParent.getId()); //$NON-NLS-1$
                        }

                        ResourceLibrary created = this.ssp.getService(ResourceLibraryService.class).create(
                            this.structureContext.getSelectedObject(),
                            foundParent != null ? foundParent.getId() : null,
                            this.createName,
                            this.createType,
                            false);
                        this.setSelectedLibraryId(created.getId());
                        this.setLibraryName(created.getName());
                        this.setLibraryType(created.getType());
                    }
                    else {
                        this.setSelectedLibraryId(found.getId());
                        this.setLibraryName(found.getName());
                        this.setLibraryType(found.getType());
                    }
                }
                catch (
                    ModelObjectReferentialIntegrityException |
                    ModelObjectValidationException |
                    ModelObjectConflictException e ) {
                    ExceptionHandler.handle(e);
                    return root;
                }
            }
            else if ( this.getSelectedLibraryId() == null && ( this.createName == null || this.createType == null ) ) {
                log.warn("Invalid creation parameters"); //$NON-NLS-1$
                return root;
            }
            else if ( this.getSelectedLibraryId() != null ) {
                ResourceLibrary byId = this.ssp.getService(ResourceLibraryService.class).getById(this.getSelectedLibraryId());
                if ( byId == null ) {
                    return root;
                }
                setLibraryName(byId.getName());
                setLibraryType(byId.getType());
            }

            this.allPaths = new ArrayList<>();
            List<ResourceLibraryFileInfo> paths = this.ssp.getService(ResourceLibraryService.class).getFiles(this.getSelectedLibraryId());
            inheritedPaths = this.ssp.getService(ResourceLibraryService.class).getInheritedFiles(this.getSelectedLibraryId());
            this.allPaths.addAll(inheritedPaths);
            this.allPaths.removeAll(paths);
            this.allPaths.addAll(paths);

            List<String> pathNames = new ArrayList<>();
            for ( ResourceLibraryFileInfo fi : this.allPaths ) {
                pathNames.add(fi.getPath());
            }
            this.allPathNames = pathNames;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return root;
        }
        makeFileTree(root, this.allPaths, inheritedPaths);
        return root;
    }


    /**
     * @param type
     */
    private void setLibraryType ( String type ) {
        this.libraryType = type;
    }


    /**
     * @return the libraryType
     */
    public String getLibraryType () {
        return this.libraryType;
    }


    /**
     * @param name
     */
    private void setLibraryName ( String name ) {
        this.libraryName = name;
    }


    /**
     * @return the libraryName
     */
    public String getLibraryName () {
        return this.libraryName;
    }


    /**
     * @return the paths
     */
    public List<String> getPaths () {
        if ( this.allPathNames == null ) {
            getFilesModel();
        }
        return this.allPathNames;
    }


    /**
     * 
     * @return dialog close on success
     */
    public String synchronize () {
        try {
            this.ssp.getService(ResourceLibraryService.class).synchronize(this.getSelectedLibraryId(), this.structureContext.getSelectedService());
            return DialogContext.closeDialog(true);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    /**
     * 
     */
    public void refresh () {
        this.filesModel = makeFilesModel();
    }


    /**
     * @param createFilename
     * @return the treenode for the given path
     */
    public FileNode getFileNode ( String createFilename ) {
        for ( TreeNode n : this.filesModel.getChildren() ) {
            if ( createFilename.equals(n.getData()) ) {
                return (FileNode) n;
            }
        }
        return null;
    }


    /**
     * @param parent
     * @param paths
     * @param inheritedPaths
     */
    static void makeFileTree ( DefaultTreeNode parent, List<ResourceLibraryFileInfo> paths, Set<ResourceLibraryFileInfo> inheritedPaths ) {
        for ( ResourceLibraryFileInfo path : paths ) {
            String fileName = makeFileName(path.getPath());
            new FileNode(path, fileName, parent);
        }

        for ( ResourceLibraryFileInfo inherited : inheritedPaths ) {
            if ( paths.contains(inherited) ) {
                continue;
            }
            String fileName = makeFileName(inherited.getPath());
            new FileNode(inherited, fileName, parent);
        }

    }


    /**
     * @param path
     * @return
     */
    private static String makeFileName ( String path ) {
        String fileName = path;
        int lastDirPos = fileName.lastIndexOf('/', fileName.length() - 2);
        if ( lastDirPos >= 0 ) {
            fileName = fileName.substring(lastDirPos + 1);
        }
        return fileName;
    }

}
