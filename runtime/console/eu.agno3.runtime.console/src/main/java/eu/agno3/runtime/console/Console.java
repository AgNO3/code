/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2013 by mbechler
 */
package eu.agno3.runtime.console;


import org.apache.karaf.shell.api.console.Session;


/**
 * @author mbechler
 * 
 */
public interface Console extends Runnable {

    /**
     * Exit the running console
     */
    void exit ();


    /**
     * Start the console
     */
    void start ();


    /**
     * Set a handler called when the console exits
     * 
     * @param handler
     */
    void setShutdownHandler ( ShutdownHandler handler );


    /**
     * Get the session for this console
     * 
     * @return the command session
     */
    Session getSession ();
}
