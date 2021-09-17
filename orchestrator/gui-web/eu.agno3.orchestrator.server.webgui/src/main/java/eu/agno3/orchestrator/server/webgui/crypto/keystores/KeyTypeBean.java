/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.11.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto.keystores;


import javax.inject.Named;

import eu.agno3.runtime.crypto.keystore.KeyType;


/**
 * @author mbechler
 *
 */
@Named ( "keyTypeBean" )
public class KeyTypeBean {

    public KeyType[] getKeyTypes () {
        return KeyType.values();
    }


    public String translateKeyType ( Object k ) {
        if ( ! ( k instanceof KeyType ) ) {
            return null;
        }

        KeyType kt = (KeyType) k;
        return kt.name();
    }

}
