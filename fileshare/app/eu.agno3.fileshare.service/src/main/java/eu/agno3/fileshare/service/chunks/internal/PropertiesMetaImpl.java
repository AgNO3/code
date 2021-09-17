/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2017 by mbechler
 */
package eu.agno3.fileshare.service.chunks.internal;


import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.service.ChunkUploadMeta;


/**
 * @author mbechler
 *
 */
public class PropertiesMetaImpl implements ChunkUploadMeta {

    private final EntityKey target;
    private final String localName;
    private final String contentType;
    private final String reference;
    private final boolean replacing;
    private final boolean targetIsFile;
    private final boolean externalSource;
    private final Long chunkSize;
    private final Long totalSize;


    /**
     * @param targetId
     * @param props
     */
    public PropertiesMetaImpl ( EntityKey targetId, Properties props ) {
        this.target = targetId;
        this.targetIsFile = ChunkUploadUtil.FILE_TYPE.equals(props.getProperty("target-type")); //$NON-NLS-1$
        this.replacing = Boolean.parseBoolean(props.getProperty("replacing")); //$NON-NLS-1$
        this.externalSource = Boolean.parseBoolean(props.getProperty("external-source")); //$NON-NLS-1$
        this.reference = props.getProperty("reference"); //$NON-NLS-1$
        this.localName = props.getProperty("local-name"); //$NON-NLS-1$
        this.contentType = props.getProperty("content-type"); //$NON-NLS-1$

        String csSpec = props.getProperty("chunk-size"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(csSpec) ) {
            this.chunkSize = Long.parseLong(csSpec);
        }
        else {
            this.chunkSize = null;
        }

        String tsSpec = props.getProperty("total-size"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(tsSpec) ) {
            this.totalSize = Long.parseLong(tsSpec);
        }
        else {
            this.totalSize = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkUploadMeta#isExternalSource()
     */
    @Override
    public boolean isExternalSource () {
        return this.externalSource;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkUploadMeta#getTarget()
     */

    @Override
    public EntityKey getTarget () {
        return this.target;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkUploadMeta#isTargetFile()
     */

    @Override
    public boolean isTargetFile () {
        return this.targetIsFile;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkUploadMeta#isReplacing()
     */

    @Override
    public boolean isReplacing () {
        return this.replacing;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkUploadMeta#getLocalName()
     */

    @Override
    public String getLocalName () {
        return this.localName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkUploadMeta#getContentType()
     */

    @Override
    public String getContentType () {
        return this.contentType;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkUploadMeta#getReference()
     */
    @Override
    public String getReference () {
        return this.reference;
    }


    /**
     * @return the totalSize
     */
    @Override
    public Long getTotalSize () {
        return this.totalSize;
    }


    /**
     * @return the chunkSize
     */
    @Override
    public Long getChunkSize () {
        return this.chunkSize;
    }

}
