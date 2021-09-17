/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.05.2016 by mbechler
 */
package eu.agno3.runtime.webdav.server.acl;


import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.security.AclResource;


/**
 * @author mbechler
 *
 */
public interface ReplacedAclResource extends AclResource {

    /**
     * @param acl
     * @throws DavException
     */
    void alterAcl ( ReplacedAclProperty acl ) throws DavException;

}
