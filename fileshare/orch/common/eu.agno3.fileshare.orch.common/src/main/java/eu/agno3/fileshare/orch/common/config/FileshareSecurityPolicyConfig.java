/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( FileshareSecurityPolicyConfigObjectTypeDescriptor.TYPE_NAME )
public interface FileshareSecurityPolicyConfig extends ConfigurationObject {

    /**
     * 
     * @return the default entropy bits for generated passwords
     */
    Integer getDefaultSharePasswordBits ();


    /**
     * 
     * @return rules to assign labels to users
     */
    @ReferencedObject
    @Valid
    List<FileshareUserLabelRule> getUserLabelRules ();


    /**
     * 
     * @return the label to assign to user root directories, if not specfied defaults to the user label otherwise the
     *         default entity label is used
     */
    String getDefaultRootLabel ();


    /**
     * 
     * @return the default label for created entities, if no other rules apply
     */
    String getDefaultEntityLabel ();


    /**
     * 
     * @return the policies
     */
    @ReferencedObject
    @Valid
    Set<FileshareSecurityPolicy> getPolicies ();

}
