/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.01.2016 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.log;


import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class Log extends AbstractExecutionUnit<StatusOnlyResult, Log, LogConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 1925167099873420790L;

    private Level level = Level.INFO;
    private String message;


    /**
     * @return the level
     */
    public Level getLevel () {
        return this.level;
    }


    /**
     * @param lvl
     */
    void setLevel ( Level lvl ) {
        this.level = lvl;
    }


    /**
     * @return the message
     */
    public String getMessage () {
        return this.message;
    }


    /**
     * @param message
     *            the message to set
     */
    void setMessage ( String message ) {
        this.message = message;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult prepare ( Context context ) throws ExecutionException {
        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult execute ( Context context ) throws ExecutionException {
        String msg = getMessage();
        if ( StringUtils.isEmpty(msg) ) {
            return new StatusOnlyResult(Status.SKIPPED);
        }
        switch ( getLevel() ) {
        case DEBUG:
            context.getOutput().debug(msg);
            break;
        case INFO:
            context.getOutput().info(msg);
            break;
        case WARNING:
        case ERROR:
        default:
            context.getOutput().error(msg);
            break;

        }
        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public LogConfigurator createConfigurator () {
        return new LogConfigurator(this);
    }

}
