/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.orchestrator;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.orchestrator.config.auth.RoleConfig;
import eu.agno3.orchestrator.config.auth.StaticRolesConfig;
import eu.agno3.orchestrator.config.auth.StaticRolesConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfiguration;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfigurationObjectTypeDescriptor;
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
@Named ( "orch_authenticationConfigBean" )
@ApplicationScoped
public class OrchestratorAuthConfigBean {

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
            return "/orchestrator/auth/innerTemplate.xhtml"; //$NON-NLS-1$
        }
        else if ( ctx instanceof InstanceConfigContextBean ) {
            return "/orchestrator/auth/innerInstance.xhtml"; //$NON-NLS-1$
        }
        return null;
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
                    StaticRolesConfigObjectTypeDescriptor.TYPE_NAME
                };

                for ( String type : types ) {
                    ConfigurationObject objectDefaults = ctx.getObjectDefaults(type, null);
                    if ( getRolesFromObject(roles, objectDefaults) ) {
                        break;
                    }
                }

            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return new EmptyCompleter();
        }

        return makeRoleCompleter(roles);
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

        OuterWrapper<?> outerWrapper = wr.get(OrchestratorConfigurationObjectTypeDescriptor.TYPE_NAME);
        if ( outerWrapper == null ) {
            return new EmptyCompleter();
        }

        final List<RoleConfig> roles = new ArrayList<>();
        AbstractObjectEditor<?> editor = outerWrapper.getEditor();
        try {
            OrchestratorConfiguration current = (OrchestratorConfiguration) editor.getCurrent();

            if ( current == null || current.getAuthenticationConfig() == null || current.getAuthenticationConfig().getRoleConfig() == null
                    || current.getAuthenticationConfig().getRoleConfig().getRoles().isEmpty() ) {

                OrchestratorConfiguration defaults = (OrchestratorConfiguration) editor.getDefaults();

                if ( defaults == null || defaults.getAuthenticationConfig() == null || defaults.getAuthenticationConfig().getRoleConfig() == null
                        || defaults.getAuthenticationConfig().getRoleConfig().getRoles().isEmpty() ) {
                    return new EmptyCompleter();
                }

                roles.addAll(defaults.getAuthenticationConfig().getRoleConfig().getRoles());
            }
            else {
                roles.addAll(current.getAuthenticationConfig().getRoleConfig().getRoles());
            }
        }
        catch ( Exception e ) {
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

        if ( obj instanceof StaticRolesConfig && ! ( (StaticRolesConfig) roles ).getRoles().isEmpty() ) {
            roles.addAll( ( (StaticRolesConfig) roles ).getRoles());
            return true;
        }
        return false;
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
