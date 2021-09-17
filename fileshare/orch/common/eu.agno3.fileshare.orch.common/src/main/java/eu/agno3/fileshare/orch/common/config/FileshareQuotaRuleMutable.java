/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareQuotaRule.class )
public interface FileshareQuotaRuleMutable extends FileshareQuotaRule {

    /**
     * 
     * @param quota
     */
    void setQuota ( Long quota );


    /**
     * 
     * @param matchRole
     */
    void setMatchRole ( String matchRole );

}
