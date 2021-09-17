package eu.agno3.fileshare.app;


import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.config.FrontendConfiguration;
import eu.agno3.fileshare.webdav.FileshareWebDAVServlet;
import eu.agno3.runtime.http.service.ActiveHandler;
import eu.agno3.runtime.update.ApplicationStateProvider;
import eu.agno3.runtime.update.LicensingService;
import eu.agno3.runtime.update.PlatformState;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "restriction" )
@Component ( service = ApplicationStateProvider.class )
public class FileshareApplicationStateProvider implements ApplicationStateProvider {

    private static final Logger log = Logger.getLogger(FileshareApplicationStateProvider.class);

    private DefaultServiceContext serviceContext;
    private FileshareWebDAVServlet davServlet;
    private LicensingService licenseService;
    private FrontendConfiguration frontendConfig;
    private ActiveHandler webapp;


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void bindServiceContext ( DefaultServiceContext dsc ) {
        this.serviceContext = dsc;
    }


    protected synchronized void unbindServiceContext ( DefaultServiceContext dsc ) {
        if ( this.serviceContext == dsc ) {
            this.serviceContext = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void bindWebDAVServlet ( FileshareWebDAVServlet fws ) {
        this.davServlet = fws;
    }


    protected synchronized void unbindWebDAVServlet ( FileshareWebDAVServlet fws ) {
        if ( this.davServlet == fws ) {
            this.davServlet = null;
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
    protected synchronized void bindFrontendConfig ( FrontendConfiguration fc ) {
        this.frontendConfig = fc;
    }


    protected synchronized void unbindFrontendConfig ( FrontendConfiguration fc ) {
        if ( this.frontendConfig == fc ) {
            this.frontendConfig = null;
        }
    }


    @Reference (
        cardinality = ReferenceCardinality.OPTIONAL,
        policyOption = ReferencePolicyOption.GREEDY,
        target = "(handler.id=eu.agno3.fileshare.webgui)" )
    protected synchronized void bindWebapp ( ActiveHandler h ) {
        this.webapp = h;
    }


    protected synchronized void unbindWebapp ( ActiveHandler h ) {
        if ( this.webapp == h ) {
            this.webapp = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.ApplicationStateProvider#getStatus()
     */
    @Override
    public PlatformState getStatus () {
        if ( this.serviceContext == null ) {
            log.debug("No serviceContext"); //$NON-NLS-1$
            return PlatformState.FAILED;
        }

        if ( this.davServlet == null ) {
            log.debug("No davServlet"); //$NON-NLS-1$
            return PlatformState.FAILED;
        }

        if ( this.licenseService == null ) {
            log.debug("No licenseService"); //$NON-NLS-1$
            return PlatformState.FAILED;
        }

        if ( this.frontendConfig == null ) {
            log.debug("No frontendConfig"); //$NON-NLS-1$
            return PlatformState.FAILED;
        }

        if ( this.webapp == null ) {
            log.debug("No webapp"); //$NON-NLS-1$
            return PlatformState.FAILED;
        }

        if ( !this.licenseService.isLicenseValid("urn:agno3:1.0:fileshare") || this.licenseService.warnExpiration() ) { //$NON-NLS-1$
            return PlatformState.WARNING;
        }

        return PlatformState.STARTED;
    }
}
