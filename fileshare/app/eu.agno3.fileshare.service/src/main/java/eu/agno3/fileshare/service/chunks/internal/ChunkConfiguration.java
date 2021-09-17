/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 8, 2017 by mbechler
 */
package eu.agno3.fileshare.service.chunks.internal;


import org.joda.time.Duration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = ChunkConfiguration.class, configurationPid = "chunks" )
public class ChunkConfiguration {

    private int cleanupInterval;
    private Duration uploadTimeout;
    private long defaultChunkSize;
    private long maximumChunkSize;
    private int optimalChunkCount;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        parseConfig(ctx);
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx);
    }


    /**
     * @param ctx
     */
    protected void parseConfig ( ComponentContext ctx ) {
        this.cleanupInterval = ConfigUtil.parseInt(ctx.getProperties(), "cleanupInterval", ChunkUploadUtil.DEFAULT_CLEANUP_INTERVAL); //$NON-NLS-1$
        this.uploadTimeout = ConfigUtil
                .parseDuration(ctx.getProperties(), "uploadTimeout", Duration.millis(ChunkUploadUtil.DEFAULT_UPLOAD_TIMEOUT_MS)); //$NON-NLS-1$

        this.defaultChunkSize = ConfigUtil.parseByteSize(ctx.getProperties(), "defaultChunkSize", ChunkUploadUtil.DEFAULT_CHUNK_SIZE); //$NON-NLS-1$
        this.maximumChunkSize = ConfigUtil.parseByteSize(ctx.getProperties(), "maxChunkSize", ChunkUploadUtil.DEFAULT_MAXIMUM_CHUNK_SIZE); //$NON-NLS-1$
        this.optimalChunkCount = ConfigUtil.parseInt(ctx.getProperties(), "optimalChunkCount", ChunkUploadUtil.DEFAULT_OPTIMAL_CHUNK_COUNT); //$NON-NLS-1$
    }


    /**
     * @return the cleanupInterval
     */
    public int getCleanupInterval () {
        return this.cleanupInterval;
    }


    /**
     * @return the uploadTimeout
     */
    public Duration getUploadTimeout () {
        return this.uploadTimeout;
    }


    /**
     * @return the defaultChunkSize
     */
    public long getDefaultChunkSize () {
        return this.defaultChunkSize;
    }


    /**
     * @return the maximumChunkSize
     */
    public long getMaximumChunkSize () {
        return this.maximumChunkSize;
    }


    /**
     * @return the optimalChunkCount
     */
    public int getOptimalChunkCount () {
        return this.optimalChunkCount;
    }
}
