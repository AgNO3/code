/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.subject;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.service.tree.ui.FileRootSelectionBean;
import eu.agno3.runtime.util.format.ByteSizeFormatter;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "userQuotaBean" )
public class UserQuotaBean {

    @Inject
    private CurrentUserBean currentUser;

    @Inject
    private FileRootSelectionBean fileRoot;


    /**
     * 
     * @return whether the current user has a quota
     */
    public boolean haveCurrentUserQuota () {
        User user = this.currentUser.getCurrentUser();
        if ( user == null ) {
            return false;
        }
        return !user.getNoSubjectRoot() && user.getQuota() != null;
    }


    /**
     * @return the quota of the current user
     */
    public long getCurrentUserQuota () {
        User user = this.currentUser.getCurrentUser();
        if ( user == null ) {
            return 0;
        }
        return user.getQuota() != null ? user.getQuota() : 0;
    }


    /**
     * @return the space used by the current user
     */
    public long getCurrentUserUsedQuota () {
        VFSContainerEntity userRoot = this.fileRoot.getUserRoot();
        if ( userRoot != null ) {
            return Math.max(0, userRoot.getChildrenSize());
        }
        return 0;
    }


    /**
     * 
     * @return the used quota percent
     */
    public float getCurrentUserQuotaUsagePercent () {
        if ( !haveCurrentUserQuota() ) {
            return 0.0f;
        }

        return (float) ( ( ( (double) getCurrentUserUsedQuota() ) / getCurrentUserQuota() ) * 100.0f );
    }


    /**
     * 
     * @return a formatted percentage
     */
    public String getFormattedQuotaUsagePercent () {
        return FileshareMessages.format(
            FileshareMessages.USER_QUOTA_SHORT,
            this.getCurrentUserQuotaUsagePercent(),
            ByteSizeFormatter.formatByteSizeSI(this.getCurrentUserUsedQuota()),
            ByteSizeFormatter.formatByteSizeSI(this.getCurrentUserQuota()));
    }


    /**
     * 
     * @return the formatted quota detail message
     */
    public String getFormattedQuotaDetail () {
        return FileshareMessages.format(
            FileshareMessages.USER_QUOTA_DETAIL,
            this.getCurrentUserQuotaUsagePercent(),
            ByteSizeFormatter.formatByteSizeSI(this.getCurrentUserUsedQuota()),
            ByteSizeFormatter.formatByteSizeSI(this.getCurrentUserQuota()));
    }
}
