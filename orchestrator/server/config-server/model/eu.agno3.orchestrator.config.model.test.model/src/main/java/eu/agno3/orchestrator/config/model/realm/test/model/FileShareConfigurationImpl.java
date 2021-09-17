/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.test.model;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationInstance;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@Entity
@Table ( )
@Inheritance ( strategy = InheritanceType.JOINED )
@PersistenceUnit ( unitName = "config" )
@MapAs ( FileShareConfiguration.class )
@DiscriminatorValue ( "fs" )
public class FileShareConfigurationImpl extends AbstractConfigurationInstance<FileShareConfiguration> implements FileShareConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -2255990579848561958L;
    private AuthenticatorCollectionMutable authenticators;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileShareConfiguration> getType () {
        return FileShareConfiguration.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.test.model.FileShareConfigurationMutable#getAuthenticators()
     */
    @Override
    @ManyToOne ( targetEntity = AuthenticatorCollectionImpl.class )
    public AuthenticatorCollectionMutable getAuthenticators () {
        return this.authenticators;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.test.model.FileShareConfigurationMutable#setAuthenticators(eu.agno3.orchestrator.config.model.realm.test.model.AuthenticatorCollectionMutable)
     */
    @Override
    public void setAuthenticators ( AuthenticatorCollectionMutable authenticators ) {
        this.authenticators = authenticators;
    }

}
