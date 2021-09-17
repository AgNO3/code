/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.01.2015 by mbechler
 */
package eu.agno3.fileshare.model;


/**
 * @author mbechler
 *
 */
public interface GroupInfo extends SubjectInfo {

    /**
     * @return the group name
     */
    String getName ();


    /**
     * 
     * @return the group realm, if set
     */
    @Override
    String getRealm ();

}
