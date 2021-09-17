/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 28, 2017 by mbechler
 */
package eu.agno3.runtime.redis.internal;


import java.util.Collections;
import java.util.Dictionary;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.ReplicatedServersConfig;

import eu.agno3.runtime.redis.RedisClientException;
import eu.agno3.runtime.redis.RedisClientProvider;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = RedisClientProvider.class, configurationPid = "redis", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class RedisClientProviderImpl implements RedisClientProvider {

    private static final Logger log = Logger.getLogger(RedisClientProviderImpl.class);
    private RedissonClient client;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {

        Config config = new Config();

        Dictionary<String, Object> props = ctx.getProperties();
        String mode = ConfigUtil.parseString(
            props,
            "mode", //$NON-NLS-1$
            "single"); //$NON-NLS-1$

        switch ( mode ) {
        case "single": //$NON-NLS-1$
            config.useSingleServer().setAddress(ConfigUtil.parseString(props, "node", null)); //$NON-NLS-1$
            break;
        case "cluster": //$NON-NLS-1$
            ClusterServersConfig csc = config.useClusterServers();
            for ( String node : ConfigUtil.parseStringCollection(props, "nodes", Collections.EMPTY_LIST) ) { //$NON-NLS-1$
                csc.addNodeAddress(node);
            }
            break;
        case "replicated": //$NON-NLS-1$
            ReplicatedServersConfig rsc = config.useReplicatedServers();
            for ( String node : ConfigUtil.parseStringCollection(props, "nodes", Collections.EMPTY_LIST) ) { //$NON-NLS-1$
                rsc.addNodeAddress(node);
            }
            break;
        default:
            log.error("Invalid client mode" + mode); //$NON-NLS-1$
            return;
        }

        this.client = Redisson.create(config);
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        RedissonClient cl = this.client;
        this.client = null;
        if ( cl != null ) {
            cl.shutdown();
        }
    }


    @Override
    public RedissonClient getClient () throws RedisClientException {
        RedissonClient cl = this.client;
        if ( cl == null ) {
            throw new RedisClientException("Client unavailable"); //$NON-NLS-1$
        }
        return cl;
    }
}
