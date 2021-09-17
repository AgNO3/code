/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.04.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.realms;


import java.io.Serializable;

import eu.agno3.orchestrator.realms.KeyInfo;


/**
 * @author mbechler
 *
 */
public class KeyInfoWrapper implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3625122522475520927L;
    private KeytabInfoWrapper keytab;
    private KeyInfo key;


    /**
     * @return the key
     */
    public KeyInfo getKey () {
        return this.key;
    }


    /**
     * @param key
     *            the key to set
     */
    public void setKey ( KeyInfo key ) {
        this.key = key;
    }


    /**
     * @return the keytab
     */
    public KeytabInfoWrapper getKeytab () {
        return this.keytab;
    }


    /**
     * @param keytab
     *            the keytab to set
     */
    public void setKeytab ( KeytabInfoWrapper keytab ) {
        this.keytab = keytab;
    }
}
