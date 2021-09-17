/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.file.view;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;
import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.file.FileDownloadController;
import eu.agno3.fileshare.webgui.service.file.URLFileSelectionBean;
import eu.agno3.fileshare.webgui.service.file.UserTokenBean;
import eu.agno3.fileshare.webgui.service.share.GrantSelectionBean;


/**
 * @author mbechler
 *
 */
@Named ( "fileViewBean" )
@ViewScoped
public class FileViewBean implements Serializable {

    private static final Logger log = Logger.getLogger(FileViewBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = -8186836751338416998L;
    private boolean closable;
    private boolean showBack;

    private boolean initialPreview;

    private Boolean preview;

    private boolean canOverrideLoaded;
    private boolean canOverride;

    private String downloadLink;
    private DateTime downloadLinkExpiration;

    @Inject
    private URLFileSelectionBean fileSelection;

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private FileDownloadController downloadController;

    @Inject
    private UserTokenBean userToken;

    @Inject
    private GrantSelectionBean grantSelection;


    /**
     * @return the closable
     */
    public boolean getClosable () {
        return this.closable;
    }


    /**
     * @param closable
     *            the closable to set
     */
    public void setClosable ( boolean closable ) {
        this.closable = closable;
    }


    /**
     * @return the showBack
     */
    public boolean getShowBack () {
        return this.showBack;
    }


    /**
     * @param showBack
     *            the showBack to set
     */
    public void setShowBack ( boolean showBack ) {
        this.showBack = showBack;
    }


    /**
     * @return the initialPreview
     */
    public boolean getInitialPreview () {
        return this.initialPreview;
    }


    /**
     * @param initialPreview
     *            the initialPreview to set
     */
    public void setInitialPreview ( boolean initialPreview ) {
        this.initialPreview = initialPreview;
    }


    /**
     * @return the preview
     */
    public boolean getPreview () {
        if ( this.preview == null ) {
            return this.initialPreview;
        }
        return this.preview;
    }


    /**
     * @param preview
     *            the preview to set
     */
    public void setPreview ( boolean preview ) {
        this.preview = preview;
    }


    /**
     * 
     * @return null
     */
    public String showPreview () {
        this.setPreview(true);
        return null;
    }


    /**
     * 
     * @return null
     */
    public String hidePreview () {
        this.setPreview(false);
        return null;
    }


    /**
     * @return the downloadLinKExpiration
     */
    public DateTime getDownloadLinkExpiration () {
        return this.downloadLinkExpiration;
    }


    /**
     * @return the downloadLink
     */
    public String getDownloadLink () {
        return this.downloadLink;
    }


    /**
     * @param downloadLink
     *            the downloadLink to set
     */
    public void setDownloadLink ( String downloadLink ) {}


    /**
     * 
     * @return null
     */
    public String makeDownloadLink () {
        VFSEntity selectedEntity = this.fileSelection.getSingleSelection();
        if ( ! ( selectedEntity instanceof VFSFileEntity ) || StringUtils.isEmpty(this.userToken.getToken()) ) {
            return null;
        }

        this.downloadLinkExpiration = DateTime.now().plus(this.fsp.getConfigurationProvider().getFrontendConfiguration().getIntentTimeout());
        this.downloadLink = this.downloadController
                .getDownloadLinkWithIntent((VFSFileEntity) selectedEntity, this.grantSelection.getSingleSelection(), this.userToken);
        return null;
    }


    /**
     * 
     * @return the parent entity
     */
    public boolean getCanOverwrite () {
        if ( !this.canOverrideLoaded ) {
            this.canOverrideLoaded = true;
            try {
                @Nullable
                VFSEntity singleSelection = this.fileSelection.getSingleSelection();
                if ( singleSelection != null ) {
                    this.fsp.getEntityService().checkWriteAccess(singleSelection.getEntityKey());
                }
                this.canOverride = true;
            }
            catch ( AccessDeniedException e ) {
                log.debug("Access is denied", e); //$NON-NLS-1$
                this.canOverride = false;
            }
            catch (
                UndeclaredThrowableException |
                FileshareException e ) {
                ExceptionHandler.handleException(e);
                this.canOverride = false;
            }
        }
        return this.canOverride;
    }
}
