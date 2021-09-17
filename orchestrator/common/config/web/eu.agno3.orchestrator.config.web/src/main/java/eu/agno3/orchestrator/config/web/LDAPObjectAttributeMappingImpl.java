/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
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
@MapAs ( LDAPObjectAttributeMapping.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_web_ldap_obj_attr" )
@Audited
@DiscriminatorValue ( "webc_ldapattr" )
public class LDAPObjectAttributeMappingImpl extends AbstractConfigurationObject<LDAPObjectAttributeMapping> implements LDAPObjectAttributeMapping,
        LDAPObjectAttributeMappingMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -6546740831314574326L;

    private String attributeId;
    private String attributeName;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<LDAPObjectAttributeMapping> getType () {
        return LDAPObjectAttributeMapping.class;
    }


    /**
     * @return the attributeId
     */
    @Override
    public String getAttributeId () {
        return this.attributeId;
    }


    /**
     * @param attributeId
     *            the attributeId to set
     */
    @Override
    public void setAttributeId ( String attributeId ) {
        this.attributeId = attributeId;
    }


    /**
     * @return the attributeName
     */
    @Override
    public String getAttributeName () {
        return this.attributeName;
    }


    /**
     * @param attributeName
     *            the attributeName to set
     */
    @Override
    public void setAttributeName ( String attributeName ) {
        this.attributeName = attributeName;
    }


    /**
     * 
     * @param e
     * @return cloned object
     */
    public static LDAPObjectAttributeMapping clone ( LDAPObjectAttributeMapping e ) {
        LDAPObjectAttributeMappingImpl cloned = new LDAPObjectAttributeMappingImpl();
        cloned.attributeId = e.getAttributeId();
        cloned.attributeName = e.getAttributeName();
        return cloned;
    }


    /**
     * 
     * @param e
     * @return cloned set
     */
    public static Set<LDAPObjectAttributeMapping> clone ( Set<LDAPObjectAttributeMapping> e ) {
        Set<LDAPObjectAttributeMapping> cloned = new HashSet<>();
        for ( LDAPObjectAttributeMapping l : e ) {
            cloned.add(clone(l));
        }
        return cloned;
    }
}
