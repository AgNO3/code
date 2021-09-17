/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.app;


import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import eu.agno3.orchestrator.agent.connector.AgentServerConnector;
import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.server.connector.ServerConnectorState;
import eu.agno3.runtime.update.ApplicationStateProvider;
import eu.agno3.runtime.update.LicensingService;
import eu.agno3.runtime.update.PlatformState;


/**
 * @author mbechler
 *
 */
@Component ( service = ApplicationStateProvider.class )
public class AgentApplicationStateProvider implements ApplicationStateProvider {

    private static final Logger log = Logger.getLogger(AgentApplicationStateProvider.class);

    private JobCoordinator jobCoordinator;
    private AgentServerConnector serverConnector;
    private ServiceManager serviceManager;
    private LicensingService licenseService;


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void bindJobCoordinator ( JobCoordinator jc ) {
        this.jobCoordinator = jc;
    }


    protected synchronized void unbindJobCoordinator ( JobCoordinator jc ) {
        if ( this.jobCoordinator == jc ) {
            this.jobCoordinator = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void bindServerConnector ( AgentServerConnector asc ) {
        this.serverConnector = asc;
    }


    protected synchronized void unbindServerConnector ( AgentServerConnector asc ) {
        if ( this.serverConnector == asc ) {
            this.serverConnector = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void bindServiceManager ( ServiceManager sm ) {
        this.serviceManager = sm;
    }


    protected synchronized void unbindServiceManager ( ServiceManager sm ) {
        if ( this.serviceManager == sm ) {
            this.serviceManager = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void bindLicenseService ( LicensingService lm ) {
        this.licenseService = lm;
    }


    protected synchronized void unbindLicenseService ( LicensingService lm ) {
        if ( this.licenseService == lm ) {
            this.licenseService = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.ApplicationStateProvider#getStatus()
     */
    @Override
    public PlatformState getStatus () {

        if ( this.jobCoordinator == null ) {
            log.debug("Job coordinator unavailable"); //$NON-NLS-1$
            return PlatformState.FAILED;
        }

        if ( this.serverConnector == null ) {
            log.debug("Server connector unavailable"); //$NON-NLS-1$
            return PlatformState.FAILED;
        }

        if ( this.serviceManager == null ) {
            log.debug("Service manager unavailable"); //$NON-NLS-1$
            return PlatformState.FAILED;
        }

        if ( this.licenseService == null ) {
            log.debug("License service unavailable"); //$NON-NLS-1$
            return PlatformState.FAILED;
        }

        if ( !this.licenseService.isLicenseValid("urn:agno3:1.0:hostconfig") || this.licenseService.warnExpiration() ) { //$NON-NLS-1$
            return PlatformState.WARNING;
        }

        ServerConnectorState connState = this.serverConnector.getState();
        if ( connState == ServerConnectorState.ERROR || connState == ServerConnectorState.DISCONNECTED ) {
            return PlatformState.WARNING;
        }
        return PlatformState.STARTED;
    }

}
