/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.09.2015 by mbechler
 */
package eu.agno3.runtime.update;


/**
 * @author mbechler
 *
 */
public interface UpdateManagerProvider {

    /**
     * @param cfg
     * @return an update manager with the given configuration
     * @throws UpdateException
     */
    UpdateManager getUpdateManager ( UpdateConfiguration cfg ) throws UpdateException;


    /**
     * @return an update manager for the local installation
     * @throws UpdateException
     */
    UpdateManager getLocalUpdateManager () throws UpdateException;

}
