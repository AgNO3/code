/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.network.java.impl;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.system.info.SystemInformationProvider;
import eu.agno3.orchestrator.system.info.network.NetworkInformation;
import eu.agno3.orchestrator.system.info.network.NetworkInformationProvider;
import eu.agno3.orchestrator.system.info.network.NetworkInterface;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    NetworkInformationProvider.class, SystemInformationProvider.class
} )
public class JavaNetworkInformationProvider implements NetworkInformationProvider {

    private static final Logger log = Logger.getLogger(JavaNetworkInformationProvider.class);


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInformationProvider#getInformation()
     */
    @Override
    public NetworkInformation getInformation () {
        JavaNetworkInformation jni = new JavaNetworkInformation();
        enhance(jni);
        return jni;
    }


    /**
     * @param jni
     */
    private static void enhance ( JavaNetworkInformation jni ) {
        Map<String, String> aliases = getAliases();
        for ( NetworkInterface ni : jni.getNetworkInterfaces() ) {
            String alias = aliases.get(ni.getName());
            if ( alias != null ) {
                jni.addAlias(ni.getName(), alias);
            }
        }
    }


    /**
     * @return interface aliases
     */
    private static Map<String, String> getAliases () {
        Map<String, String> aliases = new HashMap<>();
        try {
            Path ifstate = Paths.get("/run/network/ifstate"); //$NON-NLS-1$
            if ( Files.exists(ifstate) && Files.isReadable(ifstate) ) {
                for ( String line : Files.readAllLines(ifstate, StandardCharsets.US_ASCII) ) {
                    line = line.trim();
                    if ( StringUtils.isBlank(line) ) {
                        continue;
                    }
                    int sep = line.indexOf('=');
                    if ( sep < 0 ) {
                        continue;
                    }

                    String ifname = line.substring(0, sep).trim();
                    String alias = line.substring(sep + 1).trim();

                    if ( StringUtils.isBlank(ifname) || StringUtils.isBlank(alias) ) {
                        continue;
                    }

                    if ( alias.charAt(0) == '_' ) {
                        alias = alias.substring(1);
                    }

                    aliases.put(ifname, alias);
                }
            }
            else {
                log.debug("Cannot read interface state file"); //$NON-NLS-1$
            }
        }
        catch ( IOException e ) {
            log.warn("Failed to read interface state file", e); //$NON-NLS-1$
        }
        return aliases;
    }

}
