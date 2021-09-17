/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.02.2016 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


/**
 * @author mbechler
 *
 */
public interface FilesharePassthroughGroupMutable extends FilesharePassthroughGroup {

    /**
     * @param allowSharing
     */
    void setAllowSharing ( Boolean allowSharing );


    /**
     * @param securityPolicy
     */
    void setSecurityPolicy ( String securityPolicy );


    /**
     * @param groupName
     */
    void setGroupName ( String groupName );

}
