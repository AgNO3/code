/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.versioning;


import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.AbstractModelException;


/**
 * Provides the revisions an specific object
 * 
 * @author mbechler
 * @param <T>
 *            configuration type
 * @param <TMutable>
 *            mutable configuration type
 */
public abstract class AbstractRevisionProviderImpl <T, TMutable extends T> implements RevisionProvider {

    private static final Logger log = Logger.getLogger(AbstractRevisionProviderImpl.class);


    protected abstract List<VersionInfo> fetchRevisions () throws AbstractModelException;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.versioning.RevisionProvider#fetchAllRevisions()
     */
    @Override
    public List<VersionInfo> fetchAllRevisions () throws AbstractModelException {

        List<VersionInfo> revs = this.fetchRevisions();

        if ( revs == null ) {
            return Collections.EMPTY_LIST;
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Fetched %d revisions", revs.size())); //$NON-NLS-1$
        }

        Collections.reverse(revs);
        return revs;
    }

}
