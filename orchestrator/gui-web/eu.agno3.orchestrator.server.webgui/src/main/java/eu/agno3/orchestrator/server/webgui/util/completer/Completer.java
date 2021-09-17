/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.util.completer;


import java.util.List;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public interface Completer <T> {

    /**
     * 
     * @param query
     * @return completion results
     */
    List<T> complete ( String query );
}
