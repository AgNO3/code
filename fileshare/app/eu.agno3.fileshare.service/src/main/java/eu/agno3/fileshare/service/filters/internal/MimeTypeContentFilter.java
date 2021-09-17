/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.filters.internal;


import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.mime.MediaType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.DisallowedMimeTypeException;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.service.api.internal.ContentFilter;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;


/**
 * @author mbechler
 *
 */
@Component ( service = ContentFilter.class )
public class MimeTypeContentFilter implements ContentFilter {

    private static final Logger log = Logger.getLogger(MimeTypeContentFilter.class);

    private DefaultServiceContext ctx;


    @Reference
    protected synchronized void setServiceContext ( DefaultServiceContext sctx ) {
        this.ctx = sctx;
    }


    protected synchronized void unsetServiceContext ( DefaultServiceContext sctx ) {
        if ( this.ctx == sctx ) {
            this.ctx = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.ContentFilter#getId()
     */
    @Override
    public String getId () {
        return "mime"; //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.ContentFilter#filterContent(eu.agno3.fileshare.model.VFSFileEntity,
     *      java.nio.channels.SeekableByteChannel)
     */
    @Override
    public void filterContent ( VFSFileEntity f, SeekableByteChannel data ) throws DisallowedMimeTypeException {
        try ( InputStream is = Channels.newInputStream(data);
              InputStream noMark = new NoMarkInputStream(is) ) {
            TikaConfig tika = new TikaConfig();
            Metadata meta = new Metadata();

            if ( this.ctx.getConfigurationProvider().getMimeTypePolicyConfiguration().isUseUserSuppliedTypes() ) {
                meta.set(TikaMetadataKeys.RESOURCE_NAME_KEY, f.getLocalName());
            }

            MediaType detect = tika.getDetector().detect(noMark, meta);
            String mimeType = detect.toString();

            if ( mimeType != null && !StringUtils.isEmpty(mimeType) ) {
                setMimeType(mimeType, f);
                return;
            }
        }
        catch (
            TikaException |
            IOException e ) {
            log.warn("Failed to detect file type", e); //$NON-NLS-1$
        }

        String fallbackMimeType = this.ctx.getConfigurationProvider().getMimeTypePolicyConfiguration().getFallbackMimeType();
        setMimeType(fallbackMimeType, f);
    }


    /**
     * @param mimeType
     * @param f
     * @throws DisallowedMimeTypeException
     */
    private void setMimeType ( String mimeType, VFSFileEntity f ) throws DisallowedMimeTypeException {
        this.ctx.getConfigurationProvider().getMimeTypePolicyConfiguration().checkMimeType(mimeType, false);
        f.setContentType(mimeType);
    }

}
