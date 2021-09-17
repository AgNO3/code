/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.result;


import eu.agno3.orchestrator.system.base.execution.Status;


/**
 * @author mbechler
 * 
 */
public class NOPResult extends StatusOnlyResult {

    /**
     * 
     */
    private static final long serialVersionUID = 8499206680675800515L;


    /**
     * 
     */
    public NOPResult () {
        super(Status.SUCCESS);
    }
}
