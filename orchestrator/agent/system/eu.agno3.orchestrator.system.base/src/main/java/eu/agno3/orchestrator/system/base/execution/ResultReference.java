/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import java.io.Serializable;


/**
 * @author mbechler
 * @param <T>
 *            result type
 * 
 */
public class ResultReference <T extends Result> implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2102496020207639894L;

    private ExecutionUnit<T, ?, ?> unit;


    /**
     * @param unit
     */
    public ResultReference ( ExecutionUnit<T, ?, ?> unit ) {
        this.unit = unit;
    }


    /**
     * @return the referenced execution unit
     */
    public ExecutionUnit<T, ?, ?> getExecutionUnit () {
        return this.unit;
    }
}
