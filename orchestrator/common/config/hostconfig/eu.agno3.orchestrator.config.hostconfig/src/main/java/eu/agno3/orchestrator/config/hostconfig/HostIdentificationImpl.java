/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig;


import javax.persistence.Basic;
import javax.persistence.Column;
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
@MapAs ( HostIdentification.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_hostconfig_identity" )
@Audited
@DiscriminatorValue ( "hc_id" )
public class HostIdentificationImpl extends AbstractConfigurationObject<HostIdentification> implements HostIdentification, HostIdentificationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -4734162889218308610L;
    private String hostName;
    private String domainName;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<HostIdentification> getType () {
        return HostIdentification.class;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.HostIdentification#getHostName()
     */
    @Override
    @Basic
    @Column ( nullable = true )
    public String getHostName () {
        return this.hostName;
    }


    /**
     * @param hostname
     *            the hostname to set
     */
    @Override
    public void setHostName ( String hostname ) {
        this.hostName = hostname;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.HostIdentification#getDomainName()
     */
    @Override
    @Basic
    @Column ( nullable = true )
    public String getDomainName () {
        return this.domainName;
    }


    /**
     * @param domainName
     *            the domainName to set
     */
    @Override
    public void setDomainName ( String domainName ) {
        this.domainName = domainName;
    }
}
