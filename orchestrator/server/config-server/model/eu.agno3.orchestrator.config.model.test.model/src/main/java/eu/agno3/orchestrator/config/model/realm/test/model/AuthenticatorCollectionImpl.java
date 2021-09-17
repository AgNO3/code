/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.test.model;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@Entity
@Table ( )
@Inheritance ( strategy = InheritanceType.JOINED )
@PersistenceUnit ( unitName = "config" )
@MapAs ( AuthenticatorCollection.class )
@DiscriminatorValue ( "authcol" )
public class AuthenticatorCollectionImpl extends AbstractConfigurationObject<AuthenticatorCollection> implements AuthenticatorCollectionMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 4832088208048257290L;
    List<AuthenticatorMutable> authenticators = new ArrayList<>();


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<AuthenticatorCollection> getType () {
        return AuthenticatorCollection.class;
    }


    /**
     * @return the authenticators
     */
    @Override
    @ManyToMany ( cascade = {
        CascadeType.PERSIST
    }, targetEntity = AbstractAuthenticator.class )
    @JoinTable ( name = "authenticators_collection", joinColumns = @JoinColumn ( name = "id" ) )
    @OrderColumn ( name = "idx", nullable = false )
    public List<AuthenticatorMutable> getAuthenticators () {
        return this.authenticators;
    }


    /**
     * @param authenticators
     *            the authenticators to set
     */
    @Override
    public void setAuthenticators ( List<AuthenticatorMutable> authenticators ) {
        this.authenticators = authenticators;
    }

}
