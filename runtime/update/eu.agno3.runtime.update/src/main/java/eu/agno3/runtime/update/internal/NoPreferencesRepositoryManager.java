/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2015 by mbechler
 */
package eu.agno3.runtime.update.internal;


import org.eclipse.equinox.internal.p2.metadata.repository.MetadataRepositoryManager;
import org.eclipse.equinox.p2.core.IProvisioningAgent;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "restriction" )
public class NoPreferencesRepositoryManager extends MetadataRepositoryManager {

    /**
     * @param agent
     */

    public NoPreferencesRepositoryManager ( IProvisioningAgent agent ) {
        super(new NoLocationAgentWrapper(agent));
    }

}
