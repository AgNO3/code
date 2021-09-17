/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.config;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.fileshare.orch.common.config.FileshareAuthConfig;
import eu.agno3.fileshare.orch.common.config.FileshareAuthConfigObjectTypeDescriptor;
import eu.agno3.fileshare.orch.common.config.FileshareConfiguration;
import eu.agno3.fileshare.orch.common.config.FileshareConfigurationObjectTypeDescriptor;
import eu.agno3.orchestrator.config.auth.RoleConfig;
import eu.agno3.orchestrator.config.auth.StaticRolesConfig;
import eu.agno3.orchestrator.config.auth.StaticRolesConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;
import eu.agno3.orchestrator.server.webgui.config.auth.RoleConfigComparator;
import eu.agno3.orchestrator.server.webgui.config.instance.InstanceConfigContextBean;
import eu.agno3.orchestrator.server.webgui.config.template.TemplateConfigContextBean;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.util.completer.Completer;
import eu.agno3.orchestrator.server.webgui.util.completer.EmptyCompleter;


/**
 * @author mbechler
 *
 */
@Named ( "fs_authenticationConfigBean" )
@ApplicationScoped
public class AuthenticationConfigBean {

    /**
     * 
     * @param o
     * @return inner editor template
     */
    public String getInnerEditorTemplate ( Object o ) {
        if ( ! ( o instanceof ConfigContext ) ) {
            return null;
        }

        ConfigContext<?, ?> ctx = (ConfigContext<?, ?>) o;

        if ( ctx instanceof TemplateConfigContextBean ) {
            return "/fileshare/auth/innerTemplate.xhtml"; //$NON-NLS-1$
        }
        else if ( ctx instanceof InstanceConfigContextBean ) {
            return "/fileshare/auth/innerInstance.xhtml"; //$NON-NLS-1$
        }
        return null;
    }


    /**
     * 
     * @param wr
     * @return whether local authentication is enabled
     */
    public boolean isLocalAuthEnabled ( OuterWrapper<?> wr ) {
        if ( wr == null ) {
            return true;
        }

        OuterWrapper<?> outerWrapper = wr.get(FileshareConfigurationObjectTypeDescriptor.TYPE_NAME);
        if ( outerWrapper == null ) {
            return true;
        }

        AbstractObjectEditor<?> editor = outerWrapper.getEditor();
        try {
            FileshareConfiguration current = (FileshareConfiguration) editor.getCurrent();

            if ( current == null || current.getAuthConfiguration() == null || current.getAuthConfiguration().getAuthenticators() == null
                    || current.getAuthConfiguration().getAuthenticators().getEnableLocalAuth() == null ) {

                FileshareConfiguration defaults = (FileshareConfiguration) editor.getDefaults();

                if ( defaults == null || defaults.getAuthConfiguration() == null || defaults.getAuthConfiguration().getAuthenticators() == null
                        || defaults.getAuthConfiguration().getAuthenticators().getEnableLocalAuth() ) {
                    return true;
                }
                return defaults.getAuthConfiguration().getAuthenticators().getEnableLocalAuth();
            }

            return current.getAuthConfiguration().getAuthenticators().getEnableLocalAuth();
        }
        catch (
            ModelObjectNotFoundException |
            ModelServiceException |
            GuiWebServiceException e ) {
            ExceptionHandler.handle(e);
            return true;
        }
    }


    /**
     * 
     * @param ctx
     * @return a role completer
     */
    @SuppressWarnings ( "unchecked" )
    public Completer<String> getContextRoleCompleter ( ConfigContext<?, ?> ctx ) {

        if ( ctx == null ) {
            return new EmptyCompleter();
        }

        final List<RoleConfig> roles = new ArrayList<>();
        try {
            boolean found = false;
            for ( ConfigurationObject obj : Arrays.asList(ctx.getEnforced(), ctx.getCurrent(), ctx.getInherited(), ctx.getDefaults()) ) {
                if ( getRolesFromObject(roles, obj) ) {
                    found = true;
                    break;
                }
            }

            if ( !found ) {
                String[] types = new String[] {
                    StaticRolesConfigObjectTypeDescriptor.TYPE_NAME, FileshareAuthConfigObjectTypeDescriptor.TYPE_NAME,
                    FileshareConfigurationObjectTypeDescriptor.TYPE_NAME
                };

                for ( String type : types ) {
                    ConfigurationObject objectDefaults = ctx.getObjectDefaults(type, null);
                    if ( getRolesFromObject(roles, objectDefaults) ) {
                        break;
                    }
                }

            }
        }
        catch (
            ModelObjectNotFoundException |
            ModelServiceException |
            GuiWebServiceException e ) {
            ExceptionHandler.handle(e);
            return new EmptyCompleter();
        }

        return makeRoleCompleter(roles);
    }


    /**
     * @param roles
     * @param found
     * @param obj
     * @return
     */
    private static boolean getRolesFromObject ( final List<RoleConfig> roles, ConfigurationObject obj ) {
        if ( obj == null ) {
            return false;
        }
        if ( obj instanceof FileshareConfiguration && ( (FileshareConfiguration) obj ).getAuthConfiguration() != null
                && ( (FileshareConfiguration) obj ).getAuthConfiguration().getRoleConfig() != null
                && ! ( (FileshareConfiguration) obj ).getAuthConfiguration().getRoleConfig().getRoles().isEmpty() ) {
            roles.addAll( ( (FileshareConfiguration) obj ).getAuthConfiguration().getRoleConfig().getRoles());
            return true;
        }
        else if ( obj instanceof FileshareAuthConfig && ( (FileshareAuthConfig) obj ).getRoleConfig() != null
                && ! ( (FileshareAuthConfig) obj ).getRoleConfig().getRoles().isEmpty() ) {
            roles.addAll( ( (FileshareAuthConfig) obj ).getRoleConfig().getRoles());
            return true;
        }
        else if ( obj instanceof StaticRolesConfig && ! ( (StaticRolesConfig) roles ).getRoles().isEmpty() ) {
            roles.addAll( ( (StaticRolesConfig) roles ).getRoles());
            return true;
        }
        return false;
    }


    /**
     * 
     * @param wr
     * @return a role completer
     */
    @SuppressWarnings ( "unchecked" )
    public Completer<String> getRoleCompleter ( OuterWrapper<?> wr ) {
        if ( wr == null ) {
            return new EmptyCompleter();
        }

        OuterWrapper<?> outerWrapper = wr.get(FileshareConfigurationObjectTypeDescriptor.TYPE_NAME);
        if ( outerWrapper == null ) {
            return new EmptyCompleter();
        }

        final List<RoleConfig> roles = new ArrayList<>();
        AbstractObjectEditor<?> editor = outerWrapper.getEditor();
        try {
            FileshareConfiguration current = (FileshareConfiguration) editor.getCurrent();

            if ( current == null || current.getAuthConfiguration() == null || current.getAuthConfiguration().getRoleConfig() == null
                    || current.getAuthConfiguration().getRoleConfig().getRoles().isEmpty() ) {

                FileshareConfiguration defaults = (FileshareConfiguration) editor.getDefaults();

                if ( defaults == null || defaults.getAuthConfiguration() == null || defaults.getAuthConfiguration().getRoleConfig() == null
                        || defaults.getAuthConfiguration().getRoleConfig().getRoles().isEmpty() ) {
                    return new EmptyCompleter();
                }

                roles.addAll(defaults.getAuthConfiguration().getRoleConfig().getRoles());
            }
            else {
                roles.addAll(current.getAuthConfiguration().getRoleConfig().getRoles());
            }
        }
        catch (
            ModelObjectNotFoundException |
            ModelServiceException |
            GuiWebServiceException e ) {
            ExceptionHandler.handle(e);
            return new EmptyCompleter();
        }

        return makeRoleCompleter(roles);
    }


    /**
     * @param roles
     * @return
     */
    private static Completer<String> makeRoleCompleter ( final List<RoleConfig> roles ) {
        Collections.sort(roles, new RoleConfigComparator());

        return new Completer<String>() {

            @Override
            public List<String> complete ( String query ) {
                String uc = query != null ? query.toUpperCase() : null;
                List<String> res = new ArrayList<>();
                for ( RoleConfig r : roles ) {
                    if ( uc == null || ( r.getRoleId() != null && r.getRoleId().toUpperCase().startsWith(query) ) ) {
                        res.add(r.getRoleId());
                    }
                }
                return res;
            }

        };
    }
}
