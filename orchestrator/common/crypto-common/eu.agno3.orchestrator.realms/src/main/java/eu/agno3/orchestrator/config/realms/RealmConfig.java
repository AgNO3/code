/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.base.config.ObjectName;
import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.realms.RealmType;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( AbstractRealmConfigObjectTypeDescriptor.OBJECT_TYPE )
public interface RealmConfig extends ConfigurationObject {

    /**
     * 
     * @return the realm type
     */
    RealmType getRealmType ();


    /**
     * 
     * @return the realm name
     */
    @ObjectName
    String getRealmName ();


    /**
     * 
     * @return cross realm trust paths
     */
    @Valid
    @ReferencedObject
    Set<CAPathEntry> getCaPaths ();


    /**
     * 
     * @return additional domain mappings
     */
    List<String> getDomainMappings ();


    /**
     * 
     * @return override the local hostname for usage in this realm
     */
    String getOverrideLocalHostname ();


    /**
     * @return import keytabs
     */
    @Valid
    @ReferencedObject
    Set<KeytabEntry> getImportKeytabs ();


    /**
     * 
     * @return the maximum ticket lifetime, not enforced - but used for determining when service keys can be cleaned up
     */
    Duration getMaximumTicketLifetime ();


    /**
     * @return the interval in which service keys should be rekeyed
     */
    Duration getServiceRekeyInterval ();


    /**
     * @return whether to rekey services
     */
    Boolean getRekeyServices ();


    /**
     * @return the applied security level
     */
    KerberosSecurityLevel getSecurityLevel ();

}
