/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.terms;


import java.util.Set;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( TermsConfiguration.class )
public interface TermsConfigurationMutable extends TermsConfiguration {

    /**
     * @param terms
     */
    void setTerms ( Set<TermsDefinition> terms );


    /**
     * @param termsLibrary
     */
    void setTermsLibrary ( String termsLibrary );

}
