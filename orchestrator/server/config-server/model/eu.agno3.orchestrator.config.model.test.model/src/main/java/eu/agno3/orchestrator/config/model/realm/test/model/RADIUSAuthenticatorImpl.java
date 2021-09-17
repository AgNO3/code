/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.test.model;


import javax.persistence.Basic;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@Entity
@Table ( )
@Inheritance ( strategy = InheritanceType.JOINED )
@PersistenceUnit ( unitName = "config" )
@MapAs ( RADIUSAuthenticator.class )
@DiscriminatorValue ( "auth_radius" )
public class RADIUSAuthenticatorImpl extends AbstractAuthenticator<RADIUSAuthenticator> implements RADIUSAuthenticatorMutable, AuthenticatorMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 1136240263460444654L;
    private String radius1;
    private String radius2;

    private String nasIp;
    private String nasSecret;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<RADIUSAuthenticator> getType () {
        return RADIUSAuthenticator.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.test.model.RADIUSAuthenticatorMutable#getRadius1()
     */
    @Override
    @Basic
    public String getRadius1 () {
        return this.radius1;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.test.model.RADIUSAuthenticatorMutable#setRadius1(java.lang.String)
     */
    @Override
    public void setRadius1 ( String radius1 ) {
        this.radius1 = radius1;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.test.model.RADIUSAuthenticatorMutable#getRadius2()
     */
    @Override
    @Basic
    public String getRadius2 () {
        return this.radius2;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.test.model.RADIUSAuthenticatorMutable#setRadius2(java.lang.String)
     */
    @Override
    public void setRadius2 ( String radius2 ) {
        this.radius2 = radius2;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.test.model.RADIUSAuthenticatorMutable#getNasIp()
     */
    @Override
    @Basic
    public String getNasIp () {
        return this.nasIp;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.test.model.RADIUSAuthenticatorMutable#setNasIp(java.lang.String)
     */
    @Override
    public void setNasIp ( String nasIp ) {
        this.nasIp = nasIp;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.test.model.RADIUSAuthenticatorMutable#getNasSecret()
     */
    @Override
    @Basic
    public String getNasSecret () {
        return this.nasSecret;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.test.model.RADIUSAuthenticatorMutable#setNasSecret(java.lang.String)
     */
    @Override
    public void setNasSecret ( String nasSecret ) {
        this.nasSecret = nasSecret;
    }

}
