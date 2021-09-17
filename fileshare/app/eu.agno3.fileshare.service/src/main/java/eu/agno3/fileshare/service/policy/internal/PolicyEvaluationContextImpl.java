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
public class PolicyEvaluationContextImpl implements PolicyEvaluationContext {

    private User currentUser;
    private AccessToken tokenAuthValue;
    private Subject authSubject;
    private ServletRequest servletRequest;
    private Grant grant;
    private boolean owner;
    private VFSEntity entity;


    /**
     * @param entity
     * @param currentUser
     * @param tokenAuthValue
     * @param grant
     * @param authSubject
     * @param servletRequest
     * @param owner
     */
    public PolicyEvaluationContextImpl ( VFSEntity entity, User currentUser, AccessToken tokenAuthValue, Grant grant, Subject authSubject,
            ServletRequest servletRequest, boolean owner ) {
        this.entity = entity;
        this.currentUser = currentUser;
        this.tokenAuthValue = tokenAuthValue;
        this.grant = grant;
        this.authSubject = authSubject;
        this.servletRequest = servletRequest;
        this.owner = owner;
    }


    /**
     * @return the owner
     */
    @Override
    public boolean isOwner () {
        return this.owner;
    }


    /**
     * @return the entity
     */
    @Override
    public VFSEntity getEntity () {
        return this.entity;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.policy.internal.PolicyEvaluationContext#getAuthSubject()
     */
    @Override
    public Subject getAuthSubject () {
        return this.authSubject;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.policy.internal.PolicyEvaluationContext#getCurrentUser()
     */
    @Override
    public User getCurrentUser () {
        return this.currentUser;
    }


    /**
     * @return the grant
     */
    @Override
    public Grant getGrant () {
        return this.grant;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.policy.internal.PolicyEvaluationContext#getCurrentToken()
     */
    @Override
    public AccessToken getCurrentToken () {
        return this.tokenAuthValue;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.policy.internal.PolicyEvaluationContext#getServletRequest()
     */
    @Override
    public ServletRequest getServletRequest () {
        return this.servletRequest;
    }

}
