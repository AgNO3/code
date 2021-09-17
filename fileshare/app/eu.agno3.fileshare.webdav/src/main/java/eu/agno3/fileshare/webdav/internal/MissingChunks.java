/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 11, 2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.util.List;

import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.agno3.fileshare.model.query.ChunkInfo;


/**
 * @author mbechler
 *
 */
public class MissingChunks extends DefaultDavProperty<List<ChunkInfo>> implements DavProperty<List<ChunkInfo>> {

    /**
     * @param missingChunks
     */
    public MissingChunks ( List<ChunkInfo> missingChunks ) {
        super(Constants.MISSING_CHUNKS, missingChunks, true);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.property.AbstractDavProperty#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml ( Document doc ) {
        Element elem = getName().toXml(doc);
        String ns = Constants.AGNO3_NS.getURI();
        String prefix = elem.lookupPrefix(ns);
        for ( ChunkInfo ci : this.getValue() ) {
            Element chunkElem = doc.createElementNS(ns, "c"); //$NON-NLS-1$
            chunkElem.setPrefix(prefix);
            chunkElem.setAttribute("i", String.valueOf(ci.getIndex())); //$NON-NLS-1$
            chunkElem.setAttribute("o", String.valueOf(ci.getStartOffset())); //$NON-NLS-1$
            chunkElem.setAttribute("l", String.valueOf(ci.getLength())); //$NON-NLS-1$
            elem.appendChild(chunkElem);
        }
        return elem;
    }
}
