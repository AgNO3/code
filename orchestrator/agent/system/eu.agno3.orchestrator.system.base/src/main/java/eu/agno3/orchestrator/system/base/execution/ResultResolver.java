/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import eu.agno3.orchestrator.system.base.execution.exception.ResultReferenceException;


/**
 * @author mbechler
 * 
 */
public interface ResultResolver {

    /**
     * @param ref
     * @return the result for the reference
     * @throws ResultReferenceException
     */
    <T extends Result> T fetchResult ( ResultReference<T> ref ) throws ResultReferenceException;
}
