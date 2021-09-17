/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.05.2014 by mbechler
 */
package eu.agno3.runtime.cdi;


import org.osgi.framework.Bundle;


/**
 * @author mbechler
 * 
 */
public interface CDIMetadataCache {

    /**
     * @param b
     * @param cached
     */
    void putCache ( Bundle b, CDICacheEntry cached );


    /**
     * @param b
     * @return a cached entry or null if none available
     */
    CDICacheEntry getCached ( Bundle b );

}
