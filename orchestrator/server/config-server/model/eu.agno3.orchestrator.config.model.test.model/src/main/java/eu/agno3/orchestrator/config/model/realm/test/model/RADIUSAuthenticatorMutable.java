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
public interface RADIUSAuthenticatorMutable extends RADIUSAuthenticator {

    /**
     * @param radius1
     *            the radius1 to set
     */
    void setRadius1 ( String radius1 );


    /**
     * @param radius2
     *            the radius2 to set
     */
    void setRadius2 ( String radius2 );


    /**
     * @param nasIp
     *            the nasIp to set
     */
    void setNasIp ( String nasIp );


    /**
     * @param nasSecret
     *            the nasSecret to set
     */
    void setNasSecret ( String nasSecret );

}