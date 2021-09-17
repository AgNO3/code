/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.09.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.internal;


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfigProperties;
import eu.agno3.orchestrator.system.base.units.file.PrefixUtil;
import eu.agno3.runtime.update.UpdateConfiguration;


/**
 * @author mbechler
 *
 */
public class ServiceUpdateConfiguration implements UpdateConfiguration {

    /**
     * 
     */
    private static final String FILE = "file:/"; //$NON-NLS-1$
    /**
     * 
     */
    private static final String REPOSITORIES = "repositories"; //$NON-NLS-1$
    private static final String TARGET_PROFILE = "targetProfile"; //$NON-NLS-1$
    private static final String P2_TARGET_AREA = "target"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String UPDATES_CONF = "updates.conf"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(ServiceUpdateConfiguration.class);

    private RuntimeServiceManager sm;
    private Properties cachedProps;
    private ExecutionConfigProperties executionConfig;


    /**
     * @param sm
     * @param cfg
     */
    public ServiceUpdateConfiguration ( RuntimeServiceManager sm, ExecutionConfigProperties cfg ) {
        this.sm = sm;
        this.executionConfig = cfg;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.UpdateConfiguration#getRepositories()
     */
    @Override
    public Set<URI> getRepositories () {
        Set<URI> uris = new HashSet<>();
        Properties p = loadUpdateProperties();
        for ( String repo : StringUtils.split((String) p.getOrDefault(REPOSITORIES, StringUtils.EMPTY), ',') ) {
            try {
                uris.add(new URI(repo.trim()));
            }
            catch ( URISyntaxException e ) {
                log.error("Failed to parse repository URI " + repo, e); //$NON-NLS-1$
            }
        }
        return uris;
    }


    /**
     * @param p
     * @return
     * @throws IOException
     */
    private Properties loadUpdateProperties () {

        if ( this.cachedProps != null ) {
            return this.cachedProps;
        }

        Path cfgPath = PrefixUtil.resolvePrefix(this.executionConfig, this.sm.getConfigFilesPath());
        try {
            Properties defaultProps = new Properties();
            Path p = cfgPath.resolve("defaults").resolve(UPDATES_CONF); //$NON-NLS-1$
            if ( Files.exists(p) ) {
                try ( FileChannel f = FileChannel.open(p);
                      InputStream is = Channels.newInputStream(f) ) {
                    defaultProps.load(is);
                }
            }

            p = cfgPath.resolve(UPDATES_CONF);
            Properties props = new Properties();
            if ( Files.exists(p) ) {

                try ( FileChannel f = FileChannel.open(p);
                      InputStream is = Channels.newInputStream(f) ) {
                    props.load(is);
                }
            }

            defaultProps.putAll(props);
            this.cachedProps = defaultProps;
            return defaultProps;
        }
        catch ( IOException e ) {
            log.warn("Failed to get update configuration for service", e); //$NON-NLS-1$
            return new Properties();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.UpdateConfiguration#getTargetArea()
     */
    @Override
    public URI getTargetArea () throws URISyntaxException {
        String overrideTarget = loadUpdateProperties().getProperty(P2_TARGET_AREA);
        if ( !StringUtils.isEmpty(overrideTarget) ) {
            if ( !overrideTarget.startsWith(FILE) ) {
                overrideTarget = FILE + overrideTarget;
            }
            return new URI(overrideTarget);
        }
        return this.sm.getP2InstallLocation();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.UpdateConfiguration#getTargetProfile()
     */
    @Override
    public String getTargetProfile () {
        return (String) loadUpdateProperties().getOrDefault(TARGET_PROFILE, "DefaultProfile"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.UpdateConfiguration#getOwner()
     */
    @Override
    public UserPrincipal getOwner () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.UpdateConfiguration#getGroup()
     */
    @Override
    public GroupPrincipal getGroup () {
        try {
            return this.sm.getGroupPrincipal();
        }
        catch ( ServiceManagementException e ) {
            throw new RuntimeException(e);
        }
    }

}
