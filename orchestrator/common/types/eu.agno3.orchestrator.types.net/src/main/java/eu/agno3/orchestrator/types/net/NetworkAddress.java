/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.03.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net;


import java.io.Serializable;


/**
 * @author mbechler
 * 
 */
public interface NetworkAddress extends Serializable {

    /**
     * @return the address as raw bytes
     */
    short[] getAddress ();


    /**
     * @return a canonical representation of this address
     */
    String getCanonicalForm ();


    /**
     * @return a readable form of this address
     */
    String getReadableForm ();


    /**
     * @return whether this is a loopback address
     */
    boolean isLoopback ();


    /**
     * @return whether this is a broadcast address
     */
    boolean isBroadcast ();


    /**
     * 
     * @return whether this is a anycast address
     */
    boolean isAnycast ();


    /**
     * @return whether this is multicast address
     */
    boolean isMulticast ();


    /**
     * @return whether this is a reserved address
     */
    boolean isReserved ();


    /**
     * 
     * @return whether this is a unspecified address
     */
    boolean isUnspecified ();


    /**
     * 
     * @return whether this is unicast address
     */
    boolean isUnicast ();


    /**
     * @return number of bits in this address type
     */
    int getBitSize ();

}
