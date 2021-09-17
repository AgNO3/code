/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.runtime.security.login;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.SimpleAuthenticationInfo;

import eu.agno3.runtime.security.AuthPhase;


/**
 * @author mbechler
 *
 */
public class LoginSession implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2388469592597468739L;

    private static final Logger log = Logger.getLogger(LoginSession.class);

    private String selectedRealmId;

    private List<LoginChallenge<?>> challenges = new ArrayList<>();

    private SimpleAuthenticationInfo mergedAuthInfo;
    private Map<String, AuthenticationInfo> realmAuthInfo = new HashMap<>();
    private LoginContext loginContext;
    private Integer throttleDelay;
    private Map<AuthPhase, Map<String, AuthResponse>> phaseResponses = new HashMap<>();


    /**
     * 
     */
    public LoginSession () {}


    /**
     * @return the loginContext
     */
    public LoginContext getLoginContext () {
        return this.loginContext;
    }


    /**
     * @param loginContext
     *            the loginContext to set
     */
    public void setLoginContext ( LoginContext loginContext ) {
        this.loginContext = loginContext;
    }


    /**
     * @return the currently active throttle delay
     */
    public Integer getThrottleDelay () {
        return this.throttleDelay;
    }


    /**
     * @param throttleDelay
     *            the throttleDelay to set
     */
    public void setThrottleDelay ( Integer throttleDelay ) {
        this.throttleDelay = throttleDelay;
    }


    /**
     * @return the challenges
     */
    public List<LoginChallenge<?>> getChallenges () {
        return Collections.unmodifiableList(this.challenges);
    }


    /**
     * @param challenge
     */
    public void addChallenge ( LoginChallenge<?> challenge ) {
        this.challenges.add(challenge);
    }


    /**
     * @param challenges
     *            the challenges to set
     */
    public void setChallenges ( List<LoginChallenge<?>> challenges ) {
        this.challenges = new ArrayList<>(challenges);
    }


    /**
     * 
     * @return whether this session holds state
     */
    public boolean haveState () {
        if ( this.phaseResponses.isEmpty() || this.challenges.isEmpty() ) {
            return false;
        }

        for ( LoginChallenge<?> loginChallenge : this.challenges ) {
            if ( loginChallenge.isComplete() ) {
                return true;
            }
        }
        return false;
    }


    /**
     * 
     * @param type
     * @param id
     * @return the specified challenge
     */
    @SuppressWarnings ( "unchecked" )
    public <T extends LoginChallenge<?>> T getChallenge ( Class<T> type, String id ) {
        for ( LoginChallenge<?> ch : this.challenges ) {
            if ( type.isAssignableFrom(ch.getClass()) && Objects.equals(id, ch.getId()) ) {
                return (T) ch;
            }
        }
        return null;
    }


    /**
     * @param realm
     * @param ai
     */
    public void storeAuthInfo ( LoginRealm realm, AuthenticationInfo ai ) {
        if ( this.realmAuthInfo == null ) {
            this.realmAuthInfo = new HashMap<>();
        }

        this.realmAuthInfo.put(realm.getId(), ai);
        if ( this.mergedAuthInfo == null ) {
            this.mergedAuthInfo = new SimpleAuthenticationInfo();
        }
        this.mergedAuthInfo.merge(ai);
    }


    /**
     * @param realm
     * @return the stored auth info
     */
    public AuthenticationInfo restoreAuthInfo ( LoginRealm realm ) {
        return this.realmAuthInfo.get(realm.getId());
    }


    /**
     * @return the merged authentication info
     */
    public AuthenticationInfo getMergedAuthInfo () {
        return this.mergedAuthInfo;
    }


    /**
     * @return the selectedRealmId
     */
    public String getSelectedRealmId () {
        return this.selectedRealmId;
    }


    /**
     * @param selectedRealmId
     *            the selectedRealmId to set
     */
    public void setSelectedRealmId ( String selectedRealmId ) {
        this.selectedRealmId = selectedRealmId;
    }


    /**
     * Clears the sensitive data from this object
     */
    public void clear () {
        log.debug("Clearing challenges"); //$NON-NLS-1$
        this.challenges.clear();
        this.phaseResponses.clear();
    }


    /**
     * Destroy the login session
     */
    public void destroy () {}


    /**
     * Regenerate the login session's id
     */
    public void regenerateId () {}


    /**
     * @param p
     * @return the latest responses for the phase
     */
    public Map<String, AuthResponse> getPhaseResponses ( AuthPhase p ) {
        Map<String, AuthResponse> map = this.phaseResponses.get(p);
        if ( map == null ) {
            map = new HashMap<>();
            this.phaseResponses.put(p, map);
        }
        return map;
    }

}
