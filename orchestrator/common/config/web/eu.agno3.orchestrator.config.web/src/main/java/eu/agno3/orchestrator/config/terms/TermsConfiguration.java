/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.terms;


import java.util.Set;

import javax.validation.Valid;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( TermsConfigurationObjectTypeDescriptor.OBJECT_TYPE )
public interface TermsConfiguration extends ConfigurationObject {

    /**
     * 
     * @return the configured authenticators
     */
    @ReferencedObject
    @Valid
    Set<TermsDefinition> getTerms ();


    /**
     * @return resource library to store terms
     */
    String getTermsLibrary ();

}
