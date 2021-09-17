/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2015 by mbechler
 */
package eu.agno3.runtime.update.internal;


import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.util.HashSet;
import java.util.Set;

import eu.agno3.runtime.update.UpdateConfiguration;


/**
 * @author mbechler
 *
 */
public class UpdateConfigurationWithRepositories implements UpdateConfiguration {

    private UpdateConfiguration delegate;
    private Set<URI> repositories = new HashSet<>();


    /**
     * @param config
     * @param repositories
     */
    public UpdateConfigurationWithRepositories ( UpdateConfiguration config, Set<URI> repositories ) {
        this.delegate = config;
        this.repositories.addAll(config.getRepositories());
        this.repositories.addAll(repositories);
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
     * @see eu.agno3.runtime.update.UpdateConfiguration#getTargetArea()
     */
    @Override
    public URI getTargetArea () throws URISyntaxException {
        return this.delegate.getTargetArea();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.UpdateConfiguration#getTargetProfile()
     */
    @Override
    public String getTargetProfile () {
        return this.delegate.getTargetProfile();
    }


    @Override
    public UserPrincipal getOwner () {
        return this.delegate.getOwner();
    }


    @Override
    public GroupPrincipal getGroup () {
        return this.delegate.getGroup();
    }

}
