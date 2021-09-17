/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.09.2015 by mbechler
 */
package eu.agno3.runtime.update;


import eu.agno3.runtime.crypto.tls.TrustConfiguration;


/**
 * @author mbechler
 *
 */
public interface UpdateTrustConfiguration extends TrustConfiguration {

    /**
     * @return the fallback trust store
     */
    UpdateTrustConfiguration getFallback ();


    /**
     * @return whether a delegate trust store is available
     */
    boolean hasDelegate ();

}
