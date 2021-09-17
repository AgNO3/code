/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 17, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.terms;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.MapKeyColumn;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( TermsDefinition.class )
@Entity
@Table ( name = "config_terms_def" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "termdefc" )
public class TermsDefinitionImpl extends AbstractConfigurationObject<TermsDefinition> implements TermsDefinition, TermsDefinitionMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -1556372291503212623L;

    private String termsId;

    private Integer priority;

    private TermsApplyType applyType;

    private Set<String> includeRoles = new HashSet<>();
    private Set<String> excludeRoles = new HashSet<>();

    private Map<Locale, String> titles = new HashMap<>();
    private Map<Locale, String> descriptions = new HashMap<>();

    private Boolean persistAcceptance;

    private DateTime updated;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<TermsDefinition> getType () {
        return TermsDefinition.class;
    }


    /**
     * @return the termsId
     */
    @Override
    public String getTermsId () {
        return this.termsId;
    }


    /**
     * @param termsId
     *            the termsId to set
     */
    @Override
    public void setTermsId ( String termsId ) {
        this.termsId = termsId;
    }


    /**
     * @return the priority
     */
    @Override
    public Integer getPriority () {
        return this.priority;
    }


    /**
     * @param priority
     *            the priority to set
     */
    @Override
    public void setPriority ( Integer priority ) {
        this.priority = priority;
    }


    /**
     * @return the applyType
     */
    @Override
    public TermsApplyType getApplyType () {
        return this.applyType;
    }


    /**
     * @param applyType
     *            the applyType to set
     */
    @Override
    public void setApplyType ( TermsApplyType applyType ) {
        this.applyType = applyType;
    }


    /**
     * @return the persistAcceptance
     */
    @Override
    public Boolean getPersistAcceptance () {
        return this.persistAcceptance;
    }


    /**
     * @param persistAcceptance
     *            the persistAcceptance to set
     */
    @Override
    public void setPersistAcceptance ( Boolean persistAcceptance ) {
        this.persistAcceptance = persistAcceptance;
    }


    /**
     * @return the updated
     */
    @Override
    public DateTime getUpdated () {
        return this.updated;
    }


    /**
     * @param updated
     *            the updated to set
     */
    @Override
    public void setUpdated ( DateTime updated ) {
        this.updated = updated;
    }


    /**
     * @return the permissions
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_terms_def_incroles" )
    public Set<String> getIncludeRoles () {
        return this.includeRoles;
    }


    /**
     * @param roles
     *            the roles to set
     */
    @Override
    public void setIncludeRoles ( Set<String> roles ) {
        this.includeRoles = roles;
    }


    /**
     * @return the permissions
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_terms_def_excroles" )
    public Set<String> getExcludeRoles () {
        return this.excludeRoles;
    }


    /**
     * @param roles
     *            the roles to set
     */
    @Override
    public void setExcludeRoles ( Set<String> roles ) {
        this.excludeRoles = roles;
    }


    /**
     * @return the titles
     */
    @Override
    @ElementCollection ( fetch = FetchType.EAGER )
    @MapKeyColumn ( name = "locale" )
    @Column ( name = "msg" )
    @CollectionTable ( name = "config_terms_def_title" )
    public Map<Locale, String> getTitles () {
        return this.titles;
    }


    /**
     * @param titles
     *            the titles to set
     */
    @Override
    public void setTitles ( Map<Locale, String> titles ) {
        this.titles = titles;
    }


    /**
     * @return the descriptions
     */
    @Override
    @ElementCollection ( fetch = FetchType.EAGER )
    @MapKeyColumn ( name = "locale" )
    @Column ( name = "msg" )
    @CollectionTable ( name = "config_terms_def_desc" )
    public Map<Locale, String> getDescriptions () {
        return this.descriptions;
    }


    /**
     * @param descriptions
     *            the descriptions to set
     */
    @Override
    public void setDescriptions ( Map<Locale, String> descriptions ) {
        this.descriptions = descriptions;
    }

}
