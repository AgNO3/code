/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.01.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.util.UUID;


/**
 * @author mbechler
 *
 */
public interface SubjectInfo {

    /**
     * @return the subject id
     */
    UUID getId ();


    /**
     * @return the subject type
     */
    SubjectType getType ();


    /**
     * 
     * @return the subjects realm
     */
    String getRealm ();


    /**
     * 
     * @return the name source
     */
    NameSource getNameSource ();

}
