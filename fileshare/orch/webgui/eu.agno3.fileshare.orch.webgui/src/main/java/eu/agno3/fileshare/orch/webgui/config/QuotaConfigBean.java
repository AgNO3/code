/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.config;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.fileshare.orch.common.config.FileshareQuotaRule;
import eu.agno3.fileshare.orch.common.config.FileshareQuotaRuleImpl;


/**
 * @author mbechler
 *
 */
@Named ( "fs_quotaConfigBean" )
@ApplicationScoped
public class QuotaConfigBean {

    /**
     * 
     * @return an instance
     */
    public FileshareQuotaRule makeQuotaRule () {
        return new FileshareQuotaRuleImpl();
    }
}
