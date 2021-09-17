/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2015 by mbechler
 */
package eu.agno3.orchestrator.server.app;


import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.runtime.http.service.ActiveHandler;
import eu.agno3.runtime.update.ApplicationStateProvider;
import eu.agno3.runtime.update.LicensingService;
import eu.agno3.runtime.update.PlatformState;
import eu.agno3.runtime.ws.server.WebserviceEndpointInfo;


/**
 * @author mbechler
 *
 */
@Component ( service = ApplicationStateProvider.class )
public class ServerApplicationStateProvider implements ApplicationStateProvider {

    private static final Logger log = Logger.getLogger(ServerApplicationStateProvider.class);

    private DefaultServerServiceContext serverContext;
    private LicensingService licenseService;
    private WebserviceEndpointInfo webServiceInfo;
    private ActiveHandler manageWebapp;
    private ActiveHandler authWebapp;


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void bindServerContext ( DefaultServerServiceContext ssc ) {
        this.serverContext = ssc;
    }


    protected synchronized void unbindServerContext ( DefaultServerServiceContext ssc ) {
        if ( this.serverContext == ssc ) {
            this.serverContext = null;
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


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void bindWebServiceEndpoint ( WebserviceEndpointInfo wsei ) {
        this.webServiceInfo = wsei;
    }


    protected synchronized void unbindWebServiceEndpoint ( WebserviceEndpointInfo wsei ) {
        if ( this.webServiceInfo == wsei ) {
            this.webServiceInfo = null;
        }
    }


    @Reference (
        cardinality = ReferenceCardinality.OPTIONAL,
        policyOption = ReferencePolicyOption.GREEDY,
        target = "(handler.id=eu.agno3.orchestrator.server.webgui)" )
    protected synchronized void bindManagementWebapp ( ActiveHandler h ) {
        this.manageWebapp = h;
    }


    protected synchronized void unbindManagementWebapp ( ActiveHandler h ) {
        if ( this.manageWebapp == h ) {
            this.manageWebapp = null;
        }
    }


    @Reference (
        cardinality = ReferenceCardinality.OPTIONAL,
        policyOption = ReferencePolicyOption.GREEDY,
        target = "(handler.id=eu.agno3.orchestrator.server.auth.webapp)" )
    protected synchronized void bindAuthWebapp ( ActiveHandler h ) {
        this.authWebapp = h;
    }


    protected synchronized void unbindAuthWebapp ( ActiveHandler h ) {
        if ( this.authWebapp == h ) {
            this.authWebapp = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.ApplicationStateProvider#getStatus()
     */
    @Override
    public PlatformState getStatus () {
        if ( this.serverContext == null ) {
            log.debug("No serverContext"); //$NON-NLS-1$
            return PlatformState.FAILED;
        }

        if ( this.licenseService == null ) {
            log.debug("No licenseService"); //$NON-NLS-1$
            return PlatformState.FAILED;
        }

        if ( this.webServiceInfo == null ) {
            log.debug("No webServiceInfo"); //$NON-NLS-1$
            return PlatformState.FAILED;
        }

        if ( this.manageWebapp == null ) {
            log.debug("No manageWebapp"); //$NON-NLS-1$
            return PlatformState.FAILED;
        }

        if ( this.authWebapp == null ) {
            log.debug("No authWebapp"); //$NON-NLS-1$
            return PlatformState.FAILED;
        }

        if ( !this.licenseService.isLicenseValid("urn:agno3:1.0:orchestrator") || this.licenseService.warnExpiration() ) { //$NON-NLS-1$
            return PlatformState.WARNING;
        }

        return PlatformState.STARTED;
    }
}
