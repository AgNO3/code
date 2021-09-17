/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig;


import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import eu.agno3.orchestrator.config.crypto.keystore.KeystoresConfig;
import eu.agno3.orchestrator.config.crypto.truststore.TruststoresConfig;
import eu.agno3.orchestrator.config.hostconfig.datetime.DateTimeConfiguration;
import eu.agno3.orchestrator.config.hostconfig.mailing.MailingConfiguration;
import eu.agno3.orchestrator.config.hostconfig.network.NetworkConfiguration;
import eu.agno3.orchestrator.config.hostconfig.resolver.ResolverConfiguration;
import eu.agno3.orchestrator.config.hostconfig.storage.StorageConfiguration;
import eu.agno3.orchestrator.config.hostconfig.system.SystemConfiguration;
import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.Instance;
import eu.agno3.orchestrator.config.model.validation.Materialized;
import eu.agno3.orchestrator.config.realms.RealmsConfig;


/**
 * Top leven host configuration entity
 * 
 * @author mbechler
 * 
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:hostconfig" )
public interface HostConfiguration extends ConfigurationInstance {

    /**
     * @return the host identification aspect
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    HostIdentification getHostIdentification ();


    /**
     * @return basic system configuration aspect
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    SystemConfiguration getSystemConfiguration ();


    /**
     * 
     * @return date/time/timezone configuration aspect
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    DateTimeConfiguration getDateTimeConfiguration ();


    /**
     * 
     * @return network configuration aspect
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    NetworkConfiguration getNetworkConfiguration ();


    /**
     * 
     * @return resolver configuration aspect
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    ResolverConfiguration getResolverConfiguration ();


    /**
     * 
     * @return trust configuration aspect
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    TruststoresConfig getTrustConfiguration ();


    /**
     * 
     * @return trust configuration aspect
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    KeystoresConfig getKeystoreConfiguration ();


    /**
     * 
     * @return the auth realms configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    RealmsConfig getRealmsConfiguration ();


    /**
     * 
     * @return storage configuration aspect
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    StorageConfiguration getStorageConfiguration ();


    /**
     * @return mailing configuration aspect
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    MailingConfiguration getMailingConfiguration ();

}
