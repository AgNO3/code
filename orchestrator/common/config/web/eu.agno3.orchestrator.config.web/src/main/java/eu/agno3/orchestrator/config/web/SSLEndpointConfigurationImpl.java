/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OrderColumn;
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

@MapAs ( WebEndpointConfig.class )
@Entity
@Table ( name = "config_web_sslendpoint" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "webc_sslendp" )
public class SSLEndpointConfigurationImpl extends AbstractConfigurationObject<SSLEndpointConfiguration> implements SSLEndpointConfiguration,
        SSLEndpointConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -1532925608438168560L;

    private String keystoreAlias;
    private String keyAlias;
    private SSLSecurityMode securityMode;
    private Set<String> customProtocols = new HashSet<>();
    private List<String> customCiphers = new ArrayList<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<SSLEndpointConfiguration> getType () {
        return SSLEndpointConfiguration.class;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.SSLEndpointConfiguration#getKeystoreAlias()
     */
    @Override
    public String getKeystoreAlias () {
        return this.keystoreAlias;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.SSLEndpointConfigurationMutable#setKeystoreAlias(java.lang.String)
     */
    @Override
    public void setKeystoreAlias ( String keystoreAlias ) {
        this.keystoreAlias = keystoreAlias;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.SSLEndpointConfiguration#getKeyAlias()
     */
    @Override
    public String getKeyAlias () {
        return this.keyAlias;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.SSLEndpointConfigurationMutable#setKeyAlias(java.lang.String)
     */
    @Override
    public void setKeyAlias ( String keyAlias ) {
        this.keyAlias = keyAlias;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.SSLEndpointConfiguration#getSecurityMode()
     */
    @Override
    @Enumerated ( EnumType.STRING )
    public SSLSecurityMode getSecurityMode () {
        return this.securityMode;
    }


    /**
     * @param securityMode
     *            the securityMode to set
     */
    @Override
    public void setSecurityMode ( SSLSecurityMode securityMode ) {
        this.securityMode = securityMode;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.SSLEndpointConfiguration#getCustomProtocols()
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_web_sslendpoint_protocols" )
    public Set<String> getCustomProtocols () {
        return this.customProtocols;
    }


    /**
     * @param customProtocols
     *            the customProtocols to set
     */
    @Override
    public void setCustomProtocols ( Set<String> customProtocols ) {
        this.customProtocols = customProtocols;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.SSLEndpointConfiguration#getCustomCiphers()
     */
    @Override
    @ElementCollection
    @OrderColumn ( name = "cipher_idx" )
    @CollectionTable ( name = "config_web_sslendpoint_ciphers" )
    public List<String> getCustomCiphers () {
        return this.customCiphers;
    }


    /**
     * @param customCiphers
     *            the customCiphers to set
     */
    @Override
    public void setCustomCiphers ( List<String> customCiphers ) {
        this.customCiphers = customCiphers;
    }

}
