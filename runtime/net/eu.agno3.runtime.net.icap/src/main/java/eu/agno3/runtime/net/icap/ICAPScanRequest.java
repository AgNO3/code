/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.06.2015 by mbechler
 */
package eu.agno3.runtime.net.icap;


import java.io.InputStream;


/**
 * @author mbechler
 *
 */
public class ICAPScanRequest {

    private InputStream data;
    private long size = -1;
    private String fileName;
    private String contentType;
    private String clientIp;
    private String clientUser;


    /**
     * @param is
     * 
     */
    public ICAPScanRequest ( InputStream is ) {
        this.data = is;
    }


    /**
     * @param is
     * @param size
     * 
     */
    public ICAPScanRequest ( InputStream is, long size ) {
        this.data = is;
        this.size = size;
    }


    /**
     * @return the data to scan
     */
    public InputStream getData () {
        return this.data;
    }


    /**
     * @return the data size if known
     */
    public long getSize () {
        return this.size;
    }


    /**
     * @return the filename, if known
     */
    public String getFileName () {
        return this.fileName;
    }


    /**
     * @param size
     *            the size to set
     */
    public void setSize ( long size ) {
        this.size = size;
    }


    /**
     * @param fileName
     *            the fileName to set
     */
    public void setFileName ( String fileName ) {
        this.fileName = fileName;
    }


    /**
     * @param contentType
     *            the contentType to set
     */
    public void setContentType ( String contentType ) {
        this.contentType = contentType;
    }


    /**
     * @param clientIp
     *            the clientIp to set
     */
    public void setClientIp ( String clientIp ) {
        this.clientIp = clientIp;
    }


    /**
     * @param clientUser
     *            the clientUser to set
     */
    public void setClientUser ( String clientUser ) {
        this.clientUser = clientUser;
    }


    /**
     * @return the data content type
     */
    public String getContentType () {
        return this.contentType;
    }


    /**
     * @return the client ip
     */
    public String getClientIp () {
        return this.clientIp;
    }


    /**
     * @return the client user
     */
    public String getClientUser () {
        return this.clientUser;
    }

}
