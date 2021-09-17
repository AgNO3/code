/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 28, 2017 by mbechler
 */
package eu.agno3.runtime.redis;


import org.redisson.api.RedissonClient;


/**
 * @author mbechler
 *
 */
public interface RedisClientProvider {

    /**
     * @return a client instance
     * @throws RedisClientException
     */
    RedissonClient getClient () throws RedisClientException;

}
