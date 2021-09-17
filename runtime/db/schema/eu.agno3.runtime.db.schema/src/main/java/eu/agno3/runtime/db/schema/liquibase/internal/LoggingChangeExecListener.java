/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.09.2015 by mbechler
 */
package eu.agno3.runtime.db.schema.liquibase.internal;


import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.ChangeSet.ExecType;
import liquibase.changelog.ChangeSet.RunStatus;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.precondition.core.PreconditionContainer.ErrorOption;
import liquibase.precondition.core.PreconditionContainer.FailOption;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public class LoggingChangeExecListener implements ChangeExecListener {

    private static final Logger log = Logger.getLogger(LoggingChangeExecListener.class);


    /**
     * {@inheritDoc}
     *
     * @see liquibase.changelog.visitor.ChangeExecListener#preconditionErrored(liquibase.exception.PreconditionErrorException,
     *      liquibase.precondition.core.PreconditionContainer.ErrorOption)
     */
    @Override
    public void preconditionErrored ( PreconditionErrorException arg0, ErrorOption arg1 ) {

    }


    /**
     * {@inheritDoc}
     *
     * @see liquibase.changelog.visitor.ChangeExecListener#preconditionFailed(liquibase.exception.PreconditionFailedException,
     *      liquibase.precondition.core.PreconditionContainer.FailOption)
     */
    @Override
    public void preconditionFailed ( PreconditionFailedException arg0, FailOption arg1 ) {

    }


    /**
     * {@inheritDoc}
     *
     * @see liquibase.changelog.visitor.ChangeExecListener#ran(liquibase.changelog.ChangeSet,
     *      liquibase.changelog.DatabaseChangeLog, liquibase.database.Database, liquibase.changelog.ChangeSet.ExecType)
     */
    @Override
    public void ran ( ChangeSet arg0, DatabaseChangeLog arg1, Database arg2, ExecType arg3 ) {

    }


    /**
     * {@inheritDoc}
     *
     * @see liquibase.changelog.visitor.ChangeExecListener#ran(liquibase.change.Change, liquibase.changelog.ChangeSet,
     *      liquibase.changelog.DatabaseChangeLog, liquibase.database.Database)
     */
    @Override
    public void ran ( Change arg0, ChangeSet arg1, DatabaseChangeLog arg2, Database arg3 ) {

    }


    /**
     * {@inheritDoc}
     *
     * @see liquibase.changelog.visitor.ChangeExecListener#rolledBack(liquibase.changelog.ChangeSet,
     *      liquibase.changelog.DatabaseChangeLog, liquibase.database.Database)
     */
    @Override
    public void rolledBack ( ChangeSet arg0, DatabaseChangeLog arg1, Database arg2 ) {

    }


    /**
     * {@inheritDoc}
     *
     * @see liquibase.changelog.visitor.ChangeExecListener#runFailed(liquibase.changelog.ChangeSet,
     *      liquibase.changelog.DatabaseChangeLog, liquibase.database.Database, java.lang.Exception)
     */
    @Override
    public void runFailed ( ChangeSet arg0, DatabaseChangeLog arg1, Database arg2, Exception arg3 ) {

    }


    /**
     * {@inheritDoc}
     *
     * @see liquibase.changelog.visitor.ChangeExecListener#willRun(liquibase.changelog.ChangeSet,
     *      liquibase.changelog.DatabaseChangeLog, liquibase.database.Database, liquibase.changelog.ChangeSet.RunStatus)
     */
    @Override
    public void willRun ( ChangeSet chset, DatabaseChangeLog chlog, Database arg2, RunStatus arg3 ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Running changeset %s of %s", chset.getId(), chset.getFilePath())); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see liquibase.changelog.visitor.ChangeExecListener#willRun(liquibase.change.Change,
     *      liquibase.changelog.ChangeSet, liquibase.changelog.DatabaseChangeLog, liquibase.database.Database)
     */
    @Override
    public void willRun ( Change arg0, ChangeSet arg1, DatabaseChangeLog arg2, Database arg3 ) {

    }

}
