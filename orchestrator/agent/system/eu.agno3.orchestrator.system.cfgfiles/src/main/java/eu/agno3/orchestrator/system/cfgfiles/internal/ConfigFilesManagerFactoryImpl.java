/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.06.2015 by mbechler
 */
package eu.agno3.orchestrator.system.cfgfiles.internal;


import java.io.File;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Set;

import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.SystemServiceType;
import eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManager;
import eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManagerFactory;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    ConfigFilesManagerFactory.class, SystemService.class
} )
@SystemServiceType ( ConfigFilesManagerFactory.class )
public class ConfigFilesManagerFactoryImpl implements ConfigFilesManagerFactory {

    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ConfigFilesManagerFactory#getForPath(java.io.File,
     *      java.nio.file.attribute.UserPrincipal, java.nio.file.attribute.GroupPrincipal, java.util.Set, java.util.Set)
     */
    @Override
    public ConfigFilesManager getForPath ( File p, UserPrincipal owner, GroupPrincipal group, Set<PosixFilePermission> filePerms,
            Set<PosixFilePermission> dirPerms ) {
        return new ConfigFilesManagerImpl(p, owner, group, filePerms, dirPerms);
    }

}
