/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;




/**
 * @author mbechler
 * 
 */
public interface PhaseResults extends Result {

    /**
     * @param p
     * @param r
     */
    void add ( Phase p, UnitResults r );


    /**
     * @param p
     * @return the overall result of a phase
     */
    UnitResults getPhaseResult ( Phase p );

}