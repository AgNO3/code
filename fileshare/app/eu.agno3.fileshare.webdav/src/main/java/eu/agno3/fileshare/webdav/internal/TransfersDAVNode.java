/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.04.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


/**
 * @author mbechler
 *
 */
public class TransfersDAVNode extends AbstractVirtualDAVNode {

    /**
     * 
     */
    public static final String TRANSFERS_PATH = "/.transfers"; //$NON-NLS-1$


    /**
     * @param layout
     */
    public TransfersDAVNode ( DAVLayout layout ) {
        super(".transfers", null, null, layout); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getAbsolutePath()
     */
    @Override
    public String getAbsolutePath () {
        return TRANSFERS_PATH;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getInode()
     */
    @Override
    protected byte[] getInode () {
        return null;
    }

}
