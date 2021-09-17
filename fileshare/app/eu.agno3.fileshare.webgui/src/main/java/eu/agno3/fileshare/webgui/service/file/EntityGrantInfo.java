/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file;


import java.util.List;

import eu.agno3.fileshare.model.Grant;


/**
 * @author mbechler
 *
 */
public interface EntityGrantInfo {

    /**
     * @return the firstGrants
     */
    List<Grant> getFirstGrants ();


    /**
     * @return the numGrants
     */
    int getNumGrants ();


    /**
     * 
     * @return the number of grants that exceed the display limit
     */
    int getGrantsExceedingLimit ();


    /**
     * 
     */
    void refresh ();

}