/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 17, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.terms;


import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:terms:def" )
public interface TermsDefinition extends ConfigurationObject {

    /**
     * @return identifier for the terms
     */
    String getTermsId ();


    /**
     * @return proprity to use for ordering multiple terms (lower is prompted first)
     */
    Integer getPriority ();


    /**
     * @return when to apply the terms
     */
    TermsApplyType getApplyType ();


    /**
     * @return description strings
     */
    Map<Locale, String> getDescriptions ();


    /**
     * @return title strings
     */
    Map<Locale, String> getTitles ();


    /**
     * @return roles to exclude
     */
    Set<String> getExcludeRoles ();


    /**
     * @return roles to include
     */
    Set<String> getIncludeRoles ();


    /**
     * @return whether terms acceptance is persisted (otherwise acceptance is needed for every new session)
     */
    Boolean getPersistAcceptance ();


    /**
     * @return configurable time that invalidates any acceptance before this date
     */
    DateTime getUpdated ();

}
