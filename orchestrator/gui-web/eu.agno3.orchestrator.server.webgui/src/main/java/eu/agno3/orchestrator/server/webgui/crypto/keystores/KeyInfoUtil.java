/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jul 11, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto.keystores;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.crypto.keystore.KeyInfo;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "keyInfoUtil" )
public class KeyInfoUtil {

    /**
     * @param ki
     * @return key alias to display
     */
    public static String getDisplayKeyAlias ( KeyInfo ki ) {
        String keyAlias = ki.getKeyAlias();
        if ( keyAlias == null || StringUtils.isBlank(keyAlias) || keyAlias.charAt(0) == '_' ) {
            return StringUtils.EMPTY;
        }
        return keyAlias;
    }

}
