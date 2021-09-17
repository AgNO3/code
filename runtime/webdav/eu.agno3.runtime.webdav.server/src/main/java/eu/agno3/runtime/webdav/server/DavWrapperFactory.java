/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jul 15, 2016 by mbechler
 */
package eu.agno3.runtime.webdav.server;


import eu.agno3.runtime.webdav.server.impl.DavResourceWrapper;


/**
 * @author mbechler
 * @param <T>
 *
 */
public interface DavWrapperFactory <T> {

    /**
     * @param context
     * @param child
     * @return a wrapper for the node
     */
    DavResourceWrapper<T> getWrapperFor ( DavResourceWrapper<T> context, DAVTreeNode<T> child );

}
