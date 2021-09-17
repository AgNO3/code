/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.12.2014 by mbechler
 */
package eu.agno3.runtime.cdi.comet;


/**
 * @author mbechler
 *
 */
public interface CometPublisher {

    /**
     * @param path
     * @param o
     */
    void publish ( String path, Object o );

}
