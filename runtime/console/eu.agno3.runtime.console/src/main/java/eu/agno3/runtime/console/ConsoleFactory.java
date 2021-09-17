/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2013 by mbechler
 */
package eu.agno3.runtime.console;


import org.jline.terminal.Terminal;


/**
 * @author mbechler
 * 
 */
public interface ConsoleFactory {

    /**
     * Create a console on the given streams
     * 
     * @param in
     * @param out
     * @param err
     * @param title
     * @param term
     * @return a console instance
     */
    Console createConsole ( Terminal term );

}
