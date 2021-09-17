/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.orchestrator;


import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.auth.AuthenticatorsConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.auth.RoleConfigImpl;
import eu.agno3.orchestrator.config.auth.StaticRolesConfigImpl;
import eu.agno3.orchestrator.config.auth.StaticRolesConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.config.orchestrator.i18n.OrchestratorConfigurationMessages;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class OrchestratorAuthenticationConfigurationObjectTypeDescriptor
        extends AbstractObjectTypeDescriptor<OrchestratorAuthenticationConfiguration, OrchestratorAuthenticationConfigurationImpl> {

    /**
     * 
     */
    public OrchestratorAuthenticationConfigurationObjectTypeDescriptor () {
        super(
            OrchestratorAuthenticationConfiguration.class,
            OrchestratorAuthenticationConfigurationImpl.class,
            OrchestratorConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull OrchestratorAuthenticationConfiguration newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    @SuppressWarnings ( "nls" )
    public @NonNull OrchestratorAuthenticationConfiguration getGlobalDefaults () {
        OrchestratorAuthenticationConfigurationImpl authc = new OrchestratorAuthenticationConfigurationImpl();
        StaticRolesConfigImpl roles = new StaticRolesConfigImpl();

        addRole(
            roles,
            "ADMIN", //$NON-NLS-1$
            false,
            "55ebd48b-ebc5-4690-b8ba-fc8627734ab5",
            "Administrator",
            "Full administrative rights, all permissions",
            "*");

        addRole(
            roles,
            "DEFAULT_USER", //$NON-NLS-1$
            false,
            "1e059e98-8655-46e4-b827-fa20bca8292b",
            "Default User",
            "Regular user account",
            "agents:*",
            "config:*",
            "default:*",
            "enforcement:*",
            "job:*",
            "keystores:*",
            "realms:*",
            "resourceLibrary:*",
            "security:*",
            "structure:*",
            "sysinfo:*",
            "update:*",
            "backup:*",
            "power:*",
            "licensing:*",
            "fileshare:*",
            "webgui");

        authc.setRoleConfig(roles);
        return authc;
    }


    /**
     * @param roles
     * @param string
     * @param string2
     * @param string3
     * @param string4
     * @param string5
     * @return
     */
    private static RoleConfigImpl addRole ( StaticRolesConfigImpl roles, String role, boolean hidden, String id, String title, String description,
            String... perms ) {
        RoleConfigImpl admRole = new RoleConfigImpl();
        admRole.setId(UUID.fromString(id));
        admRole.setRoleId(role);
        admRole.getTitles().put(Locale.ROOT, title);
        admRole.getDescriptions().put(Locale.ROOT, description);
        admRole.getPermissions().addAll(Arrays.asList(perms));
        roles.getRoles().add(admRole);
        return admRole;
    }


    /**
     * @return empty instance
     */
    public static @NonNull OrchestratorAuthenticationConfigurationMutable emptyInstance () {
        OrchestratorAuthenticationConfigurationImpl oc = new OrchestratorAuthenticationConfigurationImpl();
        oc.setAuthenticatorsConfig(AuthenticatorsConfigObjectTypeDescriptor.emptyInstance());
        oc.setRoleConfig(StaticRolesConfigObjectTypeDescriptor.emptyInstance());
        return oc;
    }
}
