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
@MapAs ( ADAuthenticator.class )
@DiscriminatorValue ( "auth_ad" )
public class ADAuthenticatorImpl extends AbstractAuthenticator<ADAuthenticator> implements ADAuthenticatorMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 2845142708630456818L;
    private String domain;
    private String dc1;
    private String dc2;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<ADAuthenticator> getType () {
        return ADAuthenticator.class;
    }


    /**
     * @return the domain
     */
    @Override
    @Basic
    public String getDomain () {
        return this.domain;
    }


    /**
     * @param domain
     *            the domain to set
     */
    @Override
    public void setDomain ( String domain ) {
        this.domain = domain;
    }


    /**
     * @return the dc1
     */
    @Override
    @Basic
    public String getDc1 () {
        return this.dc1;
    }


    /**
     * @param dc1
     *            the dc1 to set
     */
    @Override
    public void setDc1 ( String dc1 ) {
        this.dc1 = dc1;
    }


    /**
     * @return the dc2
     */
    @Override
    @Basic
    public String getDc2 () {
        return this.dc2;
    }


    /**
     * @param dc2
     *            the dc2 to set
     */
    @Override
    public void setDc2 ( String dc2 ) {
        this.dc2 = dc2;
    }

}
