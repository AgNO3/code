/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.09.2014 by mbechler
 */
package eu.agno3.runtime.update;


import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.util.Set;


/**
 * @author mbechler
 *
 */
public interface UpdateConfiguration {

    /**
     * Configuration PID
     */
    static String PID = "updates"; //$NON-NLS-1$


    /**
     * 
     * @return the repositories to use for obtaining updates
     */
    Set<URI> getRepositories ();


    /**
     * 
     * @return the P2 target area to manage
     * @throws URISyntaxException
     */
    URI getTargetArea () throws URISyntaxException;


    /**
     * @return the target P2 profile
     */
    String getTargetProfile ();


    /**
     * 
     * @return the owner to set on the installed files
     */
    UserPrincipal getOwner ();


    /**
     * 
     * @return the group to set on the installed files
     */
    GroupPrincipal getGroup ();

}
