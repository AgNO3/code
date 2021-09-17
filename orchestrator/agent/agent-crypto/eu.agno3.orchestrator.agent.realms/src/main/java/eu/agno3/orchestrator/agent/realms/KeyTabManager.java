/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.UserPrincipal;
import java.util.Collection;
import java.util.Set;

import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;

import eu.agno3.orchestrator.system.account.util.UnixAccountException;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.KeyTab;


/**
 * @author mbechler
 *
 */
public interface KeyTabManager {

    /**
     * @return the keytab alias
     */
    String getAlias ();


    /**
     * @return the path to the keytab
     */
    Path getPath ();


    /**
     * @param key
     */
    void addKey ( KerberosKey key );


    /**
     * @param keys
     */
    void addKeys ( Collection<KerberosKey> keys );


    /**
     * @param kt
     */
    void addKeyTab ( KeyTab kt );


    /**
     * 
     * @param princ
     * @param password
     * @param algo
     */
    void addPasswordKey ( KerberosPrincipal princ, String password, String algo );


    /**
     * 
     * @param princ
     * @param kvno
     * @param keyType
     */
    void removeKey ( KerberosPrincipal princ, long kvno, int keyType );


    /**
     * @param princ
     * @param kvno
     */
    void removeKeys ( KerberosPrincipal princ, long kvno );


    /**
     * 
     * @param princ
     */
    void removeKeys ( KerberosPrincipal princ );


    /**
     * 
     * @return the keys
     */
    Collection<KerberosKey> listKeys ();


    /**
     * 
     * @throws IOException
     */
    void delete () throws IOException;


    /**
     * 
     * @throws IOException
     */
    void save () throws IOException;


    /**
     * @param user
     * @throws UnixAccountException
     * @throws KerberosException
     */
    void revokeUser ( UserPrincipal user ) throws UnixAccountException, KerberosException;


    /**
     * @param user
     * @throws UnixAccountException
     * @throws KerberosException
     */
    void allowUser ( UserPrincipal user ) throws UnixAccountException, KerberosException;


    /**
     * @return the user allowed to access the keytab
     * @throws UnixAccountException
     * @throws KerberosException
     */
    Set<UserPrincipal> getAllowedUsers () throws UnixAccountException, KerberosException;


    /**
     * @return whether the the keytab file exists
     */
    boolean exists ();

}