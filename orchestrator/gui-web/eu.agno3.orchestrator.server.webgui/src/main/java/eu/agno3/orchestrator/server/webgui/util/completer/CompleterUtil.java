/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.util.completer;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;


/**
 * @author mbechler
 *
 */
@Named ( "completerUtil" )
@ApplicationScoped
public class CompleterUtil {

    /**
     * 
     * @param toWrap
     * @return either the original completer, or an empty completer
     */
    @SuppressWarnings ( "unchecked" )
    public <T> Completer<T> wrap ( Completer<T> toWrap ) {
        if ( toWrap == null ) {
            return new EmptyCompleter();
        }

        return toWrap;
    }
}
