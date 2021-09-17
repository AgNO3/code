/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.05.2015 by mbechler
 */
package eu.agno3.fileshare.service.config;


import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.TrustLevel;


/**
 * @author mbechler
 *
 */
public interface TrustLevelConfiguration {

    /**
     * 
     * @param level
     * @return trust level for the given id
     */
    TrustLevel getTrustLevel ( String level );


    /**
     * 
     * @param s
     * @return trust level applicable for the given subject
     */
    TrustLevel getTrustLevel ( Subject s );


    /**
     * @param mailAddress
     * @return the trust level to assign to the mail address
     */
    TrustLevel getMailTrustLevel ( String mailAddress );


    /**
     * @return the trust level to assign to links
     */
    TrustLevel getLinkTrustLevel ();


    /**
     * @param g
     * @return the trust level to assign to the group
     */
    TrustLevel getGroupTrustLevel ( Group g );
}
