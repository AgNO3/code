/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.01.2016 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.log;


import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class LogConfigurator extends AbstractConfigurator<StatusOnlyResult, Log, LogConfigurator> {

    /**
     * @param unit
     */
    protected LogConfigurator ( Log unit ) {
        super(unit);
    }


    /**
     * 
     * @param message
     * @return this
     */
    public LogConfigurator msg ( String message ) {
        this.getExecutionUnit().setMessage(message);
        return this.self();
    }


    /**
     * 
     * @param lvl
     * @return this
     */
    public LogConfigurator level ( Level lvl ) {
        this.getExecutionUnit().setLevel(lvl);
        return this.self();
    }


    /**
     * 
     * @param message
     * @return this
     */
    public LogConfigurator debug ( String message ) {
        return this.level(Level.DEBUG).msg(message);
    }


    /**
     * 
     * @param message
     * @return this
     */
    public LogConfigurator info ( String message ) {
        return this.level(Level.INFO).msg(message);
    }


    /**
     * 
     * @param message
     * @return this
     */
    public LogConfigurator warning ( String message ) {
        return this.level(Level.WARNING).msg(message);
    }


    /**
     * 
     * @param message
     * @return this
     */
    public LogConfigurator error ( String message ) {
        return this.level(Level.ERROR).msg(message);
    }

}
