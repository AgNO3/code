/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.test.model;


import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;


/**
 * @author mbechler
 * @param <T>
 * 
 */
@Entity
@Table ( name = "authenticator" )
@Inheritance ( strategy = InheritanceType.JOINED )
@PersistenceUnit ( unitName = "config" )
public abstract class AbstractAuthenticator <T extends AuthenticatorMutable> extends AbstractConfigurationObject<T> implements AuthenticatorMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 5186809180325543971L;

}
