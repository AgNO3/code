/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.service;


import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 * 
 */
public class ServiceResult extends StatusOnlyResult implements Result {

    /**
     * 
     */
    private static final long serialVersionUID = 7378181282044031653L;


    /**
     * @param s
     */
    public ServiceResult ( Status s ) {
        super(s);
    }

}
