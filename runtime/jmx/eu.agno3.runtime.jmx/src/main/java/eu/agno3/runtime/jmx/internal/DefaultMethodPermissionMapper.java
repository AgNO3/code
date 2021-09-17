/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.03.2017 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.util.HashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;

import org.apache.log4j.Logger;

import eu.agno3.runtime.jmx.JMXPermissions;
import eu.agno3.runtime.jmx.MethodEntry;
import eu.agno3.runtime.jmx.MethodPermissionMapper;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "nls" )
public class DefaultMethodPermissionMapper implements MethodPermissionMapper {

    private static final Logger log = Logger.getLogger(DefaultMethodPermissionMapper.class);

    private static final Map<MethodEntry, JMXPermissions> PERMISSION_MAP = new HashMap<>();


    static {
        try {
            PERMISSION_MAP.put(new MethodEntry("java.lang:type=Threading", "getThreadInfo", "long"), JMXPermissions.READ);
            PERMISSION_MAP.put(new MethodEntry("java.lang:type=Threading", "dumpAllThreads", "boolean", "boolean"), JMXPermissions.READ);
        }
        catch ( MalformedObjectNameException e ) {
            log.error("Error in method permission mapping", e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jmx.MethodPermissionMapper#map(eu.agno3.runtime.jmx.MethodEntry)
     */
    @Override
    public JMXPermissions map ( MethodEntry me ) {
        return PERMISSION_MAP.get(me);
    }
}
