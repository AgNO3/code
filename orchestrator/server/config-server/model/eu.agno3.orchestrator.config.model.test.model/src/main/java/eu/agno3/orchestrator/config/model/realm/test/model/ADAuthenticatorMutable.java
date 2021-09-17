/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.test.model;


/**
 * @author mbechler
 * 
 */
public interface ADAuthenticatorMutable extends ADAuthenticator {

    /**
     * 
     * @param dc2
     */
    void setDc2 ( String dc2 );


    /**
     * 
     * @param dc1
     */
    void setDc1 ( String dc1 );


    /**
     * 
     * @param domain
     */
    void setDomain ( String domain );
}