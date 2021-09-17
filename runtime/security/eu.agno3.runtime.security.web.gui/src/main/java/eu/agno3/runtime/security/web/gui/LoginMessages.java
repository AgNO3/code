/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2014 by mbechler
 */
package eu.agno3.runtime.security.web.gui;


import java.util.Locale;

import eu.agno3.runtime.jsf.i18n.FacesMessageBundle;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( {
    "javadoc", "nls"
} )
public final class LoginMessages extends FacesMessageBundle {

    public static final String LOGIN_MESSAGES_BASE = "eu.agno3.runtime.security.web.gui.messages";
    public static final String FAILURE_DETAIL = "failureDetail";
    public static final String FAILURE = "failure";
    public static final String USERNAME_REQUIRED_DETAIL_MSG = "usernameRequiredDetail";
    public static final String USERNAME_REQUIRED_MSG = "usernameRequired";

    public static final String PW_CHANGE_REQUIRED = "pwChange.required";
    public static final String PW_CHANGE_FAIL_POLICY = "pwChange.fail.policy";
    public static final String PW_CHANGE_PASSWORDS_NO_MATCH = "pwChange.passwordsNoMatch";

    public static final String PW_CHANGE_UNAVAILABLE = "pwChange.unavailable";

    public static final String PW_CHANGED = "pwChange.complete";

    public static final String FAIL_UNKNOWN = "auth.fail.unknown";
    public static final String FAIL_UNAVAILABLE = "auth.fail.unavailable";
    public static final String FAIL_DISABLED_ACCOUNT = "auth.fail.disabledAccount";
    public static final String FAIL_WRONG_CREDENTIALS = "auth.fail.wrongCredentials";
    public static final String FAIL_INTERNAL = "auth.fail.internal";
    public static final String FAIL_EXTERNAL = "auth.fail.external";
    public static final String FAIL_EXTERNAL_SUCCESS = "auth.fail.external.success";
    public static final String FAIL_EXTERNAL_DO = "auth.fail.external.do";
    public static final String PW_POLICY_FAIL = "auth.fail.policy";
    public static final String PW_EXPIRED = "auth.fail.pwexpired";

    public static final String LICENSEEXCEEDED = "auth.fail.licenseExceeded";
    public static final String UNCONFIGURED = "auth.fail.unconfigured";

    public static final String DISALLOWED = "auth.fail.disallowed";


    private LoginMessages () {}


    /**
     * 
     * @param key
     *            message id
     * @return the message localized according to the JSF ViewRoot locale
     */
    public static String get ( String key ) {
        return get(LOGIN_MESSAGES_BASE, key);
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
        return get(LOGIN_MESSAGES_BASE, key, l);
    }


    /**
     * @param key
     * @param args
     * @return the template formatted to the JSF ViewRoot locale
     */
    public static String format ( String key, Object... args ) {
        return format(LOGIN_MESSAGES_BASE, key, args);
    }
}
