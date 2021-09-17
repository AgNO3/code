/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 17, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.terms;


/**
 * @author mbechler
 *
 */
public class ObjectFactory {

    /**
     * 
     * @return default impl
     */
    public TermsDefinition makeTermsDefinition () {
        return new TermsDefinitionImpl();
    }


    /**
     * 
     * @return default impl
     */
    public TermsConfiguration makeTermsConfiguration () {
        return new TermsConfigurationImpl();
    }
}
