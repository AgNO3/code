/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.terms;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( TermsConfiguration.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_terms" )
@Audited
@DiscriminatorValue ( "termsc" )
public class TermsConfigurationImpl extends AbstractConfigurationObject<TermsConfiguration> implements TermsConfiguration, TermsConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 6190910802634796332L;

    private Set<TermsDefinition> terms = new HashSet<>();

    private String termsLibrary;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.ConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<TermsConfiguration> getType () {
        return TermsConfiguration.class;
    }


    @Override
    @ManyToMany ( fetch = FetchType.LAZY, targetEntity = TermsDefinitionImpl.class )
    public Set<TermsDefinition> getTerms () {
        return this.terms;
    }


    /**
     * @param terms
     *            the terms to set
     */
    @Override
    public void setTerms ( Set<TermsDefinition> terms ) {
        this.terms = terms;
    }


    /**
     * @return the termsLibray
     */
    @Override
    public String getTermsLibrary () {
        return this.termsLibrary;
    }


    /**
     * @param termsLibrary
     *            the termsLibray to set
     */
    @Override
    public void setTermsLibrary ( String termsLibrary ) {
        this.termsLibrary = termsLibrary;
    }
}
