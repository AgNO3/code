/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.01.2014 by mbechler
 */
package eu.agno3.runtime.console.internal;


/**
 * @author mbechler
 * 
 */
public class ConsoleException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -4932242157601896260L;


    /**
     * 
     */
    public ConsoleException () {}


    /**
     * @param arg0
     */
    public ConsoleException ( String arg0 ) {
        super(arg0);
    }


    /**
     * @param arg0
     */
    public ConsoleException ( Throwable arg0 ) {
        super(arg0);
    }


    /**
     * @param arg0
     * @param arg1
     */
    public ConsoleException ( String arg0, Throwable arg1 ) {
        super(arg0, arg1);
    }


    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     */
    public ConsoleException ( String arg0, Throwable arg1, boolean arg2, boolean arg3 ) {
        super(arg0, arg1, arg2, arg3);
    }

}
