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
public enum TermsApplyType {

    /**
     * Apply for both authenticated and unauthenticated
     */
    ALL,

    /**
     * Apply only for unauthenticated
     */
    ONLY,

    /**
     * Do not apply for authenticated
     */
    EXCLUDE

}
