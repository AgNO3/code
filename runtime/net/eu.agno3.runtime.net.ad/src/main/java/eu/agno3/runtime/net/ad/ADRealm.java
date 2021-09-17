/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad;


import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;

import eu.agno3.runtime.ldap.client.LDAPClientFactory;
import eu.agno3.runtime.net.ad.msgs.DomainPasswordInformation;
import eu.agno3.runtime.net.dns.SRVEntries;
import eu.agno3.runtime.net.krb5.KerberosRealm;
import eu.agno3.runtime.security.credentials.WrappedCredentials;
import eu.agno3.runtime.util.sid.SID;

import jcifs.CIFSContext;


/**
 * @author mbechler
 *
 */
public interface ADRealm extends KerberosRealm, LDAPClientFactory {

    /**
     * @return the domainName
     */
    String getDomainName ();


    /**
     * @return the netbiosDomainName
     */
    String getNetbiosDomainName ();


    /**
     * 
     * @return the local netbios hostname
     */
    String getLocalNetbiosHostname ();


    /**
     * @return a list of domain controller host names
     * @throws ADException
     */
    SRVEntries getDomainControllers () throws ADException;


    /**
     * @return the machinePassword
     * @throws ADException
     */
    String getMachinePassword () throws ADException;


    /**
     * @return a map of recent machine passwords
     * @throws ADException
     */
    SortedMap<Integer, String> getMachinePasswords () throws ADException;


    /**
     * @return the machine account name
     */
    String getMachineAccount ();


    /**
     * @param service
     * @return the service principal name
     */
    KerberosPrincipal makeServicePrincipal ( String service );


    /**
     * @return the host principal
     */
    KerberosPrincipal getHostPrincipal ();


    /**
     * 
     * @return the local machine sid (only if joined)
     */
    SID getMachineSid ();


    /**
     * 
     * @return the local domain sid (only if joined)
     */
    SID getDomainSid ();


    /**
     * @throws ADException
     */
    void ensureJoined () throws ADException;


    /**
     * @return a netlogon connection
     * @throws ADException
     */
    NetlogonConnection getNetlogonConnection () throws ADException;


    /**
     * @return whether a machine password is known (does not check whether it is valid)
     */
    boolean isJoined ();


    /**
     * @param realmGroup
     * @param machinePassword
     * @param realmInstanceGroup
     * @throws ADException
     */
    void joinWithMachinePassword ( String machinePassword, GroupPrincipal realmGroup, GroupPrincipal realmInstanceGroup ) throws ADException;


    /**
     * Joins or rejoins the domain
     * 
     * Resets the machine password to a random value
     * 
     * @param adminUser
     * @param adminPassword
     * @param realmGroup
     * @param hostGroup
     * @throws ADException
     */
    void join ( String adminUser, String adminPassword, GroupPrincipal realmGroup, GroupPrincipal hostGroup ) throws ADException;


    /**
     * @param subj
     * @param realmGroup
     * @param hostGroup
     * @throws ADException
     */
    void join ( Subject subj, GroupPrincipal realmGroup, GroupPrincipal hostGroup ) throws ADException;


    /**
     * @param creds
     * @param realmGroup
     * @param realmInstanceGroup
     * @throws ADException
     */
    void join ( WrappedCredentials creds, GroupPrincipal realmGroup, GroupPrincipal realmInstanceGroup ) throws ADException;


    /**
     * @param adminUser
     * @param adminPassword
     * @param realmGroup
     * @param hostGroup
     * @throws ADException
     */
    void leave ( String adminUser, String adminPassword, GroupPrincipal realmGroup, GroupPrincipal hostGroup ) throws ADException;


    /**
     * @param creds
     * @param realmGroup
     * @param hostGroup
     * @throws ADException
     */
    void leave ( WrappedCredentials creds, GroupPrincipal realmGroup, GroupPrincipal hostGroup ) throws ADException;


    /**
     * @param subj
     * @param realmGroup
     * @param hostGroup
     * @throws ADException
     */
    void leave ( Subject subj, GroupPrincipal realmGroup, GroupPrincipal hostGroup ) throws ADException;


    /**
     * Set a new random machine password
     * 
     * @param realmGroup
     * @param hostGroup
     * 
     * @throws ADException
     */
    void rekey ( GroupPrincipal realmGroup, GroupPrincipal hostGroup ) throws ADException;


    /**
     * Automatically rekeys the machine account and cleans up old keys
     * 
     * @param realmGroup
     * @param hostGroup
     * @return whether any modifications were made
     * @throws ADException
     */
    boolean runMaintenance ( GroupPrincipal realmGroup, GroupPrincipal hostGroup ) throws ADException;


    /**
     * @param servicePrincipal
     * @throws ADException
     */
    void ensureHostSPN ( KerberosPrincipal servicePrincipal ) throws ADException;


    /**
     * @param subj
     * @throws ADException
     */
    void updateDNSHostname ( Subject subj ) throws ADException;


    /**
     * @param adminUser
     * @param adminPassword
     * @throws ADException
     */
    void updateDNSHostname ( String adminUser, String adminPassword ) throws ADException;


    /**
     * @param subj
     * @throws ADException
     */
    void updateOS ( Subject subj ) throws ADException;


    /**
     * @param adminUser
     * @param adminPassword
     * @throws ADException
     */
    void updateOS ( String adminUser, String adminPassword ) throws ADException;


    /**
     * @return the DN under which the account should be stored
     */
    String getAccountDN ();


    /**
     * @return the path to the host keytab
     * @throws ADException
     */
    Path getHostKeytab () throws ADException;


    /**
     * @return the domain password information
     * @throws ADException
     */
    DomainPasswordInformation getDomainPasswordInformation () throws ADException;


    /**
     * @return a CIFS context for accessing the realms services
     */
    CIFSContext getCIFSContext ();


    /**
     * @return a CIFS context with the machine credentials
     */
    CIFSContext getMachineCIFSContext ();


    /**
     * @return whether to allow legacy crypto in protocols
     */
    boolean isAllowLegacyCrypto ();


    /**
     * @return etypes allowed for kerberos tickets
     */
    Collection<String> getAllowedETypes ();


    /**
     * 
     */
    void close ();


    /**
     * @param servicePrincipal
     * @return salt for kerberos key derivation
     */
    Map<Integer, String> getSalts ( javax.security.auth.kerberos.KerberosPrincipal servicePrincipal );

}