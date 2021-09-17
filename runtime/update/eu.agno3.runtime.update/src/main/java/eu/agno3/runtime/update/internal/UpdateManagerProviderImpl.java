/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.09.2014 by mbechler
 */
package eu.agno3.runtime.update.internal;


import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.eclipse.equinox.internal.p2.repository.Transport;
import org.eclipse.equinox.internal.provisional.configurator.Configurator;
import org.eclipse.equinox.internal.provisional.frameworkadmin.FrameworkAdmin;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.update.UpdateConfiguration;
import eu.agno3.runtime.update.UpdateException;
import eu.agno3.runtime.update.UpdateManager;
import eu.agno3.runtime.update.UpdateManagerProvider;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "restriction" )
@Component ( service = UpdateManagerProvider.class )
public class UpdateManagerProviderImpl implements UpdateManagerProvider {

    private static final Logger log = Logger.getLogger(UpdateManagerProviderImpl.class);

    private IProvisioningAgentProvider provAgentProvider;
    private Transport transport;
    private FrameworkAdmin frameworkAdmin;
    private UpdateConfiguration config;
    private ComponentContext componentContext;
    private Configurator configurator;
    private IProvisioningAgentProvider provAgentProv;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.componentContext = ctx;

        try {
            this.provAgentProv = new AgentProviderImpl(ctx, this.config.getTargetArea());
        }
        catch ( URISyntaxException e ) {
            log.error("Failed to parse target area", e); //$NON-NLS-1$
        }
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        this.componentContext = null;
    }


    @Reference
    protected synchronized void setFrameworkAdmin ( FrameworkAdmin fwa ) {
        this.frameworkAdmin = fwa;
    }


    protected synchronized void unsetFrameworkAdmin ( FrameworkAdmin fwa ) {
        if ( this.frameworkAdmin == fwa ) {
            this.frameworkAdmin = null;
        }
    }


    @Reference
    protected synchronized void setProvAgentProvider ( IProvisioningAgentProvider pap ) {
        this.provAgentProvider = pap;
    }


    protected synchronized void unsetProvAgentProvider ( IProvisioningAgentProvider pap ) {
        if ( this.provAgentProvider == pap ) {
            this.provAgentProvider = null;
        }
    }


    @Reference
    protected synchronized void setUpdateConfiguration ( UpdateConfiguration upc ) {
        this.config = upc;
    }


    protected synchronized void unsetUpdateConfiguration ( UpdateConfiguration upc ) {
        if ( this.config == upc ) {
            this.config = null;
        }
    }


    @Reference
    protected synchronized void setConfigurator ( Configurator cf ) {
        this.configurator = cf;
    }


    protected synchronized void unsetConfigurator ( Configurator cf ) {
        if ( this.configurator == cf ) {
            this.configurator = null;
        }
    }


    @Reference
    protected synchronized void setTransport ( Transport tr ) {
        this.transport = tr;
    }


    protected synchronized void unsetTransport ( Transport tr ) {
        if ( this.transport == tr ) {
            this.transport = null;
        }
    }


    @Override
    public UpdateManager getLocalUpdateManager () throws UpdateException {
        return new ProvisioningManagerImpl(this.provAgentProv, this.configurator, this.frameworkAdmin, this.transport, this.config);
    }


    @SuppressWarnings ( "resource" )
    @Override
    public UpdateManager getUpdateManager ( UpdateConfiguration cfg ) throws UpdateException {
        AgentProviderImpl agentProv;
        try {
            agentProv = new AgentProviderImpl(this.componentContext, cfg.getTargetArea());
        }
        catch ( URISyntaxException e ) {
            throw new UpdateException("Failed to get target area", e); //$NON-NLS-1$
        }

        return new ProvisioningManagerImpl(agentProv, this.configurator, this.frameworkAdmin, this.transport, cfg);

    }

}
