/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web.agent;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.auth.ldap.LDAPAuthSchemaConfig;
import eu.agno3.orchestrator.config.web.LDAPConfiguration;
import eu.agno3.orchestrator.config.web.LDAPObjectAttributeMapping;
import eu.agno3.orchestrator.config.web.LDAPObjectConfig;
import eu.agno3.orchestrator.config.web.SSLClientMode;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.system.RuntimeConfigContext;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.config.util.PropertyConfigBuilder;


/**
 * @author mbechler
 *
 */
public final class LDAPConfigUtil {

    /**
     * 
     */
    private LDAPConfigUtil () {}


    /**
     * @param b
     * @param ctx
     * @param ldapConnName
     * @param connectionConfig
     * @throws ServiceManagementException
     * @throws UnitInitializationFailedException
     * @throws InvalidParameterException
     */
    @SuppressWarnings ( "nls" )
    public static void configureConnection ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<?, ?> ctx, String ldapConnName,
            LDAPConfiguration connectionConfig ) throws InvalidParameterException, UnitInitializationFailedException, ServiceManagementException {

        PropertyConfigBuilder p = PropertyConfigBuilder.get();

        p.p("serverType", connectionConfig.getServerType().name());

        if ( !StringUtils.isBlank(connectionConfig.getSrvDomain()) ) {
            p.p("srvDomain", connectionConfig.getSrvDomain());
        }
        else {
            List<String> servers = new ArrayList<>();
            for ( URI uri : connectionConfig.getServers() ) {
                servers.add(uri.toString());
            }
            p.p("servers", servers);
        }

        String tlsContextName = "ldap-" + ldapConnName;
        SSLConfigUtil.setupSSLClientMapping(b, ctx, tlsContextName, "ldap", connectionConfig.getSslClientConfiguration(), null);
        if ( connectionConfig.getSslClientMode() != SSLClientMode.DISABLE ) {
            p.p("tlsContextName", tlsContextName);
            if ( connectionConfig.getSslClientMode() == SSLClientMode.SSL ) {
                p.p("useSSL", true);
            }
            else {
                p.p("useStartTLS", true);
            }
        }

        if ( !StringUtils.isBlank(connectionConfig.getBaseDN()) ) {
            p.p("baseDN", connectionConfig.getBaseDN());
        }

        p.p("connectTimeout", connectionConfig.getSocketTimeout());
        p.p("responseTimeout", connectionConfig.getSocketTimeout());

        switch ( connectionConfig.getAuthType() ) {
        case SASL:
            p.p("useSASL", true);
            p.p("saslAuthId", connectionConfig.getSaslUsername());
            p.p("saslMechanism", connectionConfig.getSaslMechanism());
            if ( connectionConfig.getSslClientMode() == SSLClientMode.DISABLE && connectionConfig.getSaslQOP() != null ) {
                p.p("saslQop", connectionConfig.getSaslQOP().name());
            }
            p.p("bindPassword", connectionConfig.getPassword());
            break;
        case SIMPLE:
            p.p("bindDN", connectionConfig.getBindDN());
            p.p("bindPassword", connectionConfig.getPassword());
            break;
        default:
        case ANONYMOUS:
            break;

        }

        ctx.factory("ldap", ldapConnName, p);
    }


    /**
     * @param b
     * @param ctx
     * @param schemaConfig
     * @param cfg
     */
    @SuppressWarnings ( "nls" )
    public static void makeLDAPAuthSchemaConfig ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<?, ?> ctx, LDAPAuthSchemaConfig schemaConfig,
            PropertyConfigBuilder cfg ) {

        cfg.p("groupMemberIsDN", schemaConfig.getReferencesAreDNs());
        cfg.p("recursiveResolveGroups", schemaConfig.getRecursiveResolveGroups());
        cfg.p("useForwardGroups", schemaConfig.getUseForwardGroups());

        makeLDAPObjectConfig("user.", cfg, schemaConfig.getUserSchema());
        makeLDAPObjectConfig("group.", cfg, schemaConfig.getGroupSchema());
        makeAttributeMappings(cfg, schemaConfig.getOperationalAttributeMappings(), "operational.attrs.");

    }


    /**
     * @param b
     * @param ctx
     * @param schemaConfig
     * @param cfg
     */
    @SuppressWarnings ( "nls" )
    public static void makeADAuthSchemaConfig ( @NonNull JobBuilder b, @NonNull RuntimeConfigContext<?, ?> ctx, LDAPAuthSchemaConfig schemaConfig,
            PropertyConfigBuilder cfg ) {

        cfg.p("schemaStyle", "AD");
        cfg.p("groupMemberIsDN", schemaConfig.getReferencesAreDNs());
        cfg.p("recursiveResolveGroups", schemaConfig.getRecursiveResolveGroups());
        cfg.p("useForwardGroups", schemaConfig.getUseForwardGroups());

        makeADObjectConfig("user.", cfg, schemaConfig.getUserSchema());
        makeADObjectConfig("group.", cfg, schemaConfig.getGroupSchema());
        makeAttributeMappings(cfg, schemaConfig.getOperationalAttributeMappings(), "operational.attrs.");
    }


    /**
     * @param string
     * @param cfg
     * @param sc
     */
    @SuppressWarnings ( "nls" )
    private static void makeADObjectConfig ( String prefix, PropertyConfigBuilder cfg, LDAPObjectConfig sc ) {
        cfg.p(prefix + "baseDN", sc.getBaseDN());
        cfg.p(prefix + "scope", sc.getScope() != null ? sc.getScope().name() : null);
        cfg.p(prefix + "filter", sc.getCustomFilter());
        cfg.p(prefix + "style", "AD"); //$NON-NLS-2$
        makeAttributeMappings(cfg, sc.getCustomAttributeMappings(), prefix + "attrs.");
    }


    /**
     * @param cfg
     * @param mappings
     * @param prefix
     */
    public static void makeAttributeMappings ( PropertyConfigBuilder cfg, Set<LDAPObjectAttributeMapping> mappings, String prefix ) {
        for ( LDAPObjectAttributeMapping map : mappings ) {
            cfg.p(prefix + map.getAttributeId(), map.getAttributeName());
        }
    }


    /**
     * @param prefix
     * @param cfg
     * @param sc
     */
    @SuppressWarnings ( "nls" )
    public static void makeLDAPObjectConfig ( String prefix, PropertyConfigBuilder cfg, LDAPObjectConfig sc ) {
        cfg.p(prefix + "baseDN", sc.getBaseDN());
        cfg.p(prefix + "scope", sc.getScope() != null ? sc.getScope().name() : null);
        cfg.p(prefix + "filter", sc.getCustomFilter());
        cfg.p(prefix + "style", sc.getAttributeStyle());
        makeAttributeMappings(cfg, sc.getCustomAttributeMappings(), prefix + "attrs.");
    }
}
