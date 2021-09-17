/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.09.2014 by mbechler
 */
package eu.agno3.runtime.update.internal;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.update.UpdateConfiguration;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = UpdateConfiguration.class, configurationPid = UpdateConfiguration.PID )
public class UpdateConfigurationImpl implements UpdateConfiguration {

    private static final Logger log = Logger.getLogger(UpdateConfigurationImpl.class);
    private static final String ECLIPSE_P2_DATA_AREA = "eclipse.p2.data.area"; //$NON-NLS-1$

    private static final String P2_TARGET_AREA = "target"; //$NON-NLS-1$
    private static final String REPOSITORIES = "repositories"; //$NON-NLS-1$

    private Set<URI> repositories = new HashSet<>();
    private URI targetAreaUri;
    private String targetProfile;


    @Activate
    protected synchronized void activate ( ComponentContext context ) {

        log.debug("Activating update configuration"); //$NON-NLS-1$

        String p2targetSpec = (String) context.getProperties().get(P2_TARGET_AREA);
        if ( p2targetSpec != null ) {
            try {
                this.targetAreaUri = new URI(p2targetSpec);
            }
            catch ( URISyntaxException e ) {
                log.warn("Failed to parse P2 target area URI", e); //$NON-NLS-1$
            }
        }

        this.repositories = parseRepositoryConfig(context);
        this.targetProfile = ConfigUtil.parseString(
            context.getProperties(),
            "targetProfile", //$NON-NLS-1$
            System.getProperty(
                "eclipse.p2.profile", //$NON-NLS-1$
                "DefaultProfile")); //$NON-NLS-1$

    }


    /**
     * @param context
     * @return
     */
    private static Set<URI> parseRepositoryConfig ( ComponentContext context ) {
        Set<URI> repos = new HashSet<>();
        String repositoriesSpec = (String) context.getProperties().get(REPOSITORIES);

        if ( repositoriesSpec != null ) {
            String[] reposSpec = StringUtils.split(repositoriesSpec, ',');

            for ( String repo : reposSpec ) {
                repo = repo.trim();

                if ( repo.isEmpty() ) {
                    continue;
                }

                handleRepo(repos, repo);
            }
        }
        return repos;
    }


    /**
     * @param repos
     * @param repo
     */
    private static void handleRepo ( Set<URI> repos, String repo ) {
        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Found repository " + repo); //$NON-NLS-1$
            }
            repos.add(new URI(repo));
        }
        catch ( URISyntaxException e ) {
            log.warn("Failed to parse repo URI " + repo, e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.UpdateConfiguration#getRepositories()
     */
    @Override
    public Set<URI> getRepositories () {
        return this.repositories;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws URISyntaxException
     *
     * @see eu.agno3.runtime.update.UpdateConfiguration#getTargetArea()
     */
    @Override
    public URI getTargetArea () throws URISyntaxException {
        if ( this.targetAreaUri == null ) {
            // this should yield the currently running equinox instance
            String configDir = System.getProperty("osgi.configuration.area"); //$NON-NLS-1$
            String targetFile = System.getProperty(ECLIPSE_P2_DATA_AREA).replace("@config.dir", configDir); //$NON-NLS-1$

            if ( targetFile.startsWith("file:") ) { //$NON-NLS-1$
                targetFile = targetFile.substring(5);
            }

            File f = new File(targetFile);

            if ( !f.exists() || !f.isDirectory() ) {
                log.error("Invalid target location " + targetFile); //$NON-NLS-1$
                return null;
            }

            try {
                targetFile = f.getCanonicalPath().replace(
                    " ", //$NON-NLS-1$
                    "%20"); //$NON-NLS-1$
            }
            catch ( IOException e ) {
                log.error("Failed to get canonical path for " + f, e); //$NON-NLS-1$
            }
            if ( log.isDebugEnabled() ) {
                log.debug("Targeting currently running equinox instance " + targetFile); //$NON-NLS-1$
            }

            return new URI("file:" + targetFile); //$NON-NLS-1$
        }
        return this.targetAreaUri;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.UpdateConfiguration#getTargetProfile()
     */
    @Override
    public String getTargetProfile () {
        return this.targetProfile;
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
        return null;
    }

}
