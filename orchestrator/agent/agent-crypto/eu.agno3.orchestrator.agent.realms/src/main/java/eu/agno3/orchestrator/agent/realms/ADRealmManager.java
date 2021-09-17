/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms;


import java.nio.file.Path;
import java.nio.file.attribute.UserPrincipal;

import javax.security.auth.Subject;

import eu.agno3.orchestrator.system.account.util.UnixAccountException;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.security.credentials.WrappedCredentials;


/**
 * @author mbechler
 *
 */
public interface ADRealmManager extends RealmManager {

    /**
     * 
     * @return whether the domain is joined
     * @throws ADException
     */
    boolean isJoined () throws ADException;


    /**
     * @param s
     * @throws ADException
     */
    void joinDomain ( Subject s ) throws ADException;


    /**
     * @param user
     * @param password
     * @throws ADException
     */
    void joinDomain ( String user, String password ) throws ADException;


    /**
     * @param creds
     * @throws ADException
     */
    void joinDomain ( WrappedCredentials creds ) throws ADException;


    /**
     * @param machinePassword
     * @throws ADException
     * 
     */
    void joinDomainWithMachinePassword ( String machinePassword ) throws ADException;


    /**
     * @throws ADException
     */
    void rekey () throws ADException;


    /**
     * @param s
     * @throws ADException
     */
    void leave ( Subject s ) throws ADException;


    /**
     * @param user
     * @param password
     * @throws ADException
     */
    void leave ( String user, String password ) throws ADException;


    /**
     * @param creds
     * @throws ADException
     */
    void leave ( WrappedCredentials creds ) throws ADException;


    /**
     * @return the path to the host keytab
     * @throws ADException
     * 
     */
    Path getHostKeytab () throws ADException;


    /**
     * @param user
     * @throws UnixAccountException
     * @throws KerberosException
     */
    void allowHostUser ( UserPrincipal user ) throws UnixAccountException, KerberosException;


    /**
     * @param user
     * @throws UnixAccountException
     * @throws KerberosException
     */
    void revokeHostUser ( UserPrincipal user ) throws UnixAccountException, KerberosException;

}