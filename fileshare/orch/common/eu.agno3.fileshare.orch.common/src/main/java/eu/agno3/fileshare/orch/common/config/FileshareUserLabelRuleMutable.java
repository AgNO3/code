/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareUserLabelRule.class )
public interface FileshareUserLabelRuleMutable extends FileshareUserLabelRule {

    /**
     * 
     * @param role
     */
    void setMatchRole ( String role );


    /**
     * 
     * @param label
     */
    void setAssignLabel ( String label );
}
