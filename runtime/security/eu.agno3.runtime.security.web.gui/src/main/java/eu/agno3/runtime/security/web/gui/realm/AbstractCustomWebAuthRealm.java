/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.01.2015 by mbechler
 */
package eu.agno3.runtime.security.web.gui.realm;


import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.security.login.LoginContext;
import eu.agno3.runtime.security.login.LoginSession;
import eu.agno3.runtime.security.web.gui.LoginMessages;
import eu.agno3.runtime.security.web.login.CustomWebAuthRealm;
import eu.agno3.runtime.security.web.login.WebLoginConfig;


/**
 * @author mbechler
 *
 */
public abstract class AbstractCustomWebAuthRealm implements CustomWebAuthRealm {

    private static final Logger log = Logger.getLogger(AbstractCustomWebAuthRealm.class);


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginRealm#isApplicable(eu.agno3.runtime.security.login.LoginContext)
     */
    @Override
    public boolean isApplicable ( LoginContext ctx ) {
        return true;
    }


    /**
     * 
     * @param locale
     * @return the resource bundle to use for localizing this realms failure messages
     */
    protected ResourceBundle getMessageResourceBundle ( Locale locale ) {
        return ResourceBundle.getBundle("eu.agno3.runtime.security.web.gui.messages", locale); //$NON-NLS-1$
    }


    /**
     * 
     * @return
     */
    protected String getFailMessageBase () {
        return "external.fail."; //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.web.login.CustomWebAuthRealm#handleNonSuccessReturn(java.lang.String,
     *      java.util.Locale)
     */
    @Override
    public String handleNonSuccessReturn ( String returnParam, Locale l ) {
        return this.makeReturnMessage(returnParam, l);
    }


    /**
     * @param returnParam
     * @param l
     * @return
     */
    private String makeReturnMessage ( String returnParam, Locale l ) {

        if ( "general".equals(returnParam) ) { //$NON-NLS-1$
            return LoginMessages.get(LoginMessages.FAIL_EXTERNAL, l);
        }

        try {
            ResourceBundle b = this.getMessageResourceBundle(l);
            return b.getString(this.getFailMessageBase() + returnParam);
        }
        catch ( Exception e ) {
            log.warn("Failed to get localized error message", e); //$NON-NLS-1$
        }
        return returnParam;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.runtime.security.web.login.CustomWebAuthRealm#doAuthentication(eu.agno3.runtime.security.web.login.WebLoginConfig,
     *      eu.agno3.runtime.security.login.LoginContext, eu.agno3.runtime.security.login.LoginSession)
     */
    @Override
    public String doAuthentication ( WebLoginConfig config, LoginContext loginContext, LoginSession loginSession ) throws IOException {
        return StringUtils.EMPTY;
    }

}
