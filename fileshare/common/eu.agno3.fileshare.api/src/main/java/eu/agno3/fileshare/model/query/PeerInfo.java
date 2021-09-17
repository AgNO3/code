/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.05.2015 by mbechler
 */
package eu.agno3.fileshare.model.query;


import java.util.UUID;


/**
 * @author mbechler
 *
 */
public interface PeerInfo {

    /**
     * @return the peer id
     */
    UUID getId ();


    /**
     * @return whether something has been shared by the peer
     */
    boolean haveSharedFrom ();


    /**
     * 
     * @return whether something has been share to the peer
     */
    boolean haveSharedTo ();

}
