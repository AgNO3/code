/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.binding;


/**
 * @author mbechler
 * 
 */
public class XMLBindingException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 8034898136454710266L;


    /**
     * 
     */
    public XMLBindingException () {}


    /**
     * @param arg0
     */
    public XMLBindingException ( String arg0 ) {
        super(arg0);
    }


    /**
     * @param arg0
     */
    public XMLBindingException ( Throwable arg0 ) {
        super(arg0);
    }


    /**
     * @param arg0
     * @param arg1
     */
    public XMLBindingException ( String arg0, Throwable arg1 ) {
        super(arg0, arg1);
    }

}
