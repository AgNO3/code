/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 14, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.validation;


/**
 * @author mbechler
 *
 */
public interface ConfigTestResult {

    /**
     * @param st
     * @return this
     */
    ConfigTestResult state ( ConfigTestState st );


    /**
     * @param objectType
     * @return this
     */
    ConfigTestResult withType ( String objectType );


    /**
     * @return this
     */
    ConfigTestResultImpl get ();


    /**
     * @param e
     * @return this
     */
    ConfigTestResult addEntry ( ConfigTestResultEntry e );


    /**
     * @param sev
     * @param msgTemplate
     * @param args
     * @return this
     */
    ConfigTestResult addEntry ( ConfigTestResultSeverity sev, String msgTemplate, String... args );


    /**
     * @param msgTemplate
     * @param args
     * @return this
     */
    ConfigTestResult info ( String msgTemplate, String... args );


    /**
     * @param msgTemplate
     * @param args
     * @return this
     */
    ConfigTestResult warn ( String msgTemplate, String... args );


    /**
     * @param msgTemplate
     * @param args
     * @return this
     */
    ConfigTestResult error ( String msgTemplate, String... args );

}
