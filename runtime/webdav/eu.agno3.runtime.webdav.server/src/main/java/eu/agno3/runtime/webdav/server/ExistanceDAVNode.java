/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.06.2015 by mbechler
 */
package eu.agno3.runtime.webdav.server;


/**
 * @author mbechler
 * @param <T>
 *            identifier type
 *
 */
public interface ExistanceDAVNode <T> extends DAVTreeNode<T> {

    /**
     * 
     * @return whether the node exists
     */
    boolean exists ();
}
