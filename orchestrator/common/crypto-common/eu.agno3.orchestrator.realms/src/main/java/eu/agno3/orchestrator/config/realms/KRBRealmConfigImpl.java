/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.realms.RealmType;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@Entity
@Table ( name = "config_realms_krbrealm" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "re_krb" )
@MapAs ( KRBRealmConfig.class )
public class KRBRealmConfigImpl extends AbstractRealmConfigImpl<KRBRealmConfig> implements KRBRealmConfig, KRBRealmConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -4471391510552312162L;
    private RealmType realmType;

    private String adminServer;
    private String kpasswdServer;
    private List<String> kdcs = new ArrayList<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<KRBRealmConfig> getType () {
        return KRBRealmConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.RealmConfig#getRealmType()
     */
    @Override
    @Enumerated ( EnumType.STRING )
    public RealmType getRealmType () {
        return this.realmType;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KRBRealmConfigMutable#setRealmType(eu.agno3.orchestrator.realms.RealmType)
     */
    @Override
    public void setRealmType ( RealmType realmType ) {
        this.realmType = realmType;
    }


    /**
     * @return the adminServer
     */
    @Override
    public String getAdminServer () {
        return this.adminServer;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KRBRealmConfigMutable#setAdminServer(java.lang.String)
     */
    @Override
    public void setAdminServer ( String adminServer ) {
        this.adminServer = adminServer;
    }


    /**
     * @return the kpasswdServer
     */
    @Override
    public String getKpasswdServer () {
        return this.kpasswdServer;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KRBRealmConfigMutable#setKpasswdServer(java.lang.String)
     */
    @Override
    public void setKpasswdServer ( String kpasswdServer ) {
        this.kpasswdServer = kpasswdServer;
    }


    /**
     * @return the kdcs
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_realms_krbrealm_kdcs" )
    public List<String> getKdcs () {
        return this.kdcs;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.KRBRealmConfigMutable#setKdcs(java.util.List)
     */
    @Override
    public void setKdcs ( List<String> kdcs ) {
        this.kdcs = kdcs;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.AbstractRealmConfigImpl#doClone(eu.agno3.orchestrator.config.realms.RealmConfig)
     */
    @Override
    protected void doClone ( RealmConfig obj ) {
        if ( ! ( obj instanceof KRBRealmConfig ) ) {
            throw new IllegalArgumentException();
        }

        KRBRealmConfig o = (KRBRealmConfig) obj;
        this.realmType = o.getRealmType();
        this.adminServer = o.getAdminServer();
        this.kdcs = new ArrayList<>(o.getKdcs());
        this.kpasswdServer = o.getKpasswdServer();
    }
}
