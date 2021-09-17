/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.output;


/**
 * @author mbechler
 * 
 */
public interface Out {

    /**
     * @return whether debugging is active for this output
     */
    boolean isDebugEnabled ();


    /**
     * @param msg
     */
    void debug ( String msg );


    /**
     * @param msg
     * @param t
     */
    void debug ( String msg, Throwable t );


    /**
     * @param msg
     */
    void info ( String msg );


    /**
     * @param msg
     * @param t
     */
    void info ( String msg, Throwable t );


    /**
     * @param msg
     */
    void error ( String msg );


    /**
     * @param msg
     * @param t
     */
    void error ( String msg, Throwable t );


    /**
     * @param name
     * @return a child output with the given name
     */
    Out getChild ( String name );

}
