/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.component;


/**
 * @author mbechler
 * 
 */
public class ComponentIllegalConnStateException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -5701958433268039906L;


    /**
     * 
     */
    public ComponentIllegalConnStateException () {}


    /**
     * @param m
     */
    public ComponentIllegalConnStateException ( String m ) {
        super(m);
    }


    /**
     * @param t
     */
    public ComponentIllegalConnStateException ( Throwable t ) {
        super(t);
    }


    /**
     * @param m
     * @param t
     */
    public ComponentIllegalConnStateException ( String m, Throwable t ) {
        super(m, t);
    }

}
