/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.agent.impl;


import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.realms.config.KerberosConfigUtil;


/**
 * @author mbechler
 *
 */
public class BaseKerberosAuthenticatorConfigurator {

    private KerberosConfigUtil kerberosConfigUtil;


    @Reference
    protected synchronized void setKerberosConfigUtil ( KerberosConfigUtil rm ) {
        this.kerberosConfigUtil = rm;
    }


    protected synchronized void unsetKerberosConfigUtil ( KerberosConfigUtil rm ) {
        if ( this.kerberosConfigUtil == rm ) {
            this.kerberosConfigUtil = null;
        }
    }


    /**
     * @return the kerberosConfigUtil
     */
    public KerberosConfigUtil getKerberosConfigUtil () {
        return this.kerberosConfigUtil;
    }
}