/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.service.config.ViewPolicyConfiguration;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.ua.UserAgentBean;
import eu.agno3.runtime.http.ua.UACapability;


/**
 * @author mbechler
 *
 */
@Named ( "fileViewPolicyBean" )
@ApplicationScoped
public class FileViewPolicyBean {

    @Inject
    private UserAgentBean ua;

    @Inject
    private FileshareServiceProvider fsp;

    private static final Set<String> HTML_TYPES = new HashSet<>(Arrays.asList(
        "text/html", //$NON-NLS-1$
        "application/xhtml+xml" //$NON-NLS-1$
    ));


    /**
     * @param f
     * @return whether this file should not be displayed sandboxed
     */
    public boolean shouldNotSandbox ( Object f ) {
        if ( ! ( f instanceof VFSFileEntity ) ) {
            return false;
        }
        String mimeType = getMimeType(f);
        return !StringUtils.isBlank(mimeType) && this.fsp.getConfigurationProvider().getViewPolicyConfig().getNoSandboxMimeTypes().contains(mimeType);
    }


    /**
     * 
     * @param f
     * @return the desired sandbox attributes
     */
    public String getSandboxAttributes ( VFSFileEntity f ) {
        return "allow-scripts"; //$NON-NLS-1$
    }


    /**
     * @param f
     * @return whether this is a HTML type
     */
    public boolean isHTML ( Object f ) {
        if ( ! ( f instanceof VFSFileEntity ) ) {
            return false;
        }
        String mimeType = getMimeType(f);
        return !StringUtils.isBlank(mimeType) && HTML_TYPES.contains(mimeType);
    }


    /**
     * 
     * @param f
     * @return the tooltip view the view link
     */
    public String getViewTitle ( Object f ) {

        if ( !this.isViewableContentType(getMimeType(f)) ) {
            return FileshareMessages.get(FileshareMessages.PREVIEW_TITLE_UNSUPPORTED_TYPE);
        }

        if ( this.exceedsMaxSize(f) ) {
            return FileshareMessages.get(FileshareMessages.PREVIEW_TITLE_SIZE_EXCEEDED);
        }

        if ( !this.canView(f) ) {
            return FileshareMessages.get(FileshareMessages.PREVIEW_TITLE_NO_SECURITY);
        }
        return FileshareMessages.get(FileshareMessages.PREVIEW_TILE);
    }


    /**
     * 
     * @param obj
     * @return the viewer view for the given file's type
     */
    public String getViewFor ( Object obj ) {

        if ( !canView(obj) ) {
            return "/view/noneView.xhtml"; //$NON-NLS-1$
        }

        String mimeType = getMimeType(obj);

        if ( "application/pdf".equals(mimeType) ) { //$NON-NLS-1$
            return "/view/pdfView.xhtml"; //$NON-NLS-1$
        }

        if ( !StringUtils.isBlank(mimeType) && mimeType.startsWith("image/") ) { //$NON-NLS-1$
            return "/view/imageView.xhtml"; //$NON-NLS-1$
        }

        return "/view/rawView.xhtml"; //$NON-NLS-1$
    }


    /**
     * 
     * @param mimeType
     * @return whether a safe custom viewer exists
     */
    public boolean haveSafeCustomViewer ( String mimeType ) {

        if ( "application/pdf".equals(mimeType) ) { //$NON-NLS-1$
            return true;
        }

        ViewPolicyConfiguration viewPolicyConfig = this.fsp.getConfigurationProvider().getViewPolicyConfig();
        if ( !StringUtils.isBlank(mimeType) && mimeType.startsWith("image/") //$NON-NLS-1$
                && ( viewPolicyConfig.isViewable(mimeType) || viewPolicyConfig.isSafe(mimeType) ) ) {
            return true;
        }

        return false;
    }


    /**
     * @param obj
     * @return whether the file can be viewed inline
     */
    public boolean canView ( Object obj ) {
        String mimeType = getMimeType(obj);

        if ( exceedsMaxSize(obj) ) {
            return false;
        }

        if ( isSafeViewableContentType(mimeType) || haveSafeCustomViewer(mimeType) ) {
            return true;
        }

        if ( ( !shouldNotSandbox(obj) && !this.ua.hasCapability(UACapability.FRAME_SANDBOXING) ) || !this.ua.hasCapability(UACapability.CSP10) ) {
            return false;
        }

        return isViewableContentType(mimeType);
    }


    /**
     * @param obj
     * @return
     */
    private boolean exceedsMaxSize ( Object obj ) {
        if ( obj instanceof VFSFileEntity && this.fsp.getConfigurationProvider().getViewPolicyConfig().getMaxPreviewFileSize() >= 0 ) {
            VFSFileEntity e = (VFSFileEntity) obj;
            if ( e.getFileSize() > this.fsp.getConfigurationProvider().getViewPolicyConfig().getMaxPreviewFileSize() ) {
                return true;
            }
        }
        return false;
    }


    /**
     * @param obj
     * @return
     */
    private static String getMimeType ( Object obj ) {
        if ( ! ( obj instanceof VFSFileEntity ) ) {
            return null;
        }

        VFSFileEntity f = (VFSFileEntity) obj;
        return f.getContentType();
    }


    /**
     * 
     * @param obj
     * @return whether viewing is generally allowed for this file's type
     */
    public boolean isViewableFile ( Object obj ) {
        String mimeType = getMimeType(obj);
        return isViewableContentType(mimeType);
    }


    /**
     * 
     * @param obj
     * @return whether viewing is generally allowed for this file's type
     */
    public boolean isSafeViewableFile ( Object obj ) {
        return isSafeViewableContentType(getMimeType(obj));
    }


    /**
     * 
     * @param mimeType
     * @return whether the given mime type can be displayed inline
     */
    public boolean isViewableContentType ( String mimeType ) {
        if ( StringUtils.isBlank(mimeType) ) {
            return false;
        }
        return isSafeViewableContentType(mimeType) || this.fsp.getConfigurationProvider().getViewPolicyConfig().isViewable(mimeType);
    }


    /**
     * 
     * @param mimeType
     * @return whether mimeType is a safe type to display
     */
    public boolean isSafeViewableContentType ( String mimeType ) {
        if ( StringUtils.isBlank(mimeType) ) {
            return false;
        }
        return this.fsp.getConfigurationProvider().getViewPolicyConfig().isSafe(mimeType);
    }
}
