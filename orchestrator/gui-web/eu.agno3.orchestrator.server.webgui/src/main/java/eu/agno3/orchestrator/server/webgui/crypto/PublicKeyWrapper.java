/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.10.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto;


import java.security.PublicKey;

import eu.agno3.orchestrator.types.entities.crypto.PublicKeyEntry;


/**
 * @author mbechler
 *
 */
public class PublicKeyWrapper {

    private PublicKeyEntry pe;


    public PublicKeyWrapper ( PublicKeyEntry pe ) {
        this.pe = pe;
    }


    public PublicKey getValue () {
        return this.pe.getPublicKey();
    }


    public void setValue ( PublicKey pk ) {
        this.pe.setPublicKey(pk);
    }


    public String getComment () {
        return this.pe.getComment();
    }


    public void setComment ( String comment ) {
        this.pe.setComment(comment);
    }
}
