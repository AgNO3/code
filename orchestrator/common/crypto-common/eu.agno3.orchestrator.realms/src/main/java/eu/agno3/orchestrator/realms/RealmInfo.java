/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.orchestrator.realms;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author mbechler
 *
 */
public class RealmInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5371084454418397358L;
    private String realmName;
    private RealmType type;
    private boolean joined;
    private List<KeytabInfo> keytabs = new ArrayList<>();


    /**
     * 
     */
    public RealmInfo () {}


    /**
     * 
     * @return the realm name
     */
    public String getRealmName () {
        return this.realmName;
    }


    /**
     * @param realmName
     *            the realmName to set
     */
    public void setRealmName ( String realmName ) {
        this.realmName = realmName;
    }


    /**
     * @return the joined
     */
    public boolean getJoined () {
        return this.joined;
    }


    /**
     * @param joined
     *            the joined to set
     */
    public void setJoined ( boolean joined ) {
        this.joined = joined;
    }


    /**
     * 
     * @return the realm type
     */
    public RealmType getType () {
        return this.type;
    }


    /**
     * @param type
     *            the type to set
     */
    public void setType ( RealmType type ) {
        this.type = type;
    }


    /**
     * 
     * @return the keytab info
     */
    public List<KeytabInfo> getKeytabs () {
        return this.keytabs;
    }


    /**
     * @param keytabs
     *            the keytabs to set
     */
    public void setKeytabs ( List<KeytabInfo> keytabs ) {
        this.keytabs = keytabs;
    }
}
