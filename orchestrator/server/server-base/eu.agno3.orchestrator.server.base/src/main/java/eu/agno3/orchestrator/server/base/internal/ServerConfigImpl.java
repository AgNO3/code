/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.base.internal;


import java.net.URI;
import java.net.URISyntaxException;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.server.config.ServerConfiguration;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.http.service.session.SessionManagerFactory;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.util.net.LocalHostUtil;


/**
 * @author mbechler
 * 
 */
@Component ( service = ServerConfiguration.class, configurationPid = ServerConfigImpl.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class ServerConfigImpl implements ServerConfiguration {

    private static final String AUTH_PROTOCOL = "https"; //$NON-NLS-1$
    private static final String AUTH_CONTEXT = "/auth/"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(ServerConfigImpl.class);

    /**
     * Configuration PID
     */
    public static final String PID = "server"; //$NON-NLS-1$

    private static final String ID = "id"; //$NON-NLS-1$

    private static final String AUTH_SERVER = "authServer"; //$NON-NLS-1$

    private Optional<@NonNull UUID> serverId = Optional.empty();

    private URI authServerUrl;
    private boolean localAuthServer;

    private TLSContext localAuthServerTLSContext;

    private SessionManagerFactory sessManagerFactory;
    private Set<String> allowedAuthServerNames;


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, target = "(subsystem=https)" )
    protected synchronized void setLocalAuthServerTLSContext ( TLSContext context ) {
        this.localAuthServerTLSContext = context;
    }


    protected synchronized void unsetLocalAuthServerTLSContext ( TLSContext context ) {
        if ( this.localAuthServerTLSContext == context ) {
            this.localAuthServerTLSContext = null;
        }
    }


    @Reference
    protected synchronized void setSessionManagerFactory ( SessionManagerFactory smf ) {
        this.sessManagerFactory = smf;
    }


    protected synchronized void unsetSessionManagerFactory ( SessionManagerFactory smf ) {
        if ( this.sessManagerFactory == smf ) {
            this.sessManagerFactory = null;
        }
    }


    @Activate
    protected void activate ( ComponentContext ctx ) {
        String idSpec = (String) ctx.getProperties().get(ID);

        UUID serverUUID;
        if ( idSpec != null ) {
            serverUUID = UUID.fromString(idSpec);
        }
        else {
            log.warn("No server ID configured, generating a random one"); //$NON-NLS-1$
            serverUUID = UUID.randomUUID();
        }

        if ( serverUUID != null ) {
            this.serverId = Optional.of(serverUUID);
        }

        updateAuthServerURL(ctx);
    }


    @Modified
    protected void modified ( ComponentContext ctx ) {
        updateAuthServerURL(ctx);
    }


    /**
     * @param ctx
     */
    private void updateAuthServerURL ( ComponentContext ctx ) {
        this.allowedAuthServerNames = new HashSet<>();
        try {
            String authServerSpec = (String) ctx.getProperties().get(AUTH_SERVER);
            if ( !StringUtils.isBlank(authServerSpec) ) {
                this.authServerUrl = new URI(authServerSpec);
                this.allowedAuthServerNames.add(this.authServerUrl.getHost());
                this.localAuthServer = false;
            }
            else {
                String guessPrimaryHostName = LocalHostUtil.guessPrimaryHostName();
                if ( !StringUtils.isBlank(guessPrimaryHostName) ) {
                    this.localAuthServer = true;
                    this.allowedAuthServerNames.add(guessPrimaryHostName);
                    this.authServerUrl = new URI(AUTH_PROTOCOL, null, guessPrimaryHostName, 8443, AUTH_CONTEXT, null, null);
                }
                else {
                    log.error("Do not have any network addresses, cannot determine server URL"); //$NON-NLS-1$
                }
            }
        }
        catch ( URISyntaxException e ) {
            log.warn("Failed to determine auth server URL", e); //$NON-NLS-1$
        }

        Set<String> allow = ConfigUtil.parseStringSet(ctx.getProperties(), "allowedAuthServerNames", null); //$NON-NLS-1$
        if ( allow != null ) {
            this.allowedAuthServerNames.addAll(allow);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.config.ServerConfiguration#getServerId()
     */
    @Override
    public @NonNull UUID getServerId () {
        return this.serverId.get();
    }


    /**
     * @return the localAuthServer
     */
    @Override
    public boolean isLocalAuthServer () {
        return this.localAuthServer;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.config.ServerConfiguration#getAuthServerUrl()
     */
    @Override
    public URI getAuthServerUrl () {
        return this.authServerUrl;
    }


    /**
     * @return the allowedAuthServerNames
     */
    @Override
    public Set<String> getAllowedAuthServerNames () {
        return this.allowedAuthServerNames;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.config.ServerConfiguration#getSessionCookieName()
     */
    @Override
    public String getSessionCookieName () {
        return this.sessManagerFactory.getSessionCookieName();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.config.ServerConfiguration#getAuthServerPubKey()
     */
    @Override
    public PublicKey getAuthServerPubKey () {
        if ( this.localAuthServerTLSContext != null ) {
            return this.localAuthServerTLSContext.getPrimaryCertificatePubKey();
        }
        return null;
    }

}
