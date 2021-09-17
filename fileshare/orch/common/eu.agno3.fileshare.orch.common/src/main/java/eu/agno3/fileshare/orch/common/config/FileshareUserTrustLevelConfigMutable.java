/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Set;


/**
 * @author mbechler
 *
 */
public interface FileshareUserTrustLevelConfigMutable extends FileshareUserTrustLevelConfig {

    /**
     * 
     * @param linkTrustLevel
     */
    void setLinkTrustLevel ( String linkTrustLevel );


    /**
     * 
     * @param mailTrustLevel
     */
    void setMailTrustLevel ( String mailTrustLevel );


    /**
     * 
     * @param groupTrustLevel
     */
    void setGroupTrustLevel ( String groupTrustLevel );


    /**
     * 
     * @param trustLevels
     */
    void setTrustLevels ( Set<FileshareUserTrustLevel> trustLevels );

}
