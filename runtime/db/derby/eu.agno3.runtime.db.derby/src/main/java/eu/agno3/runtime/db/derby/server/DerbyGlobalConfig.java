/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.05.2014 by mbechler
 */
package eu.agno3.runtime.db.derby.server;


import java.util.Dictionary;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import eu.agno3.runtime.db.derby.DerbyConfiguration;
import eu.agno3.runtime.db.derby.DerbyLog;


/**
 * @author mbechler
 * 
 */
@Component ( service = DerbyGlobalConfig.class, configurationPid = DerbyGlobalConfig.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class DerbyGlobalConfig {

    private static final Logger log = Logger.getLogger(DerbyGlobalConfig.class);

    /**
     * Configuration PID
     */
    public static final String PID = "db.server.derby"; //$NON-NLS-1$


    @Activate
    protected void activate ( ComponentContext ctx ) {
        Dictionary<String, Object> globalProps = ctx.getProperties();

        log.debug("Setting global derby properties"); //$NON-NLS-1$

        if ( globalProps.get(DerbyConfiguration.SYSTEM_HOME_ATTR) != null ) {
            System.setProperty(DerbyConfigProperties.SYSTEM_HOME, (String) globalProps.get(DerbyConfiguration.SYSTEM_HOME_ATTR));
        }

        System.setProperty(DerbyConfigProperties.STREAM_ERROR_FIELD, DerbyLog.class.getName() + ".LOG"); //$NON-NLS-1$
    }

}
