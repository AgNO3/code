/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.auth.cas;


import org.jasig.cas.authentication.Credential;


/**
 * @author mbechler
 *
 */
public class ShiroCredential implements Credential {

    /**
     * {@inheritDoc}
     *
     * @see org.jasig.cas.authentication.Credential#getId()
     */
    @Override
    public String getId () {
        return "Shiro"; //$NON-NLS-1$
    }

}
