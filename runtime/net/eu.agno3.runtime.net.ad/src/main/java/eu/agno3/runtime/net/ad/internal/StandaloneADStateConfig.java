/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 3, 2017 by mbechler
 */
package eu.agno3.runtime.net.ad.internal;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = StandaloneADStateConfig.class, configurationPid = "ad.state", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class StandaloneADStateConfig {

    private static final Logger log = Logger.getLogger(StandaloneADStateConfig.class);

    private Path adStateDirectory;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        String stateDir = ConfigUtil.parseString(ctx.getProperties(), "stateDir", null); //$NON-NLS-1$
        if ( StringUtils.isBlank(stateDir) ) {
            log.error("No stateDir configured"); //$NON-NLS-1$
            return;
        }

        Path statePath = Paths.get(stateDir.trim());

        if ( !Files.isDirectory(statePath) ) {
            try {
                Files.createDirectories(statePath, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"))); //$NON-NLS-1$
            }
            catch ( IOException e ) {
                log.error("Failed to create AD state directory", e); //$NON-NLS-1$
                return;
            }
        }

        if ( !Files.isWritable(statePath) ) {
            log.error("Cannot write state directory " + statePath); //$NON-NLS-1$
        }

        this.adStateDirectory = statePath;
    }


    /**
     * @return the adStateDirectory
     */
    public Path getAdStateDirectory () {
        return this.adStateDirectory;
    }
}
