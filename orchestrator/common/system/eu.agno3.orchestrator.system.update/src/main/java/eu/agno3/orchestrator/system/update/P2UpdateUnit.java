/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.update;


import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * @author mbechler
 *
 */
public class P2UpdateUnit extends AbstractServiceUpdateUnit<P2UpdateUnit> {

    /**
     * 
     */
    private static final long serialVersionUID = -8281731689145001983L;

    private Set<URI> repositories = new HashSet<>();
    private Set<P2FeatureTarget> targets = new HashSet<>();
    private boolean forceOffline;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.update.AbstractServiceUpdateUnit#getType()
     */
    @Override
    public Class<P2UpdateUnit> getType () {
        return P2UpdateUnit.class;
    }


    /**
     * @return the repositories
     */
    public Set<URI> getRepositories () {
        return this.repositories;
    }


    /**
     * @param repositories
     *            the repositories to set
     */
    public void setRepositories ( Set<URI> repositories ) {
        this.repositories = repositories;
    }


    /**
     * @return the targets
     */
    public Set<P2FeatureTarget> getTargets () {
        return this.targets;
    }


    /**
     * @param targets
     *            the targets to set
     */
    public void setTargets ( Set<P2FeatureTarget> targets ) {
        this.targets = targets;
    }


    /**
     * @return the forceOffline
     */
    public boolean getForceOffline () {
        return this.forceOffline;
    }


    /**
     * @param forceOffline
     *            the forceOffline to set
     */
    public void setForceOffline ( boolean forceOffline ) {
        this.forceOffline = forceOffline;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.update.AbstractServiceUpdateUnit#merge(eu.agno3.orchestrator.system.update.AbstractServiceUpdateUnit)
     */
    @Override
    public P2UpdateUnit merge ( P2UpdateUnit next ) {
        P2UpdateUnit merged = new P2UpdateUnit();

        Set<URI> repos = new HashSet<>();
        repos.addAll(this.repositories);
        repos.addAll(next.getRepositories());
        merged.setRepositories(repos);

        Map<String, P2FeatureTarget> mergedTargets = new HashMap<>();
        for ( P2FeatureTarget entry : this.targets ) {
            mergedTargets.put(entry.getFeatureId(), entry);
        }

        for ( P2FeatureTarget entry : next.getTargets() ) {
            mergedTargets.put(entry.getFeatureId(), entry);
        }
        merged.setForceOffline(this.forceOffline | next.forceOffline);
        return merged;
    }
}
