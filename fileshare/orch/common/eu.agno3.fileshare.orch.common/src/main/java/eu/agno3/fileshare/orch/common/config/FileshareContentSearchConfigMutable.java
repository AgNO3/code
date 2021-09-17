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
@MapAs ( FileshareContentSearchConfig.class )
public interface FileshareContentSearchConfigMutable extends FileshareContentSearchConfig {

    /**
     * 
     * @param searchPageSize
     */
    void setSearchPageSize ( Integer searchPageSize );


    /**
     * 
     * @param searchAllowPaging
     */
    void setSearchAllowPaging ( Boolean searchAllowPaging );


    /**
     * 
     * @param searchDisabled
     */
    void setSearchDisabled ( Boolean searchDisabled );
}
