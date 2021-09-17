/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.runtime.security.login;


import java.util.Collection;

import org.apache.shiro.realm.Realm;

import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public interface LoginRealm extends Realm {

    /**
     * 
     * @return the realm identifier
     */
    public String getId ();


    /**
     * 
     * @return whether this is a primary realm
     */
    public boolean isPrimary ();


    /**
     * 
     * @return the authentication type
     */
    public LoginRealmType getAuthType ();


    /**
     * 
     * @return whether this realm supports changing password
     */
    public boolean supportPasswordChange ();


    /**
     * 
     * @return the realm type
     */
    public String getType ();


    /**
     * @return the realms to run this before
     */
    public Collection<String> getBefore ();


    /**
     * @return the realms to run this after
     */
    public Collection<String> getAfter ();


    /**
     * 
     * @param ctx
     * @return whether this realm is applicable for the given login context
     */
    public boolean isApplicable ( LoginContext ctx );


    /**
     * 
     * @param ctx
     * @param sess
     * @return authentication result
     */
    public AuthResponse authenticate ( LoginContext ctx, LoginSession sess );


    /**
     * @param ctx
     * @param up
     * @param sess
     * @return authentication result
     */
    public AuthResponse changePassword ( LoginContext ctx, UserPrincipal up, LoginSession sess );


    /**
     * @param ctx
     * @param sess
     * @return authentication result
     */
    public AuthResponse postauth ( LoginContext ctx, LoginSession sess );


    /**
     * @param ctx
     * @param sess
     * @return authentication result
     */
    public AuthResponse preauth ( LoginContext ctx, LoginSession sess );

}
