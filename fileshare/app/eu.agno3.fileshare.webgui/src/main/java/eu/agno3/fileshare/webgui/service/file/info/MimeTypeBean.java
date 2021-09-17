/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.info;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.file.FilePermissionBean;
import eu.agno3.fileshare.webgui.subject.CurrentUserBean;
import eu.agno3.runtime.jsf.i18n.FacesMessageBundle;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "mimeTypeBean" )
public class MimeTypeBean extends FacesMessageBundle {

    private static final String FILE_TYPE_MSG_BASE = "eu.agno3.fileshare.webgui.service.file.info.types"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(MimeTypeBean.class);

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private FilePermissionBean perm;

    @Inject
    private CurrentUserBean currentUser;

    private static Map<String, String> FILE_TYPE_IMAGES = new HashMap<>();

    static {

        FILE_TYPE_IMAGES.put("application/zip", //$NON-NLS-1$
            "zip.png"); //$NON-NLS-1$

        FILE_TYPE_IMAGES.put("application/x-gzip", //$NON-NLS-1$
            "tgz.png"); //$NON-NLS-1$

        FILE_TYPE_IMAGES.put("application/x-msdownload", //$NON-NLS-1$
            "exe.png"); //$NON-NLS-1$

        FILE_TYPE_IMAGES.put("application/x-dosexec", //$NON-NLS-1$
            "exe.png"); //$NON-NLS-1$

        FILE_TYPE_IMAGES.put("audio/x-aac", //$NON-NLS-1$
            "aac.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("audio/wav", //$NON-NLS-1$
            "wav.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("audio/mp3", //$NON-NLS-1$
            "mp3.png"); //$NON-NLS-1$

        FILE_TYPE_IMAGES.put("video/avi", //$NON-NLS-1$
            "avi.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("video/mpeg", //$NON-NLS-1$
            "mpg.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("video/mp4", //$NON-NLS-1$
            "mpg.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("video/x-m4v", //$NON-NLS-1$
            "mpg.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("video/x-flv", //$NON-NLS-1$
            "flv.png"); //$NON-NLS-1$

        FILE_TYPE_IMAGES.put("text/html", //$NON-NLS-1$
            "html.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("text/plain", //$NON-NLS-1$
            "txt.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("text/xml", //$NON-NLS-1$
            "xml.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("application/xml", //$NON-NLS-1$
            "xml.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("text/x-c", //$NON-NLS-1$
            "c.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("text/css", //$NON-NLS-1$
            "css.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("text/calendar", //$NON-NLS-1$
            "ics.png"); //$NON-NLS-1$

        FILE_TYPE_IMAGES.put("image/bmp", //$NON-NLS-1$
            "bmp.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("image/gif", //$NON-NLS-1$
            "gif.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("image/jpeg", //$NON-NLS-1$
            "jpg.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("image/png", //$NON-NLS-1$
            "png.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("image/vnd.dxf", //$NON-NLS-1$
            "dxf.png"); //$NON-NLS-1$

        FILE_TYPE_IMAGES.put("image/tiff", //$NON-NLS-1$
            "tiff.png"); //$NON-NLS-1$

        FILE_TYPE_IMAGES.put("application/vnd.ms-word", //$NON-NLS-1$
            "doc.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", //$NON-NLS-1$
            "doc.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("application/vnd.openxmlformats-officedocument.wordprocessingml.template", //$NON-NLS-1$
            "doc.png"); //$NON-NLS-1$

        FILE_TYPE_IMAGES.put("application/vnd.ms-excel", //$NON-NLS-1$
            "xls.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", //$NON-NLS-1$
            "xls.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("application/vnd.openxmlformats-officedocument.spreadsheetml.template", //$NON-NLS-1$
            "xls.png"); //$NON-NLS-1$

        FILE_TYPE_IMAGES.put("application/vnd.ms-powerpoint", //$NON-NLS-1$
            "ppt.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", //$NON-NLS-1$
            "ppt.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("application/vnd.openxmlformats-officedocument.presentationml.template", //$NON-NLS-1$
            "ppt.png"); //$NON-NLS-1$

        FILE_TYPE_IMAGES.put("application/vnd.oasis.opendocument.spreadsheet", //$NON-NLS-1$
            "ods.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("application/vnd.oasis.opendocument.spreadsheet-template", //$NON-NLS-1$
            "ods.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("application/vnd.oasis.opendocument.text", //$NON-NLS-1$
            "odt.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("application/vnd.oasis.opendocument.text-template", //$NON-NLS-1$
            "odt.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("application/vnd.oasis.opendocument.presentation", //$NON-NLS-1$
            "odp.png"); //$NON-NLS-1$
        FILE_TYPE_IMAGES.put("application/vnd.oasis.opendocument.presentation-template", //$NON-NLS-1$
            "odp.png"); //$NON-NLS-1$

    }


    /**
     * 
     * @param mimeType
     * @return an icon resource name for the given mime type
     */
    public static String getIconFor ( String mimeType ) {

        String found = FILE_TYPE_IMAGES.get(mimeType);

        if ( found != null ) {
            return found;
        }

        return "_blank.png"; //$NON-NLS-1$
    }


    /**
     * 
     * @param mimeType
     * @return the display type for the given mime type
     */
    public static String getDisplayType ( String mimeType ) {
        if ( StringUtils.isBlank(mimeType) ) {
            return StringUtils.EMPTY;
        }
        try {
            String type = FacesMessageBundle.get(FILE_TYPE_MSG_BASE, mimeType, MimeTypeBean.class.getClassLoader());

            return type;
        }
        catch ( MissingResourceException e ) {
            log.trace("Missing resource", e); //$NON-NLS-1$
            return mimeType;
        }
        catch ( Exception e ) {
            log.warn("Exception translating mime type", e); //$NON-NLS-1$
            return mimeType;
        }
    }


    /**
     * @param e
     * @return whether mime type changes are allowed for this entity
     */
    public boolean isMimeTypeChangeAllowed ( VFSFileEntity e ) {
        return this.currentUser.hasPermission("entity:changeMimeType") //$NON-NLS-1$
                && this.fsp.getConfigurationProvider().getMimeTypePolicyConfiguration().isAllowMimeTypeChanges() && this.perm.isOwner(e);
    }


    /**
     * 
     * @return the allowed mime types
     */
    public List<String> getAllowedMimeTypes () {
        return new ArrayList<>(this.fsp.getConfigurationProvider().getMimeTypePolicyConfiguration().getAllowedMimeTypes());
    }

}
