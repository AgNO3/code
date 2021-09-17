/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.02.2016 by mbechler
 */
package eu.agno3.orchestrator.server.security.impl;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
import javax.persistence.PersistenceUnit;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlTransient;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "orchestrator" )
@XmlTransient
@Entity
public class PreferenceStorage {

    private UUID userid;
    private long version;
    private Map<String, String> preferences = new HashMap<>();


    /**
     * @return the user id
     */
    @Id
    @Column ( length = 16 )
    public UUID getUserId () {
        return this.userid;
    }


    /**
     * @param id
     *            the id to set
     */
    public void setUserId ( UUID id ) {
        this.userid = id;
    }


    /**
     * @return the object version
     */
    @Version
    public long getVersion () {
        return this.version;
    }


    /**
     * @param version
     *            the version to set
     */
    public void setVersion ( long version ) {
        this.version = version;
    }


    /**
     * @return the user preferences
     */
    @ElementCollection ( fetch = FetchType.LAZY )
    @JoinTable ( name = "user_preferences", joinColumns = @JoinColumn ( name = "id" ) )
    @MapKeyColumn ( name = "prefkey", length = 64 )
    @Column ( name = "value" )
    public Map<String, String> getPreferences () {
        return this.preferences;
    }


    /**
     * @param preferences
     *            the preferences to set
     */
    public void setPreferences ( Map<String, String> preferences ) {
        this.preferences = preferences;
    }
}
