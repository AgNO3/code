/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;


/**
 * @author mbechler
 * @param <T>
 *
 */
@Entity
@PersistenceUnit ( unitName = "config" )
@Inheritance ( strategy = InheritanceType.JOINED )
@Table ( name = "config_auth_base" )
@Audited
@DiscriminatorValue ( "auth_base" )
public abstract class AbstractAuthenticatorConfigImpl <T extends AuthenticatorConfig> extends AbstractConfigurationObject<T> implements
        AuthenticatorConfig, AuthenticatorConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -6060902146309324736L;

    private String realm;


    /**
     * @return the realm
     */
    @Override
    public String getRealm () {
        return this.realm;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.auth.AuthenticatorConfigMutable#setRealm(java.lang.String)
     */
    @Override
    public void setRealm ( String realm ) {
        this.realm = realm;
    }


    /**
     * @param obj
     */
    public void doClone ( AuthenticatorConfig obj ) {
        this.realm = obj.getRealm();
    }

}
