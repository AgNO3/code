/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2015 by mbechler
 */
package eu.agno3.runtime.configloader;


/**
 * @author mbechler
 *
 */
public interface ReconfigurationListener {

    /**
     * Called when reconfiguration starts
     */
    void startReconfigure ();


    /**
     * Called when reconfiguration is done
     */
    void finishReconfigure ();
}
