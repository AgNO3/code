/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.directory;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.ContainerEntity;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.label.LabelUtils;
import eu.agno3.fileshare.webgui.service.tree.ui.FileTreeBean;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "directoryCreateController" )
public class DirectoryCreateController {

    private static final Logger log = Logger.getLogger(DirectoryCreateController.class);

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private FileTreeBean fileTree;

    @Inject
    private LabelUtils labelUtil;


    /**
     * 
     * @param parent
     * @param createDir
     * @return outcome
     */
    public String create ( VFSContainerEntity parent, DirectoryCreateBean createDir ) {

        if ( createDir.getInGroupRoot() ) {
            return createInGroup(createDir);
        }

        return createRealDirectory(parent, createDir);
    }


    /**
     * @param parent
     * @param createDir
     * @return
     */
    private String createRealDirectory ( VFSContainerEntity parent, DirectoryCreateBean createDir ) {
        if ( parent == null || StringUtils.isBlank(createDir.getDirectoryName()) ) {
            return null;
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Creating directory %s in %s", createDir.getDirectoryName(), parent)); //$NON-NLS-1$
        }

        VFSContainerEntity e = makeDirectoryEntry(createDir);

        return createDirectoryInternal(parent, e);
    }


    /**
     * @param parent
     * @param e
     * @return
     */
    private String createDirectoryInternal ( VFSContainerEntity parent, VFSContainerEntity e ) {
        try {
            VFSContainerEntity created = this.fsp.getDirectoryService().create(parent.getEntityKey(), e);

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Created label %s, parent label %s", created.getSecurityLabel(), parent.getSecurityLabel())); //$NON-NLS-1$
            };
            return DialogContext.closeDialog(new CreateDirectoryReturn(parent.getEntityKey(), this.labelUtil.getWarningIfHigherThanContainer(
                created,
                parent)));
        }
        catch (
            FileshareException |
            UndeclaredThrowableException ex ) {
            ExceptionHandler.handleException(ex);
        }

        return null;
    }


    /**
     * @param o
     */
    public void returnFromDialog ( SelectEvent o ) {

        if ( o.getObject() == null || ! ( o.getObject() instanceof CreateDirectoryReturn ) ) {
            return;
        }

        CreateDirectoryReturn retVal = (CreateDirectoryReturn) o.getObject();

        try {

            if ( retVal.getMsg() != null ) {
                FacesContext.getCurrentInstance().addMessage(null, retVal.getMsg());
            }
            VFSContainerEntity parent = this.fsp.getDirectoryService().getById(retVal.getParentId());
            this.fileTree.ensureExpanded(parent, this.fsp.getEntityService().getParents(parent.getEntityKey()), null);
            this.fileTree.refresh();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
            return;
        }

    }


    /**
     * @param createDir
     * @return
     */
    private String createInGroup ( DirectoryCreateBean createDir ) {

        if ( createDir.getGroup() == null ) {
            return null;
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Creating directory %s in group %s", createDir.getDirectoryName(), createDir.getGroup())); //$NON-NLS-1$
        }

        UUID groupId = createDir.getGroup().getId();
        try {
            VFSContainerEntity groupRoot = this.fsp.getBrowseService().getOrCreateGroupRoot(groupId);
            VFSContainerEntity e = makeDirectoryEntry(createDir);
            this.fileTree.ensureExpanded(e, this.fsp.getEntityService().getParents(e.getEntityKey()), null);
            return createDirectoryInternal(groupRoot, e);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException ex ) {
            ExceptionHandler.handleException(ex);
            return null;
        }

    }


    /**
     * @param createDir
     * @param parent
     * @return
     */
    private static VFSContainerEntity makeDirectoryEntry ( DirectoryCreateBean createDir ) {
        ContainerEntity e = new ContainerEntity();
        e.setLocalName(createDir.getDirectoryName());
        return e;
    }

    /**
     * @author mbechler
     *
     */
    public static class CreateDirectoryReturn implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = -5534612901614246405L;

        private EntityKey parentId;

        private FacesMessage msg;


        /**
         * @param parentId
         * @param msg
         * 
         */
        public CreateDirectoryReturn ( EntityKey parentId, FacesMessage msg ) {
            this.parentId = parentId;
            this.msg = msg;
        }


        /**
         * @return the parentId
         */
        public EntityKey getParentId () {
            return this.parentId;
        }


        /**
         * @return the msg
         */
        public FacesMessage getMsg () {
            return this.msg;
        }
    }
}
