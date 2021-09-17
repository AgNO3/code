/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.06.2013 by mbechler
 */
package eu.agno3.runtime.console;


import org.apache.karaf.shell.api.console.Session;
import org.jline.reader.Completer;


/**
 * @author mbechler
 * 
 */
public interface CompleterFactory {

    /**
     * @param session
     * @return a new completer configured for this session
     */
    Completer createCompleter ( Session session );
}
