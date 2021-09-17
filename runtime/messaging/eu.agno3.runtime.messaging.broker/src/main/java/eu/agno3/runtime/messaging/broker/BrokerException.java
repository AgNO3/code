/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.broker;


/**
 * @author mbechler
 * 
 */
public class BrokerException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -3959310085125974571L;


    /**
     * 
     */
    public BrokerException () {
        super();
    }


    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     */
    public BrokerException ( String arg0, Throwable arg1, boolean arg2, boolean arg3 ) {
        super(arg0, arg1, arg2, arg3);
    }


    /**
     * @param arg0
     * @param arg1
     */
    public BrokerException ( String arg0, Throwable arg1 ) {
        super(arg0, arg1);
    }


    /**
     * @param arg0
     */
    public BrokerException ( String arg0 ) {
        super(arg0);
    }


    /**
     * @param arg0
     */
    public BrokerException ( Throwable arg0 ) {
        super(arg0);
    }

}
