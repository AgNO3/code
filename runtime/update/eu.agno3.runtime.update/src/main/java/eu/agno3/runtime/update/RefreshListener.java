/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.12.2014 by mbechler
 */
package eu.agno3.runtime.update;


/**
 * @author mbechler
 *
 */
public interface RefreshListener {

    /**
     * @throws Exception
     * 
     */
    void bundlesRefreshed () throws Exception;


    /**
     * 
     */
    void startBundleUpdate ();


    /**
     * @throws Exception
     * 
     */
    void bundlesUpdated () throws Exception;


    /**
     * 
     */
    void bundlesStarted ();

}
