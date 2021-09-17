/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.share;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.Date;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.TokenGrant;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.i18n.LocaleSettingsBean;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "grantInfoBean" )
public class GrantInfoBean {

    private static final Logger log = Logger.getLogger(GrantInfoBean.class);

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private FileSharesBean sharesBean;

    @Inject
    private SharePolicyBean sharePolicy;

    @Inject
    private LocaleSettingsBean localeSettings;


    /**
     * 
     * @return the minimum selectable expiry date
     */
    public Date getMinDate () {
        return DateTime.now().plusDays(1).toDate();
    }


    /**
     * 
     * @param g
     * @return the maximum selectable share end date
     */
    public Date getMaxDate ( Grant g ) {
        VFSEntity e = g.getEntity();
        Duration maximumExpirationDuration = this.sharePolicy.getMaximumExpirationDuration(g.getEntity());
        Duration maximumShareLifetime = this.sharePolicy.getMaximumShareLiftime(g.getEntity());

        DateTime maxEntityExpiry = null;
        if ( e.getExpires() != null && maximumExpirationDuration != null ) {
            maxEntityExpiry = e.getCreated().plus(maximumExpirationDuration).withTime(0, 0, 0, 0);
        }

        if ( maxEntityExpiry == null && maximumShareLifetime == null ) {
            return null;
        }
        else if ( maxEntityExpiry != null && maximumShareLifetime == null ) {
            return maxEntityExpiry.toDate();
        }
        else if ( maximumShareLifetime != null ) {
            DateTime maxShareExpiry = DateTime.now().plus(maximumShareLifetime).withTime(0, 0, 0, 0);

            if ( maxEntityExpiry != null && maxEntityExpiry.isBefore(maxShareExpiry) ) {
                return maxEntityExpiry.toDate();
            }

            return maxShareExpiry.toDate();
        }

        return null;
    }


    /**
     * 
     * @param g
     * @return whether the expiry can be removed
     */
    public boolean canRemoveExpiry ( Grant g ) {
        if ( g.getEntity() instanceof VFSContainerEntity ) {
            return this.sharePolicy.getMaximumShareLiftime(g.getEntity()) == null;
        }
        return this.sharePolicy.getMaximumExpirationDuration(g.getEntity()) == null && this.sharePolicy.getMaximumShareLiftime(g.getEntity()) == null;
    }


    /**
     * 
     * @param g
     * @return a selection wrapper for the grant
     */
    public GrantInfoWrapper wrapperFor ( Grant g ) {
        return new GrantInfoWrapper(this, g);
    }


    /**
     * @param grant
     * @param expiry
     */
    public void setExpiryDate ( Grant grant, DateTime expiry ) {
        DateTime realExpiry = null;
        if ( log.isDebugEnabled() ) {
            log.debug("Setting expiry date " + expiry); //$NON-NLS-1$
        }

        if ( expiry != null ) {
            realExpiry = expiry.toDateTime(this.localeSettings.getDateTimeZone()).withTimeAtStartOfDay();
        }

        try {
            this.fsp.getShareService().setExpiry(grant.getId(), realExpiry);
            this.sharesBean.refresh();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
    }


    /**
     * 
     * @param grant
     * @param permissions
     */
    public void setPermissions ( Grant grant, Set<GrantPermission> permissions ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Setting grant permissions " + permissions); //$NON-NLS-1$
        }
        try {
            this.fsp.getShareService().setPermissions(grant.getId(), permissions);
            this.sharesBean.refresh();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
    }


    /**
     * 
     * @param g
     * @param comment
     * @return null
     */
    public String updateComment ( TokenGrant g, String comment ) {
        if ( g == null ) {
            return null;
        }

        if ( ( comment == null && g.getComment() == null ) || ( comment != null && comment.equals(g.getComment()) ) ) {
            return null;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Updating comment " + comment); //$NON-NLS-1$
        }

        try {
            this.fsp.getShareService().updateComment(g.getId(), comment);
            this.sharesBean.refresh();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException ex ) {
            ExceptionHandler.handleException(ex);
        }

        return null;

    }


    /**
     * @param g
     * @param identifier
     * @return null
     */
    public String updateIdentifier ( TokenGrant g, String identifier ) {
        if ( g == null ) {
            return null;
        }

        if ( StringUtils.isBlank(identifier) || ( identifier == null && g.getComment() == null )
                || ( identifier != null && identifier.equals(g.getComment()) ) ) {
            return null;
        }

        try {
            this.fsp.getShareService().updateIdentifier(g.getId(), identifier);
            this.sharesBean.refresh();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException ex ) {
            ExceptionHandler.handleException(ex);
        }

        return null;
    }
}
