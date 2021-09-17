/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 19, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.resolver.validation;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.resolver.ResolverConfigTestParams;
import eu.agno3.orchestrator.config.hostconfig.resolver.ResolverConfiguration;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestAsyncHandler;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestContext;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginAsync;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginRunOn;
import eu.agno3.orchestrator.config.model.validation.ConfigTestParams;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResult;
import eu.agno3.orchestrator.config.model.validation.ConfigTestState;
import eu.agno3.orchestrator.config.web.validation.SocketValidationUtils;
import eu.agno3.orchestrator.types.net.IPv6Address;
import eu.agno3.orchestrator.types.net.NetworkAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = ConfigTestPlugin.class )
public class ResolverConfigurationTestPlugin implements ConfigTestPluginAsync<ResolverConfiguration> {

    private static final Logger log = Logger.getLogger(ResolverConfigurationTestPlugin.class);


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin#getTargetType()
     */
    @Override
    public Class<ResolverConfiguration> getTargetType () {
        return ResolverConfiguration.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin#getRunOn()
     */
    @Override
    public Set<ConfigTestPluginRunOn> getRunOn () {
        return EnumSet.of(ConfigTestPluginRunOn.AGENT, ConfigTestPluginRunOn.SERVER);
    }


    @Override
    public ConfigTestResult testAsync ( ResolverConfiguration config, ConfigTestContext ctx, ConfigTestParams params, ConfigTestResult r,
            ConfigTestAsyncHandler h ) throws ModelServiceException {

        log.debug("Running DNS test"); //$NON-NLS-1$

        if ( ! ( params instanceof ResolverConfigTestParams ) ) {
            return r.state(ConfigTestState.FAILURE);
        }

        ResolverConfigTestParams p = (ResolverConfigTestParams) params;
        String hostname = p.getHostname();

        List<NetworkAddress> ns = config.getNameservers();
        if ( config.getAutoconfigureDns() != null && config.getAutoconfigureDns() ) {

            // looking for name servers in dhcp leases could work in some
            // situations but we cannot really be sure that they are the
            // ones we end up after reconfiguration

            r.info("NETWORK_ACTIVE_CONFIG"); //$NON-NLS-1$
            r.warn("DNS_AUTOCONFIG"); //$NON-NLS-1$

            InetAddress[] resolved = SocketValidationUtils.checkDNSLookup(r, hostname);
            if ( resolved == null ) {
                return r.state(ConfigTestState.FAILURE);
            }

            if ( ns == null || ns.isEmpty() ) {
                return r.state(ConfigTestState.WARNING);
            }

            return r.state(ConfigTestState.SUCCESS);
        }
        else if ( ns == null || ns.isEmpty() ) {
            r.error("DNS_NO_SERVERS"); //$NON-NLS-1$
            return r.state(ConfigTestState.FAILURE);
        }

        r.info("DNS_SERVERS", ns.toString()); //$NON-NLS-1$
        h.update(r);

        for ( NetworkAddress serv : ns ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Checking server " + serv); //$NON-NLS-1$
            }
            r.info("DNS_CHECK_SERVER", hostname, serv.toString()); //$NON-NLS-1$

            DirContext dnsctx = makeDirContext(serv);

            if ( dnsctx == null ) {
                continue;
            }

            try {
                Attributes res = dnsctx.getAttributes(hostname, new String[] {
                    "A", //$NON-NLS-1$
                    "AAAA", //$NON-NLS-1$
                    "CNAME" //$NON-NLS-1$
                });

                Map<String, List<String>> byRRType = toMap(res);
                r.info("DNS_LOOKUP_OK", hostname, serv.toString(), byRRType.toString()); //$NON-NLS-1$
            }
            catch ( NamingException e ) {
                log.debug("Exception looking up " + hostname, e); //$NON-NLS-1$
                r.error("DNS_LOOKUP_FAIL", hostname, serv.toString(), e.getMessage()); //$NON-NLS-1$
            }

            h.update(r);
        }

        return r.state(ConfigTestState.SUCCESS);
    }


    /**
     * @param res
     * @return
     * @throws NamingException
     */
    private static Map<String, List<String>> toMap ( Attributes res ) throws NamingException {
        Map<String, List<String>> byRRType = new HashMap<>();
        NamingEnumeration<? extends Attribute> attrs = res.getAll();
        while ( attrs.hasMoreElements() ) {
            Attribute attr = attrs.nextElement();

            if ( log.isDebugEnabled() ) {
                log.debug(attr.getID() + ": " + attr.get()); //$NON-NLS-1$
            }

            List<String> values;
            if ( !byRRType.containsKey(attr.getID()) ) {
                values = new ArrayList<>();
                byRRType.put(attr.getID(), values);
            }
            else {
                values = byRRType.get(attr.getID());
            }
            values.add(attr.get().toString());
        }
        return byRRType;
    }


    /**
     * @param serv
     * @return
     */
    private static DirContext makeDirContext ( NetworkAddress serv ) {
        try {
            Class<?> cl = Class.forName("com.sun.jndi.dns.DnsContext"); //$NON-NLS-1$
            Constructor<?> cons = cl.getConstructor(String.class, String[].class, Hashtable.class);
            String addr = serv instanceof IPv6Address ? '[' + serv.getCanonicalForm() + ']' : serv.getCanonicalForm();
            return (DirContext) cons.newInstance(".", new String[] { //$NON-NLS-1$
                addr
            }, new Hashtable<>());
        }
        catch (
            ClassNotFoundException |
            NoSuchMethodException |
            SecurityException |
            InstantiationException |
            IllegalAccessException |
            IllegalArgumentException |
            InvocationTargetException e ) {
            log.debug("Incompatible", e); //$NON-NLS-1$
            return null;
        }
    }

}
