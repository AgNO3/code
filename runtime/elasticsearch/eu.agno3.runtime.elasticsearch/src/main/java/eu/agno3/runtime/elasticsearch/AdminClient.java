/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 21, 2017 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


/**
 * @author mbechler
 *
 */
public interface AdminClient {

    /**
     * @return cluster admin client
     */
    ClusterAdminClient cluster ();


    /**
     * @return index admin client
     */
    IndexAdminClient indices ();

}
