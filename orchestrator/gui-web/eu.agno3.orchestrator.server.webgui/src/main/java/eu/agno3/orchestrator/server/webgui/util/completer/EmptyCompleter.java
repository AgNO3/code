/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.util.completer;


import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "rawtypes" )
@Named ( "emptyCompleter" )
@ApplicationScoped
public class EmptyCompleter implements Completer {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.util.completer.Completer#complete(java.lang.String)
     */
    @Override
    public List complete ( String query ) {
        return Collections.EMPTY_LIST;
    }
}
