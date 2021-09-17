/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.packagekit;


/**
 * @author mbechler
 *
 */
public interface PackageKitProgressListener {

    /**
     * @param status
     * @param percent
     */
    void haveProgress ( int status, float percent );

}
