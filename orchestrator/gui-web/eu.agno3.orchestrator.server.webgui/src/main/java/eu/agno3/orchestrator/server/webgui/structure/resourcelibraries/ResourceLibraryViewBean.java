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

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrary;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryFileInfo;
import eu.agno3.orchestrator.config.model.realm.service.ResourceLibraryService;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 *
 */
@Named ( "resourceLibraryViewBean" )
@ViewScoped
public class ResourceLibraryViewBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 551797536061137890L;

    private TreeNode filesModel;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private StructureViewContextBean structureContext;

    private List<ResourceLibraryFileInfo> allPaths;
    private List<String> allPathNames;

    private UUID libraryId;
    private String libraryName;
    private String libraryType;


    public void init ( ComponentSystemEvent ev ) {
        getFilesModel();
    }


    public TreeNode getFilesModel () {
        if ( this.filesModel == null ) {
            this.filesModel = makeFilesModel();
        }
        return this.filesModel;
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

            ResourceLibrary found = null;
            if ( getLibraryName() != null && getLibraryType() != null ) {
                found = this.ssp.getService(ResourceLibraryService.class).getClosestByName(selectedAnchor, getLibraryName(), getLibraryType());
            }
            else if ( this.getLibraryId() != null ) {
                found = this.ssp.getService(ResourceLibraryService.class).getById(this.getLibraryId());
            }

            if ( found == null ) {
                return root;
            }
            this.setLibraryId(found.getId());
            this.setLibraryName(found.getName());
            this.setLibraryType(found.getType());

            this.allPaths = new ArrayList<>();
            List<ResourceLibraryFileInfo> paths = this.ssp.getService(ResourceLibraryService.class).getFiles(found.getId());
            inheritedPaths = this.ssp.getService(ResourceLibraryService.class).getInheritedFiles(found.getId());
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

        ResourceLibraryEditorBean.makeFileTree(root, this.allPaths, inheritedPaths);
        return root;
    }


    /**
     * @param type
     */
    public void setLibraryType ( String type ) {
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
    public void setLibraryName ( String name ) {
        this.libraryName = name;
    }


    /**
     * @return the libraryName
     */
    public String getLibraryName () {
        return this.libraryName;
    }


    /**
     * @return the libraryId
     */
    public UUID getLibraryId () {
        return this.libraryId;
    }


    /**
     * @param libraryId
     *            the libraryId to set
     */
    public void setLibraryId ( UUID libraryId ) {
        this.libraryId = libraryId;
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
     */
    public void refresh () {
        this.filesModel = makeFilesModel();
    }

}
