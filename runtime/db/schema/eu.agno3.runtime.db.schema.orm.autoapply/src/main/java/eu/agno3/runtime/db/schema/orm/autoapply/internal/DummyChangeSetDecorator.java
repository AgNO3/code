/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.autoapply.internal;


import java.util.List;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.util.StringUtils;


/**
 * @author mbechler
 * 
 */
public class DummyChangeSetDecorator extends ChangeSet {

    private ChangeSet delegate;


    /**
     * @param delegate
     * @param filePath
     */
    public DummyChangeSetDecorator ( ChangeSet delegate, String filePath ) {
        super(delegate.getId(), delegate.getAuthor(), delegate.isAlwaysRun(), delegate.isRunOnChange(), filePath, // $
            StringUtils.join(delegate.getContexts().getContexts(), ","), //$NON-NLS-1$
            StringUtils.join(delegate.getDbmsSet(), ","), //$NON-NLS-1$
            delegate.isRunInTransaction(),
            delegate.getObjectQuotingStrategy(),
            delegate.getChangeLog());
        this.delegate = delegate;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.changelog.ChangeSet#getChanges()
     */
    @Override
    public List<Change> getChanges () {
        return this.delegate.getChanges();
    }
}
