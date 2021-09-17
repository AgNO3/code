/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.directory;


import java.io.Serializable;
import java.util.MissingResourceException;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.service.ArchiveType;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;


/**
 * @author mbechler
 *
 */
@Named ( "archiveTypeBean" )
@ViewScoped
public class ArchiveTypeBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7780520767491596401L;

    private ArchiveType archiveType = ArchiveType.ZIP; // $NON-NLS-1$


    /**
     * 
     * @param val
     * @return label for the archive type
     */
    public String translateArchiveType ( Object val ) {
        if ( ! ( val instanceof ArchiveType ) ) {
            return StringUtils.EMPTY;
        }
        ArchiveType at = (ArchiveType) val;
        try {
            return FileshareMessages.get("archiveType." + at.name()); //$NON-NLS-1$
        }
        catch ( MissingResourceException e ) {
            return at.name();
        }
    }


    /**
     * 
     * @return the available archive types
     */
    public ArchiveType[] getArchiveTypes () {
        return ArchiveType.values();
    }


    /**
     * @return the archiveType
     */
    public ArchiveType getArchiveType () {
        return this.archiveType;
    }


    /**
     * @param archiveType
     *            the archiveType to set
     */
    public void setArchiveType ( ArchiveType archiveType ) {
        this.archiveType = archiveType;
    }

}
