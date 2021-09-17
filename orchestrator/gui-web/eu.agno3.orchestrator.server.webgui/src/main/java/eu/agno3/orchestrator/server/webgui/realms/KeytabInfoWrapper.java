/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.04.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.realms;


import java.io.Serializable;

import eu.agno3.orchestrator.realms.KeytabInfo;
import eu.agno3.orchestrator.realms.RealmInfo;


/**
 * @author mbechler
 *
 */
public class KeytabInfoWrapper implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 9019232426530692353L;
    private RealmInfo realm;
    private KeytabInfo keytab;


    /**
     * @return the keytab
     */
    public KeytabInfo getKeytab () {
        return this.keytab;
    }


    /**
     * @param keytab
     *            the keytab to set
     */
    public void setKeytab ( KeytabInfo keytab ) {
        this.keytab = keytab;
    }


    /**
     * @return the realm
     */
    public RealmInfo getRealm () {
        return this.realm;
    }


    /**
     * @param realm
     *            the realm to set
     */
    public void setRealm ( RealmInfo realm ) {
        this.realm = realm;
    }

}
