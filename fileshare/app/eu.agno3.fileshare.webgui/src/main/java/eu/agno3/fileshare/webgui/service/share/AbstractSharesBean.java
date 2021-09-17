/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.share;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.primefaces.event.SelectEvent;

import eu.agno3.fileshare.exceptions.PolicyNotFoundException;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.ShareProperties;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.service.config.PolicyConfiguration;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.file.URLFileSelectionBean;
import eu.agno3.fileshare.webgui.service.file.picker.OwnedEntityPicker;
import eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode;
import eu.agno3.fileshare.webgui.service.tree.EntityTreeNode;
import eu.agno3.runtime.jsf.types.uri.URIUtil;
import eu.agno3.runtime.security.password.PasswordGenerationException;


/**
 * @author mbechler
 *
 */
public abstract class AbstractSharesBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2144499014130428549L;

    private ShareProperties shareProperties = new ShareProperties();

    @Inject
    private SharePolicyBean sharePolicy;

    @Inject
    private URLFileSelectionBean fileSelection;

    @Inject
    private OwnedEntityPicker picker;

    @Inject
    private FileshareServiceProvider fsp;

    private transient MimeMessage messagePreview;

    private boolean inPreview;

    private boolean passwordProtected;

    private Integer estimatedEntropy;


    /**
     * 
     */
    public AbstractSharesBean () {
        super();
    }


    /**
     * @return the fileSelection
     */
    protected URLFileSelectionBean getFileSelection () {
        return this.fileSelection;
    }


    /**
     * 
     * @return the applied policy
     */
    public PolicyConfiguration getPolicy () {
        try {
            VFSEntity singleSelection = getSelectedEntity();
            if ( singleSelection == null ) {
                return null;
            }
            return this.fsp.getConfigurationProvider().getSecurityPolicyConfiguration().getPolicy(singleSelection.getSecurityLabel().getLabel());
        }
        catch ( PolicyNotFoundException e ) {
            ExceptionHandler.handleException(e);
            return null;
        }
    }


    /**
     * @return
     */
    protected VFSEntity getSelectedEntity () {
        VFSEntity singleSelection = this.getFileSelection().getSingleSelection();
        if ( singleSelection == null && this.picker.getSelection() instanceof EntityTreeNode ) {
            singleSelection = ( (EntityTreeNode) this.picker.getSelection() ).getAttachedObject();
        }
        return singleSelection;
    }


    /**
     * 
     * @return whether a token password is required
     */
    public boolean isRequirePassword () {
        PolicyConfiguration policy = this.getPolicy();
        if ( policy == null || policy.isRequireTokenPassword() ) {
            return true;
        }
        return false;
    }


    /**
     * 
     * @return whether a token password is required
     */
    public boolean noUserPassword () {
        PolicyConfiguration policy = this.getPolicy();
        if ( policy == null || policy.isNoUserTokenPasswords() ) {
            return true;
        }
        return false;
    }


    /**
     * 
     * @return null
     */
    public String enablePasswordProtection () {
        this.setPasswordProtected(true);
        try {
            VFSEntity singleSelection = getSelectedEntity();
            this.setPassword(
                this.fsp.getShareService()
                        .generateSharePassword(singleSelection.getSecurityLabel(), FacesContext.getCurrentInstance().getViewRoot().getLocale()));
        }
        catch (
            PasswordGenerationException |
            PolicyNotFoundException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
            this.setPasswordProtected(false);
            return null;
        }
        this.onPasswordChange(null);
        return null;
    }


    /**
     * 
     */
    @PostConstruct
    public void init () {
        VFSEntity singleSelection = this.fileSelection.getSingleSelection();
        DateTime defaultExpiration = getDefaultExpiration(singleSelection);
        this.shareProperties.setExpiry(defaultExpiration);
        this.shareProperties.setPermissions(EnumSet.of(GrantPermission.READ));
        if ( !this.fsp.getConfigurationProvider().getFrontendConfiguration().isWebFrontendURIReliable() ) {
            this.shareProperties.setOverrideBaseURI(URIUtil.getCurrentBaseUri());
        }
    }


    /**
     * @param singleSelection
     */
    private DateTime getDefaultExpiration ( VFSEntity singleSelection ) {
        Duration defaultExpirationDuration = this.sharePolicy.getDefaultShareLifetime(singleSelection);

        if ( defaultExpirationDuration != null ) {
            DateTime defaultExpiry = DateTime.now().plus(defaultExpirationDuration).withTime(0, 0, 0, 0);
            DateTime maxExpiry = getMaximumLifetimeDateTime();

            if ( maxExpiry != null && defaultExpiry.isAfter(maxExpiry) ) {
                return maxExpiry;
            }

            return defaultExpiry;
        }
        return getMaximumLifetimeDateTime();
    }


    /**
     * @return the maximum share lifetime
     */
    public DateTime getMaximumLifetimeDateTime () {
        VFSEntity entity = this.fileSelection.getSingleSelection();
        if ( entity == null && this.picker.getSelection() instanceof EntityTreeNode ) {
            entity = ( (EntityTreeNode) this.picker.getSelection() ).getAttachedObject();
        }
        Duration maximumShareLifetime = this.sharePolicy.getMaximumShareLiftime(entity);
        Duration maximumExpiration = this.sharePolicy.getMaximumExpirationDuration(entity);

        DateTime maxExpiry = null;
        if ( maximumExpiration != null && entity != null ) {
            maxExpiry = entity.getCreated().plus(maximumExpiration).withTime(0, 0, 0, 0);
        }

        if ( maximumShareLifetime == null && maximumExpiration != null ) {
            return maxExpiry;
        }
        if ( maximumShareLifetime != null ) {
            DateTime maxLifetimeEnd = DateTime.now().plus(maximumShareLifetime).withTime(0, 0, 0, 0);
            if ( maxExpiry != null && maxLifetimeEnd.isAfter(maxExpiry) ) {
                return maxExpiry;
            }
            return maxLifetimeEnd;
        }

        return null;
    }


    /**
     * 
     * @return the maximum expiration date (java)
     */
    public Date getMaximumExpirationDate () {
        DateTime maximumExpirationDateTime = getMaximumLifetimeDateTime();
        if ( maximumExpirationDateTime != null ) {
            return maximumExpirationDateTime.toDate();
        }
        return null;
    }


    /**
     * @return the messagePreview
     */
    public MimeMessage getMessagePreview () {
        return this.messagePreview;
    }


    /**
     * @param messagePreview
     *            the messagePreview to set
     */
    public void setMessagePreview ( MimeMessage messagePreview ) {
        this.messagePreview = messagePreview;
    }


    /**
     * @return the inPreview
     */
    public boolean getInPreview () {
        return this.inPreview;
    }


    /**
     * @param inPreview
     *            the inPreview to set
     */
    public void setInPreview ( boolean inPreview ) {
        this.inPreview = inPreview;
    }


    /**
     * @return the shareProperties
     */
    public ShareProperties getShareProperties () {
        return this.shareProperties;
    }


    /**
     * @param shareProperties
     *            the shareProperties to set
     */
    public void setShareProperties ( ShareProperties shareProperties ) {
        this.shareProperties = shareProperties;
    }


    /**
     * @return the writeable
     */
    public Set<GrantPermission> getPermissions () {
        return this.shareProperties.getPermissions();
    }


    /**
     * @param permissions
     *            the permissions to set
     */
    public void setPermissions ( Set<GrantPermission> permissions ) {
        this.shareProperties.setPermissions(permissions);
    }


    /**
     * 
     * @return the numeric permissions
     */
    public int getPermissionsInt () {
        return GrantPermission.toInt(this.shareProperties.getPermissions());
    }


    /**
     * 
     * @param perms
     */
    public void setPermissionsInt ( int perms ) {
        this.shareProperties.setPermissions(GrantPermission.fromInt(perms));
    }


    /**
     * @return whether the share is writeable
     */
    public boolean isWriteable () {
        Set<GrantPermission> permissions = this.shareProperties.getPermissions();
        return permissions.contains(GrantPermission.EDIT) || permissions.contains(GrantPermission.EDIT_SELF)
                || permissions.contains(GrantPermission.UPLOAD);
    }


    /**
     * @return the expires
     */
    public DateTime getExpires () {
        DateTime expires = this.shareProperties.getExpiry();
        if ( expires != null || this.fileSelection.getSingleSelectionId() != null ) {
            return expires;
        }

        BrowseTreeNode selection = this.picker.getSelection();
        if ( ! ( selection instanceof EntityTreeNode ) ) {
            return null;
        }

        return getDefaultExpiration( ( (EntityTreeNode) selection ).getAttachedObject());
    }


    /**
     * @param expires
     *            the expires to set
     */
    public void setExpires ( DateTime expires ) {
        this.shareProperties.setExpiry(expires);
    }


    /**
     * @return the passwordProtected
     */
    public boolean isPasswordProtected () {
        return this.passwordProtected;
    }


    /**
     * @param passwordProtected
     *            the passwordProtected to set
     */
    public void setPasswordProtected ( boolean passwordProtected ) {
        this.passwordProtected = passwordProtected;
    }


    /**
     * @return the password
     */
    public String getPassword () {
        return this.getShareProperties().getPassword();
    }


    /**
     * @param password
     *            the password to set
     */
    public void setPassword ( String password ) {
        this.getShareProperties().setPassword(password);
    }


    /**
     * 
     * @param ev
     */
    public void onExpirationSet ( SelectEvent ev ) {
        // ignore
    }


    /**
     * @return the estimatedChangeEntropy
     */
    public Integer getEstimatedEntropy () {
        return this.estimatedEntropy;
    }


    /**
     * @param estimatedChangeEntropy
     *            the estimatedChangeEntropy to set
     */
    public void setEstimatedEntropy ( Integer estimatedChangeEntropy ) {
        this.estimatedEntropy = estimatedChangeEntropy;
    }


    /**
     * 
     * @param ev
     */
    public void onPasswordChange ( ActionEvent ev ) {

        String pw = this.getPassword();
        if ( !StringUtils.isBlank(pw) ) {
            this.estimatedEntropy = this.fsp.getPasswordPolicy().estimateEntropy(pw);
        }
        else {
            this.estimatedEntropy = 0;
        }
    }


    /**
     * Remove expiration date
     */
    public void unsetExpiration () {
        this.shareProperties.setExpiry(null);
    }


    /**
     * 
     */
    public void reset () {
        this.shareProperties = new ShareProperties();
        this.estimatedEntropy = null;
        this.messagePreview = null;
        this.inPreview = false;
        init();
    }

}