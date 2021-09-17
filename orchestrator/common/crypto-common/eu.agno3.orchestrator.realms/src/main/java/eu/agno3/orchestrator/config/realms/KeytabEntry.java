/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import java.util.Set;

import javax.validation.Valid;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.realms.KeyData;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:realms:keytab" )
public interface KeytabEntry extends ConfigurationObject {

    /**
     * 
     * @return the keytab import entries
     */
    @Valid
    Set<KeyData> getKeyImportEntries ();


    /**
     * 
     * @return the keytab id
     */
    String getKeytabId ();

}
