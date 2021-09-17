/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.subject;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.runtime.util.format.ByteSizeFormatter;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "groupQuotaBean" )
public class GroupQuotaBean {

    /**
     * 
     * @param g
     * @return whether the current user has a quota
     */
    public boolean haveGroupQuota ( Object g ) {
        if ( g == null || ( ! ( g instanceof VFSContainerEntity ) && ! ( ( (VFSContainerEntity) g ).getOwner() instanceof Group ) ) ) {
            return false;
        }
        return ( (VFSContainerEntity) g ).getOwner().getQuota() != null;
    }


    /**
     * @param g
     * @return the quota of the current user
     */
    public long getGroupQuota ( Object g ) {
        if ( ! ( g instanceof VFSContainerEntity ) && ! ( ( (VFSContainerEntity) g ).getOwner() instanceof Group ) ) {
            return 0;
        }
        return ( (VFSContainerEntity) g ).getOwner() != null && ( (VFSContainerEntity) g ).getOwner().getQuota() != null
                ? ( (VFSContainerEntity) g ).getOwner().getQuota() : 0;
    }


    /**
     * @param g
     * @return the space used by the current user
     */
    public long getGroupUsedQuota ( Object g ) {
        if ( ! ( g instanceof VFSContainerEntity ) && ! ( ( (VFSContainerEntity) g ).getOwner() instanceof Group ) ) {
            return 0;
        }
        return ( (VFSContainerEntity) g ).getChildrenSize();
    }


    /**
     * 
     * @param g
     * @return the used quota percent
     */
    public float getGroupQuotaUsagePercent ( Object g ) {
        if ( !haveGroupQuota(g) ) {
            return 0.0f;
        }

        return (float) ( ( ( (double) getGroupUsedQuota(g) ) / getGroupQuota(g) ) * 100.0f );
    }


    /**
     * 
     * @param g
     * @return a formatted percentage
     */
    public String getFormattedQuotaUsagePercent ( Object g ) {
        return FileshareMessages.format(
            FileshareMessages.QUOTA_SHORT,
            this.getGroupQuotaUsagePercent(g),
            ByteSizeFormatter.formatByteSizeSI(this.getGroupUsedQuota(g)),
            ByteSizeFormatter.formatByteSizeSI(this.getGroupQuota(g)));
    }


    /**
     * 
     * @param g
     * @return the formatted quota detail message
     */
    public String getFormattedQuotaDetail ( Object g ) {
        return FileshareMessages.format(
            FileshareMessages.QUOTA_DETAIL,
            getGroupQuotaUsagePercent(g),
            ByteSizeFormatter.formatByteSizeSI(getGroupUsedQuota(g)),
            ByteSizeFormatter.formatByteSizeSI(getGroupQuota(g)),
            g instanceof VFSContainerEntity && ( (VFSContainerEntity) g ).getOwner() instanceof Group
                    ? ( (Group) ( (VFSContainerEntity) g ).getOwner() ).getName() : StringUtils.EMPTY);
    }
}
