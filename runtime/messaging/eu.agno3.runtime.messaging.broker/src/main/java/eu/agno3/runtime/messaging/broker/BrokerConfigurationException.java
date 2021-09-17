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
public class BrokerConfigurationException extends BrokerException {

    /**
     * 
     */
    private static final long serialVersionUID = 8145663445409747650L;


    /**
     * 
     */
    public BrokerConfigurationException () {}


    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     */
    public BrokerConfigurationException ( String arg0, Throwable arg1, boolean arg2, boolean arg3 ) {
        super(arg0, arg1, arg2, arg3);
    }


    /**
     * @param arg0
     * @param arg1
     */
    public BrokerConfigurationException ( String arg0, Throwable arg1 ) {
        super(arg0, arg1);
    }


    /**
     * @param arg0
     */
    public BrokerConfigurationException ( String arg0 ) {
        super(arg0);
    }


    /**
     * @param arg0
     */
    public BrokerConfigurationException ( Throwable arg0 ) {
        super(arg0);
    }

}
