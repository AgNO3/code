/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.policy.internal;


import javax.servlet.ServletRequest;

import org.apache.shiro.subject.Subject;

import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.tokens.AccessToken;


/**
 * @author mbechler
 *
 */
public interface PolicyEvaluationContext {

    /**
     * 
     * @return the authentication subject
     */
    Subject getAuthSubject ();


    /**
     * 
     * @return the authenticated user
     */
    User getCurrentUser ();


    /**
     * 
     * @return the current grant
     */
    AccessToken getCurrentToken ();


    /**
     * 
     * @return the current servlet request
     */
    ServletRequest getServletRequest ();


    /**
     * @return the active grant
     */
    Grant getGrant ();


    /**
     * @return whether the current user is the owner of the entity
     */
    boolean isOwner ();


    /**
     * @return the entity
     */
    VFSEntity getEntity ();

}
