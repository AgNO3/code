/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 6, 2016 by mbechler
 */
package eu.agno3.fileshare.service;


/**
 * @author mbechler
 *
 */
public enum ArchiveType {

    /**
     * ZIP
     */
    ZIP("application/zip", //$NON-NLS-1$
            "zip"), //$NON-NLS-1$

    /**
     * Uncompressed (level 0) ZIP
     */
    ZIPU("application/zip", //$NON-NLS-1$
            "zip"), //$NON-NLS-1$

    /**
     * TAR GZ
     */
    TGZ("application/x-gzip", //$NON-NLS-1$
            "tar.gz"), //$NON-NLS-1$

    /**
     * TBZ2
     */
    TBZ2("application/x-bzip2", //$NON-NLS-1$
            "tar.bz2"), //$NON-NLS-1$

    /**
     * Uncompressed tar
     */
    TAR("application/x-tar", //$NON-NLS-1$
            "tar"), //$NON-NLS-1$

    ;

    private String mimeType;
    private String extension;


    /**
     * 
     */
    private ArchiveType ( String mimeType, String extension ) {
        this.mimeType = mimeType;
        this.extension = extension;
    }


    /**
     * @return the extension
     */
    public String getExtension () {
        return this.extension;
    }


    /**
     * @return the mimeType
     */
    public String getMimeType () {
        return this.mimeType;
    }
}
