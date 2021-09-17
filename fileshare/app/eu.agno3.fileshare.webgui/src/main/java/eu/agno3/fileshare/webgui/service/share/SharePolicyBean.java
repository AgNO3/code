/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.share;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.joda.time.Duration;

import eu.agno3.fileshare.exceptions.PolicyNotFoundException;
import eu.agno3.fileshare.model.GrantType;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.service.config.PolicyConfiguration;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.tree.EntityTreeNode;
import eu.agno3.fileshare.webgui.subject.CurrentUserBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "sharePolicyBean" )
public class SharePolicyBean {

    private static final Logger log = Logger.getLogger(SharePolicyBean.class);

    @Inject
    private CurrentUserBean currentUser;

    @Inject
    private FileshareServiceProvider fsp;


    private boolean hasPolicyFor ( VFSEntity entity ) {
        if ( entity == null || entity.getSecurityLabel() == null ) {
            return false;
        }
        return this.fsp.getConfigurationProvider().getSecurityPolicyConfiguration().hasPolicy(entity.getSecurityLabel().getLabel());
    }


    private PolicyConfiguration getPolicy ( VFSEntity entity ) throws PolicyNotFoundException {
        return this.fsp.getConfigurationProvider().getSecurityPolicyConfiguration().getPolicy(entity.getSecurityLabel().getLabel());
    }


    /**
     * @param entity
     * @param type
     * @return
     */
    private boolean checkSharePolicy ( VFSEntity entity, GrantType type ) {
        if ( entity == null ) {
            return false;
        }

        if ( !entity.isStaticSharable() ) {
            return false;
        }

        if ( !this.hasPolicyFor(entity) ) {
            return false;
        }
        try {
            return getPolicy(entity).getAllowedShareTypes().contains(type);
        }
        catch ( PolicyNotFoundException e ) {
            log.debug("Policy not found for entity", e); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * 
     * @param entity
     * @return the maximum expiration duration
     */
    public Duration getMaximumExpirationDuration ( VFSEntity entity ) {

        if ( entity == null ) {
            return null;
        }

        if ( !this.hasPolicyFor(entity) ) {
            return Duration.standardSeconds(0);
        }

        try {
            return getPolicy(entity).getMaximumExpirationDuration();
        }
        catch ( PolicyNotFoundException e ) {
            log.debug("Policy not found for entity", e); //$NON-NLS-1$
            return Duration.standardSeconds(0);
        }
    }


    /**
     * 
     * @param entity
     * @return the maximum share lifetime
     */
    public Duration getMaximumShareLiftime ( VFSEntity entity ) {

        if ( entity == null ) {
            return null;
        }

        if ( !this.hasPolicyFor(entity) ) {
            return Duration.standardSeconds(0);
        }

        try {
            return getPolicy(entity).getMaximumShareLifetime();
        }
        catch ( PolicyNotFoundException e ) {
            log.debug("Policy not found for entity", e); //$NON-NLS-1$
            return Duration.standardSeconds(0);
        }
    }


    /**
     * 
     * @param entity
     * @return the default expiration duration
     */
    public Duration getDefaultExpirationDuration ( VFSEntity entity ) {

        if ( entity == null ) {
            return null;
        }

        if ( !this.hasPolicyFor(entity) ) {
            return null;
        }

        try {
            return getPolicy(entity).getDefaultExpirationDuration();
        }
        catch ( PolicyNotFoundException e ) {
            log.debug("Policy not found for entity", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * 
     * @param entity
     * @return the default share lifetime
     */
    public Duration getDefaultShareLifetime ( VFSEntity entity ) {

        if ( entity == null ) {
            return null;
        }

        if ( !this.hasPolicyFor(entity) ) {
            return null;
        }

        try {
            return getPolicy(entity).getDefaultShareLifetime();
        }
        catch ( PolicyNotFoundException e ) {
            log.debug("Policy not found for entity", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param obj
     * @return whether any sharing mechanism is allowed
     */
    public boolean mayShareAny ( Object obj ) {
        Object o = obj;
        if ( o instanceof EntityTreeNode ) {
            o = ( (EntityTreeNode) o ).getAttachedObject();
        }

        if ( ! ( o instanceof VFSEntity ) ) {
            return false;
        }

        VFSEntity entity = (VFSEntity) o;

        if ( !entity.isStaticSharable() ) {
            return false;
        }

        return mayShareToSubjects(entity) || mayShareByLink(entity) || mayShareByMail(entity);
    }


    /**
     * 
     * @param entity
     * @return whether sharing to subjects is generally allowed
     */
    public boolean mayShareToSubjects ( VFSEntity entity ) {
        if ( !this.currentUser.hasPermission("share:subjects") ) { //$NON-NLS-1$
            return false;
        }

        return checkSharePolicy(entity, GrantType.SUBJECT);
    }


    /**
     * 
     * @param entity
     * @return whether sharing by mail is generally allowed
     */
    public boolean mayShareByMail ( VFSEntity entity ) {
        if ( !this.currentUser.hasPermission("share:mail") ) { //$NON-NLS-1$
            return false;
        }

        if ( this.fsp.getConfigurationProvider().getNotificationConfiguration().isMailingDisabled() ) {
            return false;
        }

        return checkSharePolicy(entity, GrantType.MAIL);
    }


    /**
     * 
     * @param entity
     * @return whether sharing by link is generally allowed
     */
    public boolean mayShareByLink ( VFSEntity entity ) {
        if ( !this.currentUser.hasPermission("share:link") ) { //$NON-NLS-1$
            return false;
        }

        return checkSharePolicy(entity, GrantType.LINK);
    }

}
