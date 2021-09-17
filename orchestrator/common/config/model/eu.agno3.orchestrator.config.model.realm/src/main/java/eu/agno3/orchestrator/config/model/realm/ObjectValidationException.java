/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


/**
 * @author mbechler
 * 
 */
public class ObjectValidationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 5915840661191910571L;


    /**
     * 
     */
    public ObjectValidationException () {
        super();
    }


    /**
     * @param m
     * @param t
     */
    public ObjectValidationException ( String m, Throwable t ) {
        super(m, t);
    }


    /**
     * @param m
     */
    public ObjectValidationException ( String m ) {
        super(m);
    }


    /**
     * @param t
     */
    public ObjectValidationException ( Throwable t ) {
        super(t);
    }

}
