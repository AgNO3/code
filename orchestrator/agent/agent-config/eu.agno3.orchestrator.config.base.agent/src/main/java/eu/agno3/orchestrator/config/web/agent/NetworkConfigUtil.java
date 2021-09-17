/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web.agent;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntry;
import eu.agno3.orchestrator.jobs.agent.system.ConfigurationJobContext;
import eu.agno3.orchestrator.jobs.agent.system.JobBuilderException;
import eu.agno3.orchestrator.system.info.SystemInformationException;
import eu.agno3.orchestrator.system.info.network.NetworkInformation;
import eu.agno3.orchestrator.system.info.network.NetworkInterface;


/**
 * @author mbechler
 *
 */
public class NetworkConfigUtil {

    /**
     * @param e
     * @param networkInterfaces
     * @return the real interface name for an interface entry
     * @throws SystemInformationException
     * @throws JobBuilderException
     */
    public static String findRealInterface ( InterfaceEntry e, List<NetworkInterface> networkInterfaces ) throws SystemInformationException,
            JobBuilderException {
        for ( NetworkInterface netIf : networkInterfaces ) {
            if ( e.getInterfaceIndex() != null && netIf.getInterfaceIndex() != e.getInterfaceIndex() ) {
                continue;
            }
            if ( e.getHardwareAddress() != null
                    && ( !netIf.getHardwareAddress().equals(e.getHardwareAddress()) && !netIf.getHardwareAddress().equals(
                        e.getOverrideHardwareAddress()) ) ) {
                continue;
            }
            return netIf.getName();
        }
        throw new JobBuilderException("No match exists for network interface " + e.getAlias()); //$NON-NLS-1$
    }


    /**
     * @param ctx
     * @param cfg
     * @return a map of interface alias to real interface name
     * @throws SystemInformationException
     * @throws JobBuilderException
     */
    public static Map<String, String> makeInterfaceAliasMap ( ConfigurationJobContext<?, ?> ctx, HostConfiguration cfg )
            throws SystemInformationException, JobBuilderException {
        NetworkInformation netInfo = ctx.netInfo();
        Set<InterfaceEntry> interfaces = cfg.getNetworkConfiguration().getInterfaceConfiguration().getInterfaces();
        HashMap<String, String> res = new HashMap<>();
        List<NetworkInterface> networkInterfaces = netInfo.getNetworkInterfaces();
        for ( InterfaceEntry e : interfaces ) {
            res.put(e.getAlias(), findRealInterface(e, networkInterfaces));
        }
        return res;
    }

}
