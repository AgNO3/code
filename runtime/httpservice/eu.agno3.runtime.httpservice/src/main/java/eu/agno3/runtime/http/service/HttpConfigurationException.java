/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service;


/**
 * @author mbechler
 * 
 */
public class HttpConfigurationException extends Exception {

    /**
     * 
     */
    public HttpConfigurationException () {
        super();
    }


    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     */
    public HttpConfigurationException ( String arg0, Throwable arg1, boolean arg2, boolean arg3 ) {
        super(arg0, arg1, arg2, arg3);
    }


    /**
     * @param arg0
     * @param arg1
     */
    public HttpConfigurationException ( String arg0, Throwable arg1 ) {
        super(arg0, arg1);
    }


    /**
     * @param arg0
     */
    public HttpConfigurationException ( String arg0 ) {
        super(arg0);
    }


    /**
     * @param arg0
     */
    public HttpConfigurationException ( Throwable arg0 ) {
        super(arg0);
    }

    /**
     * 
     */
    private static final long serialVersionUID = -2131713531719051326L;

}
