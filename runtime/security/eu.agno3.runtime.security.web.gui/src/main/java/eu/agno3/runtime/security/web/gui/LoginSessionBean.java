/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.runtime.security.web.gui;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.SessionScoped;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.web.util.WebUtils;

import eu.agno3.runtime.security.login.LoginSession;


/**
 * @author mbechler
 *
 */
@SessionScoped
public class LoginSessionBean extends LoginSession {

    /**
     * 
     */
    private static final long serialVersionUID = -1437678788116404660L;
    private boolean disabledAutoLogin;
    private List<String> applicableRealms;


    /**
     * @return whether automatic login mechanisms should not be used
     */
    public boolean getDisabledAutoLogin () {
        return this.disabledAutoLogin;
    }


    /**
     * @param disabledAutoLogin
     *            the disabledAutoLogin to set
     */
    public void setDisabledAutoLogin ( boolean disabledAutoLogin ) {
        this.disabledAutoLogin = disabledAutoLogin;
    }


    /**
     * @param applicableRealms
     */
    public void setApplicableRealms ( List<String> applicableRealms ) {
        this.applicableRealms = applicableRealms;
    }


    /**
     * @return the applicableRealms
     */
    public List<String> getApplicableRealms () {
        return this.applicableRealms;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginSession#clear()
     */
    @Override
    public void clear () {
        super.clear();
    }


    @Override
    public void destroy () {
        Session session = SecurityUtils.getSubject().getSession();
        if ( session != null ) {
            // make sure not to lose the saved request
            Object savedRequest = session.getAttribute(WebUtils.SAVED_REQUEST_KEY);
            session.stop();
            session = SecurityUtils.getSubject().getSession(true);
            session.setAttribute(WebUtils.SAVED_REQUEST_KEY, savedRequest);
        }
    }


    @Override
    public void regenerateId () {
        Session session = SecurityUtils.getSubject().getSession();
        if ( session != null ) {
            Map<Object, Object> sessionData = new HashMap<>();
            for ( Object key : session.getAttributeKeys() ) {
                sessionData.put(key, session.getAttribute(key));
            }
            session.stop();
            session = SecurityUtils.getSubject().getSession(true);
            for ( Entry<Object, Object> e : sessionData.entrySet() ) {
                session.setAttribute(e.getKey(), e.getValue());
            }
        }
    }
}
