/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.06.2015 by mbechler
 */
package eu.agno3.fileshare.model.query;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.service.UploadState;


/**
 * @author mbechler
 *
 */
public class ChunkedUploadInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 842579239387442434L;
    private VFSEntity target;
    private String reference;
    private String localName;
    private String contentType;

    private Long totalSize;

    private List<ChunkInfo> completeChunks = new ArrayList<>();
    private List<ChunkInfo> missingChunks = new ArrayList<>();
    private long completeSize;
    private long chunkSize;
    private UploadState state;
    private DateTime lastSeen;
    private boolean externalSource;


    /**
     * 
     * @return the target directory/or file if replacing
     */
    public VFSEntity getTarget () {
        return this.target;
    }


    /**
     * @param target
     *            the target to set
     */
    public void setTarget ( VFSEntity target ) {
        this.target = target;
    }


    /**
     * 
     * @return the reference string
     */
    public String getReference () {
        return this.reference;
    }


    /**
     * @param reference
     *            the reference to set
     */
    public void setReference ( String reference ) {
        this.reference = reference;
    }


    /**
     * @return the localName
     */
    public String getLocalName () {
        return this.localName;
    }


    /**
     * @param localName
     *            the localName to set
     */
    public void setLocalName ( String localName ) {
        this.localName = localName;
    }


    /**
     * @param contentType
     *            the contentType to set
     */
    public void setContentType ( String contentType ) {
        this.contentType = contentType;
    }


    /**
     * @return the contentType
     */
    public String getContentType () {
        return this.contentType;
    }


    /**
     * @return the totalSize
     */
    public Long getTotalSize () {
        return this.totalSize;
    }


    /**
     * @param totalSize
     *            the totalSize to set
     */
    public void setTotalSize ( Long totalSize ) {
        this.totalSize = totalSize;
    }


    /**
     * @return the missingChunks
     */
    public List<ChunkInfo> getMissingChunks () {
        return this.missingChunks;
    }


    /**
     * @param missingChunks
     *            the missingChunks to set
     */
    public void setMissingChunks ( List<ChunkInfo> missingChunks ) {
        this.missingChunks = missingChunks;
    }


    /**
     * @return the completeChunks
     */
    public List<ChunkInfo> getCompleteChunks () {
        return this.completeChunks;
    }


    /**
     * @param completeChunks
     *            the completeChunks to set
     */
    public void setCompleteChunks ( List<ChunkInfo> completeChunks ) {
        this.completeChunks = completeChunks;
    }


    /**
     * @return the completeSize
     */
    public long getCompleteSize () {
        return this.completeSize;
    }


    /**
     * @param haveSize
     */
    public void setCompleteSize ( long haveSize ) {
        this.completeSize = haveSize;
    }


    /**
     * @return the chunk size
     */
    public long getChunkSize () {
        return this.chunkSize;
    }


    /**
     * @param chunkSize
     *            the chunkSize to set
     */
    public void setChunkSize ( long chunkSize ) {
        this.chunkSize = chunkSize;
    }


    /**
     * @param state
     */
    public void setState ( UploadState state ) {
        this.state = state;
    }


    /**
     * @return the state
     */
    public UploadState getState () {
        return this.state;
    }


    /**
     * @param lastModified
     */
    public void setLastSeen ( DateTime lastModified ) {
        this.lastSeen = lastModified;
    }


    /**
     * @return the lastSeen
     */
    public DateTime getLastSeen () {
        return this.lastSeen;
    }


    /**
     * @return the externalSource
     */
    public boolean getExternalSource () {
        return this.externalSource;
    }


    /**
     * @param externalSource
     */
    public void setExternalSource ( boolean externalSource ) {
        this.externalSource = externalSource;
    }

}
