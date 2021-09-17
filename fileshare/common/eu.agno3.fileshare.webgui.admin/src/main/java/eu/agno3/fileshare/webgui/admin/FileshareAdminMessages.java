/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin;


import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.runtime.jsf.i18n.FacesMessageBundle;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( {
    "javadoc", "nls"
} )
@ApplicationScoped
@Named ( "fs_adm_msgs" )
public class FileshareAdminMessages extends FacesMessageBundle {

    public static final String ADMIN_MESSAGES_BASE = "eu.agno3.fileshare.webgui.admin.messages";
    public static final String USER_CREATED = "user.created";
    public static final String PASSWORDS_DO_NOT_MATCH = "password.confirmNoMatch";
    public static final String UNLABELED_ENTITY = "entity.unlabeled";
    public static final String PASSWORD_REQUIRED = "password.required";


    /**
     * 
     * @param key
     *            message id
     * @return the message localized according to the JSF ViewRoot locale
     */
    public static String get ( String key ) {
        return get(ADMIN_MESSAGES_BASE, key);
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
        return get(ADMIN_MESSAGES_BASE, key, l);
    }


    /**
     * @param key
     * @param args
     * @return the template formatted to the JSF ViewRoot locale
     */
    public static String format ( String key, Object... args ) {
        return format(ADMIN_MESSAGES_BASE, key, args);
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
