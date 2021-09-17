/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.liquibase.internal;


import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.logging.LogFactory;
import liquibase.logging.LogLevel;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    liquibase.logging.Logger.class, LogFactory.class
} )
public class LiquibaseLoggerAdapter extends LogFactory implements liquibase.logging.Logger {

    private static final Logger log = Logger.getLogger(LiquibaseLoggerAdapter.class.getPackage().getName());


    @Activate
    protected synchronized void activate ( ComponentContext context ) {
        log.info("Starting liquibase logger"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see liquibase.logging.Logger#closeLogFile()
     */
    @Override
    public void closeLogFile () {

    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.servicelocator.PrioritizedService#getPriority()
     */
    @Override
    public int getPriority () {
        return 100;
    }


    @Override
    public void setChangeLog ( DatabaseChangeLog databaseChangeLog ) {
        // ignore
    }


    @Override
    public void setChangeSet ( ChangeSet changeSet ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see liquibase.logging.LogFactory#getLog()
     */
    @Override
    public liquibase.logging.Logger getLog () {
        return this;
    }


    /**
     * {@inheritDoc}
     *
     * @see liquibase.logging.LogFactory#getLog(java.lang.String)
     */
    @Override
    public liquibase.logging.Logger getLog ( String name ) {
        return this;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.logging.Logger#debug(java.lang.String)
     */
    @Override
    public void debug ( String arg0 ) {
        log.debug(arg0);
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.logging.Logger#debug(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void debug ( String arg0, Throwable arg1 ) {
        log.debug(arg0, arg1);
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.logging.Logger#getLogLevel()
     */
    @Override
    public LogLevel getLogLevel () {
        return LogLevel.DEBUG;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.logging.Logger#info(java.lang.String)
     */
    @Override
    public void info ( String arg0 ) {
        log.info(arg0);
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.logging.Logger#info(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void info ( String arg0, Throwable arg1 ) {
        log.info(arg0, arg1);
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.logging.Logger#setLogLevel(java.lang.String)
     */
    @Override
    public void setLogLevel ( String arg0 ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.logging.Logger#setLogLevel(liquibase.logging.LogLevel)
     */
    @Override
    public void setLogLevel ( LogLevel arg0 ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.logging.Logger#setLogLevel(java.lang.String, java.lang.String)
     */
    @Override
    public void setLogLevel ( String arg0, String arg1 ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.logging.Logger#setName(java.lang.String)
     */
    @Override
    public void setName ( String arg0 ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.logging.Logger#severe(java.lang.String)
     */
    @Override
    public void severe ( String arg0 ) {
        log.error(arg0);

    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.logging.Logger#severe(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void severe ( String arg0, Throwable arg1 ) {
        log.error(arg0, arg1);
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.logging.Logger#warning(java.lang.String)
     */
    @Override
    public void warning ( String arg0 ) {
        log.warn(arg0);
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.logging.Logger#warning(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void warning ( String arg0, Throwable arg1 ) {
        log.warn(arg0, arg1);
    }

}
