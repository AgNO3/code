/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.fileshare.orch.common.i18n.FileshareConfigurationMessages;
import eu.agno3.orchestrator.config.auth.AuthenticatorsConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.auth.RoleConfigImpl;
import eu.agno3.orchestrator.config.auth.StaticRolesConfigImpl;
import eu.agno3.orchestrator.config.auth.StaticRolesConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class FileshareAuthConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<FileshareAuthConfig, FileshareAuthConfigImpl> {

    /**
     * 
     */
    public static final String TYPE_NAME = "urn:agno3:objects:1.0:fileshare:auth"; //$NON-NLS-1$


    /**
     * 
     */
    public FileshareAuthConfigObjectTypeDescriptor () {
        super(FileshareAuthConfig.class, FileshareAuthConfigImpl.class, FileshareConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getParentTypeName()
     */
    @Override
    public String getParentTypeName () {
        return FileshareConfigurationObjectTypeDescriptor.TYPE_NAME;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull FileshareAuthConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull FileshareAuthConfig getGlobalDefaults () {
        FileshareAuthConfigImpl authc = new FileshareAuthConfigImpl();
        StaticRolesConfigImpl roles = new StaticRolesConfigImpl();

        addRole(roles, "ADMIN", false, //$NON-NLS-1$
            "55ebd48b-ebc5-4690-b8ba-fc8627734ab5", //$NON-NLS-1$
            "Administrator", //$NON-NLS-1$
            "Full administrative rights, all permissions", //$NON-NLS-1$
            "*"); //$NON-NLS-1$

        addRole(roles, "MANAGE_USERS", false, //$NON-NLS-1$
            "3ef437cd-2037-4f4b-9982-a3423f7fc930", //$NON-NLS-1$
            "User Manager", //$NON-NLS-1$
            "Add/remove/manage user accounts", //$NON-NLS-1$
            "manage:users:*", //$NON-NLS-1$
            "manage:subjects:list"); //$NON-NLS-1$

        addRole(roles, "MANAGE_GROUPS", false, //$NON-NLS-1$
            "633d1a98-741d-43ce-9481-7f2d33cdbe94", //$NON-NLS-1$
            "Group Manager", //$NON-NLS-1$
            "Add/remove/manage groups and group memberships", //$NON-NLS-1$
            "manage:groups:*", //$NON-NLS-1$
            "manage:subjects:list"); //$NON-NLS-1$

        addRole(roles, "MANAGE_PERMS", false, //$NON-NLS-1$
            "dc985fbc-d085-40a4-b151-469d5819f2ae", //$NON-NLS-1$
            "Permission Manager", //$NON-NLS-1$
            "Modify user and group roles/permissions", //$NON-NLS-1$
            "manage:perms:*"); //$NON-NLS-1$

        addRole(roles, "DEFAULT_USER", false, //$NON-NLS-1$
            "1e059e98-8655-46e4-b827-fa20bca8292b", //$NON-NLS-1$
            "Default User", //$NON-NLS-1$
            "Regular user account", //$NON-NLS-1$
            "subjects:query:*", //$NON-NLS-1$
            "user:*", //$NON-NLS-1$
            "share:*", //$NON-NLS-1$
            "entity:*"); //$NON-NLS-1$

        addRole(roles, "EXTERNAL_USER", false, //$NON-NLS-1$
            "7b3905b5-4ae4-4548-8e4c-89e03ca1c3de", //$NON-NLS-1$
            "External User", //$NON-NLS-1$
            "External user account", //$NON-NLS-1$
            "subjects:query:*", //$NON-NLS-1$
            "user:updateDetails", //$NON-NLS-1$
            "share:subjects", //$NON-NLS-1$
            "entity:*"); //$NON-NLS-1$

        addRole(roles, "INVITED_USER", true, //$NON-NLS-1$
            "ae0faeff-9db7-469b-83f6-d25f51d03a0e", //$NON-NLS-1$
            "Invited User", //$NON-NLS-1$
            "This user was invited by another user", //$NON-NLS-1$
            "subjects:query:*", //$NON-NLS-1$
            "user:updateDetails", //$NON-NLS-1$
            "share:subjects", //$NON-NLS-1$
            "entity:*"); //$NON-NLS-1$

        addRole(roles, "SELF_REGISTERED_USER", true, //$NON-NLS-1$
            "d29212d3-0d30-49b0-847a-012f1c600451", //$NON-NLS-1$
            "Self-registered User", //$NON-NLS-1$
            "Self registered user account", //$NON-NLS-1$
            "user:updateDetails", //$NON-NLS-1$
            "entity:*"); //$NON-NLS-1$

        addRole(roles, "ADMIN_CREATED_USER", true, //$NON-NLS-1$
            "f4df5e3e-bac7-426a-9cc2-022dc175e0c0", //$NON-NLS-1$
            "Admin-created User", //$NON-NLS-1$
            "This user was created by an administrator" //$NON-NLS-1$
        );

        addRole(roles, "SYNCHRONIZED_USER", true, //$NON-NLS-1$
            "e5d04764-a12a-4ee0-b9b2-c284d6c9da0f", //$NON-NLS-1$
            "Synchronized User", //$NON-NLS-1$
            "This user is from an external authentication source" //$NON-NLS-1$
        );

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
    public static @NonNull FileshareAuthConfigMutable emptyInstance () {
        FileshareAuthConfigImpl fuc = new FileshareAuthConfigImpl();
        fuc.setAuthenticators(AuthenticatorsConfigObjectTypeDescriptor.emptyInstance());
        fuc.setRoleConfig(StaticRolesConfigObjectTypeDescriptor.emptyInstance());
        return fuc;
    }

}
