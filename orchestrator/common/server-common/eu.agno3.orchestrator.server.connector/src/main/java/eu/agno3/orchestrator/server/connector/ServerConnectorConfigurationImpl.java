/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.connector;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Modified;


/**
 * @author mbechler
 *
 */
public class ServerConnectorConfigurationImpl implements ServerConnectorConfiguration {

    private Optional<@NonNull InetAddress> serverAddress = Optional.empty();
    private Optional<@NonNull UUID> componentId = Optional.empty();
    private int serverPort;


    @Activate
    protected synchronized void activate ( ComponentContext context ) throws ServerConnectorException {
        Dictionary<String, Object> config = context.getProperties();

        this.serverAddress = Optional.of(getConfiguredServerAddress(config));
        this.componentId = Optional.of(getConfiguredComponentId(config));
        this.serverPort = getConfiguredServerPort(config);

    }


    @Modified
    protected synchronized void modified ( ComponentContext context ) throws ServerConnectorException {
        Dictionary<String, Object> config = context.getProperties();

        this.serverAddress = Optional.of(getConfiguredServerAddress(config));
        this.componentId = Optional.of(getConfiguredComponentId(config));
        this.serverPort = getConfiguredServerPort(config);

    }


    /**
     * @return the serverAddress
     */
    @Override
    @NonNull
    public InetAddress getServerAddress () {
        return this.serverAddress.get();
    }


    /**
     * @return the serverPort
     */
    @Override
    public int getServerPort () {
        return this.serverPort;
    }


    /**
     * @return the componentId
     */
    @Override
    @NonNull
    public UUID getComponentId () {
        return this.componentId.get();
    }


    /**
     * @param config
     * @throws ServerConnectorException
     */
    private static @NonNull InetAddress getConfiguredServerAddress ( Dictionary<String, Object> config ) throws ServerConnectorException {
        String serverAddressSpec = (String) config.get("server"); //$NON-NLS-1$

        if ( serverAddressSpec != null ) {
            try {
                InetAddress byName = InetAddress.getByName(serverAddressSpec);
                if ( byName == null ) {
                    throw new ServerConnectorException("Address is null"); //$NON-NLS-1$
                }

                return byName;
            }
            catch ( UnknownHostException e ) {
                throw new ServerConnectorException("Failed to resolve server hostname " + serverAddressSpec, e); //$NON-NLS-1$
            }
        }

        throw new ServerConnectorException("No server host address configured"); //$NON-NLS-1$
    }


    protected @NonNull UUID getConfiguredComponentId ( Dictionary<String, Object> config ) throws ServerConnectorException {
        String agentIdSpec = (String) config.get("id"); //$NON-NLS-1$

        if ( agentIdSpec != null ) {
            try {
                UUID uuid = UUID.fromString(agentIdSpec);

                if ( uuid == null ) {
                    throw new ServerConnectorException("component id is null"); //$NON-NLS-1$
                }
                return uuid;
            }
            catch ( IllegalArgumentException e ) {
                throw new ServerConnectorException("Illegal component ID format:", e); //$NON-NLS-1$
            }
        }

        throw new ServerConnectorException("No component ID configured"); //$NON-NLS-1$
    }


    /**
     * @param config
     * @throws ServerConnectorException
     */
    private static int getConfiguredServerPort ( Dictionary<String, Object> config ) throws ServerConnectorException {
        String serverPortSpec = (String) config.get("serverPort"); //$NON-NLS-1$

        if ( serverPortSpec != null ) {
            try {
                return Integer.parseInt(serverPortSpec);
            }
            catch ( NumberFormatException e ) {
                throw new ServerConnectorException("IllegalPortSpecification " + serverPortSpec); //$NON-NLS-1$
            }
        }

        return 61616;
    }
}
