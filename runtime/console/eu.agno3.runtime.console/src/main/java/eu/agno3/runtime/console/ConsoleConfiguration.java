/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.06.2013 by mbechler
 */
package eu.agno3.runtime.console;


/**
 * @author mbechler
 * 
 */
public interface ConsoleConfiguration {

    /**
     * 
     */
    final String PID = "console"; //$NON-NLS-1$


    /**
     * @return application name
     */
    String getApplicationName ();


    /**
     * @return console prompt
     */
    String getPrompt ();


    /**
     * 
     * @return command scopes
     */
    String getScopes ();
}
