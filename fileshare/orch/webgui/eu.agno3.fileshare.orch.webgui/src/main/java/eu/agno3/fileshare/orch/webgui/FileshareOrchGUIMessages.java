/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.webgui;


import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.runtime.jsf.i18n.FacesMessageBundle;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( {
    "nls", "javadoc"
} )
@ApplicationScoped
@Named ( "fs_orch_msgs" )
public class FileshareOrchGUIMessages extends FacesMessageBundle {

    /**
     * 
     */
    public static final String GUI_MESSAGES_BASE = "eu.agno3.fileshare.orch.webgui.messages";


    /**
     * 
     */
    public FileshareOrchGUIMessages () {}


    /**
     * 
     * @param key
     *            message id
     * @return the message localized according to the JSF ViewRoot locale
     */
    public static String get ( String key ) {
        return get(GUI_MESSAGES_BASE, key, FileshareOrchGUIMessages.class.getClassLoader());
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
        return get(GUI_MESSAGES_BASE, key, l, FileshareOrchGUIMessages.class.getClassLoader());
    }


    /**
     * @param key
     * @param args
     * @return the template formatted to the JSF ViewRoot locale
     */
    public static String format ( String key, Object... args ) {
        return format(GUI_MESSAGES_BASE, key, FileshareOrchGUIMessages.class.getClassLoader(), args);
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
