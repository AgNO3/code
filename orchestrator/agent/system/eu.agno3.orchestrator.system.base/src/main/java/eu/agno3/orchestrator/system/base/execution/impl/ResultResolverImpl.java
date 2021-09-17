/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl;


import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.ResultReference;
import eu.agno3.orchestrator.system.base.execution.ResultResolver;
import eu.agno3.orchestrator.system.base.execution.UnitResults;
import eu.agno3.orchestrator.system.base.execution.exception.ResultReferenceException;


/**
 * @author mbechler
 *
 */
/**
 * @author mbechler
 * 
 */
public class ResultResolverImpl implements ResultResolver {

    private UnitResults res;


    /**
     * @param res
     */
    public ResultResolverImpl ( UnitResults res ) {
        this.res = res;

    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ResultResolver#fetchResult(eu.agno3.orchestrator.system.base.execution.ResultReference)
     */
    @Override
    public <T extends Result> T fetchResult ( ResultReference<T> ref ) throws ResultReferenceException {
        return this.res.getResult(ref.getExecutionUnit());
    }

}
