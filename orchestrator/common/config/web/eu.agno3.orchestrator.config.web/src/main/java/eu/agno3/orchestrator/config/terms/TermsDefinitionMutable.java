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

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( TermsDefinition.class )
public interface TermsDefinitionMutable extends TermsDefinition {

    /**
     * 
     * @param descriptions
     */
    void setDescriptions ( Map<Locale, String> descriptions );


    /**
     * 
     * @param titles
     */
    void setTitles ( Map<Locale, String> titles );


    /**
     * 
     * @param roles
     */
    void setExcludeRoles ( Set<String> roles );


    /**
     * 
     * @param roles
     */
    void setIncludeRoles ( Set<String> roles );


    /**
     * 
     * @param applyType
     */
    void setApplyType ( TermsApplyType applyType );


    /**
     * @param termsId
     */
    void setTermsId ( String termsId );


    /**
     * @param priority
     */
    void setPriority ( Integer priority );


    /**
     * @param persistAcceptance
     */
    void setPersistAcceptance ( Boolean persistAcceptance );


    /**
     * @param updated
     */
    void setUpdated ( DateTime updated );

}
