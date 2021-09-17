/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui;


import java.util.Locale;
import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import eu.agno3.runtime.jsf.i18n.FacesMessageBundle;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( {
    "nls"
} )
@ApplicationScoped
@Named ( "msgs" )
public class GuiMessages extends FacesMessageBundle {

    public static final String CONFIG_ANONYMOUS_OBJECT = "config.anonymousObjectFmt";

    public static final String CONFIG_SSV_FAILED = "config.ssvFailed";

    public static final String CONFIG_REVISIONS_LOAD_FAILED = "config.revisions.loadFailed";
    public static final String CONFIG_REVISIONS_REV_LABEL = "config.revisions.revision.labelFmt";
    public static final String CONFIG_REVISIONS_REV_DETAIL = "config.revisions.revision.detailFmt";
    public static final String CONFIG_REVISIONS_REV_CUR = "config.revisions.revision.current";
    public static final String CONFIG_REVISIONS_REV_CUR_DETAIL = "config.revisions.revision.currentFmt";

    public static final String CONFIG_INSTANCE_APPLY_FAILED = "config.instance.applyFailed";

    public static final String STRUCTURE_ROOT_LABEL = "structure.root.label";
    public static final String SERVICE_TITLE = "service.titleFmt";
    public static final String SERVICE_DESCRIPTIVE = "service.titleDescriptiveFmt";
    public static final String INSTANCE_DESCRIPTIVE = "instance.titleDescriptiveFmt";
    public static final String GROUP_DESCRIPTIVE = "group.titleDescriptiveFmt";

    public static final String OBJEDIT_INHERIT_DEFAULT = "components.objectEditor.inherits.defaults";
    public static final String OBJEDIT_INHERIT_DEFAULT_DETAIL = "components.objectEditor.inherits.defaultsDetailFmt";

    public static final String JOBS_DOESNOTEXIST = "jobs.doesNotExist";

    public static final String MENU_FAILED_TO_LOAD_CHILDREN = "menu.failedToLoadChildren";

    public static final String UNNAMED_CONFIG_OBJECT = "config.object.unnamed";

    /**
     * 
     */
    public static final String GUI_MESSAGES_BASE = "eu.agno3.orchestrator.server.webgui.messages";

    public static final String CONFIG_SAVE_ERROR = "config.saveDialog.error";
    public static final String CONFIG_APPLY_ERROR = "config.apply.error";
    public static final String CONFIG_VALIDATE_ERROR = "config.validate.error";
    public static final String CONFIG_SAVE_CONFLICT = "config.save.conflict";

    public static final String AGENT_STATE_DETACHED = "agent.state.detached";
    public static final String AGENT_STATE_ATTACHED = "agent.state.attached";

    public static final String AGENT_NOT_CONNECTED = "agent.unconnected.msg";

    public static final String TEMPLATE_NAME_REQUIRED = "config.template.nameRequired";

    public static final String PASSWORDS_NO_MATCH = "passwords.noMatch";


    /**
     * 
     * @param key
     *            message id
     * @return the message localized according to the JSF ViewRoot locale
     */
    public static String get ( String key ) {
        return get(GUI_MESSAGES_BASE, key);
    }


    /**
     * 
     * @param key
     *            message id
     * @param l
     *            desired locale
     * @return the message localized according to the given locale
     */
    public static String get ( String key, Locale l ) {
        return get(GUI_MESSAGES_BASE, key, l);
    }


    /**
     * @param key
     * @param args
     * @return the template formatted to the JSF ViewRoot locale
     */
    public static String format ( String key, Object... args ) {
        return format(GUI_MESSAGES_BASE, key, args);
    }


    /**
     * @param key
     * @return whether the bundle contains the given key
     */
    public static boolean contains ( String key ) {
        return contains(GUI_MESSAGES_BASE, key);
    }


    /**
     * @param guiMessagesBase
     * @param key
     * @return
     */
    private static boolean contains ( String base, String key ) {
        return ResourceBundle
                .getBundle(base, FacesContext.getCurrentInstance().getViewRoot().getLocale(), Thread.currentThread().getContextClassLoader())
                .containsKey(key);
    }


    public static String formatEL ( String key, Object a1 ) {
        return format(key, a1);
    }


    public static String formatEL ( String key, Object a1, Object a2 ) {
        return format(key, a1, a2);
    }


    public static String formatEL ( String key, Object a1, Object a2, Object a3 ) {
        return format(key, a1, a2, a3);
    }
}
