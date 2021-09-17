/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2016 by mbechler
 */
package eu.agno3.orchestrator.agent.system.info.units;


import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class RefreshSystemInformationConfigurator
        extends AbstractConfigurator<StatusOnlyResult, RefreshSystemInformation, RefreshSystemInformationConfigurator> {

    /**
     * @param unit
     */
    protected RefreshSystemInformationConfigurator ( RefreshSystemInformation unit ) {
        super(unit);
    }


    /**
     * @return this configurator
     */
    public RefreshSystemInformationConfigurator rescanPartitions () {
        getExecutionUnit().setRescanPartitions(true);
        return self();
    }


    /**
     * @return this configurator
     */
    public RefreshSystemInformationConfigurator ignoreErrors () {
        getExecutionUnit().setIgnoreError(true);
        return self();
    }

}
