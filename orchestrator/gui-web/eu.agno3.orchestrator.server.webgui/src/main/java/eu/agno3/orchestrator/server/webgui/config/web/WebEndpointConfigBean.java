/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.web;


import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.webbeans.util.StringUtil;

import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntry;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.hostconfig.EffectiveHostConfigProvider;
import eu.agno3.orchestrator.server.webgui.util.completer.Completer;
import eu.agno3.orchestrator.server.webgui.util.completer.EmptyCompleter;
import eu.agno3.orchestrator.types.net.NetworkSpecification;


/**
 * @author mbechler
 *
 */
@Named ( "webEndpointConfigBean" )
@ApplicationScoped
public class WebEndpointConfigBean {

    @Inject
    private EffectiveHostConfigProvider hcprov;


    @SuppressWarnings ( "unchecked" )
    public Completer<String> getInterfaceCompleter ( OuterWrapper<?> outer ) {
        HostConfiguration hc = this.hcprov.getEffectiveHostConfiguration();
        if ( hc == null || hc.getNetworkConfiguration() == null || hc.getNetworkConfiguration().getInterfaceConfiguration() == null
                || hc.getNetworkConfiguration().getInterfaceConfiguration().getInterfaces() == null ) {
            return new EmptyCompleter();
        }

        final Set<InterfaceEntry> interfaces = hc.getNetworkConfiguration().getInterfaceConfiguration().getInterfaces();
        return new Completer<String>() {

            @Override
            public List<String> complete ( String query ) {
                List<String> res = new LinkedList<>();
                for ( InterfaceEntry e : interfaces ) {
                    String alias = e.getAlias();
                    if ( alias != null && !alias.isEmpty() && alias.startsWith(query) ) {
                        res.add(alias);
                    }
                }
                return res;
            }
        };
    }


    @SuppressWarnings ( "unchecked" )
    public Completer<String> getAddressCompleter ( OuterWrapper<?> outer ) {
        HostConfiguration hc = this.hcprov.getEffectiveHostConfiguration();
        if ( hc == null || hc.getNetworkConfiguration() == null || hc.getNetworkConfiguration().getInterfaceConfiguration() == null
                || hc.getNetworkConfiguration().getInterfaceConfiguration().getInterfaces() == null ) {
            return new EmptyCompleter();
        }

        final Set<InterfaceEntry> interfaces = hc.getNetworkConfiguration().getInterfaceConfiguration().getInterfaces();
        return new Completer<String>() {

            @Override
            public List<String> complete ( String query ) {
                List<String> res = new LinkedList<>();
                for ( InterfaceEntry e : interfaces ) {
                    if ( e.getStaticAddresses() == null || e.getStaticAddresses().isEmpty() ) {
                        continue;
                    }

                    for ( NetworkSpecification networkSpecification : e.getStaticAddresses() ) {
                        String addrString = networkSpecification.getAddress().toString();
                        if ( !StringUtil.isBlank(addrString) && addrString.contains(query) ) {
                            res.add(addrString);
                        }
                    }

                }
                return res;
            }
        };
    }
}
