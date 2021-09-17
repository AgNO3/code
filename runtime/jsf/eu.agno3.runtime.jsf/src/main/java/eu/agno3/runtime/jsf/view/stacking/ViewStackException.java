/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.01.2014 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking;


import javax.faces.FacesException;


/**
 * @author mbechler
 * 
 */
public class ViewStackException extends FacesException {

    /**
     * 
     */
    private static final long serialVersionUID = -5739270695409831440L;


    /**
     * @param msg
     * @param t
     */
    public ViewStackException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public ViewStackException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public ViewStackException ( Throwable t ) {
        super(t);
    }

}
