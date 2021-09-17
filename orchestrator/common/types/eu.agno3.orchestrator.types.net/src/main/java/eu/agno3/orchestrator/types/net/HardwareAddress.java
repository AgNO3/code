/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.03.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net;


import java.io.Serializable;


/**
 * @author mbechler
 * 
 */
public interface HardwareAddress extends Serializable, java.lang.Cloneable {

    /**
     * 
     * 
     * @return the raw address (bytes are returned as unsigned short values)
     */
    short[] getAddress ();


    /**
     * @return a canonical string representation
     */
    String getCanonicalForm ();
}
