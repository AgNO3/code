/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.04.2015 by mbechler
 */
package eu.agno3.runtime.net.krb5;


import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KeyTab;


/**
 * @author mbechler
 *
 */
public interface KerberosRealm {

    /**
     * @return the kerberos realm name
     */
    String getKrbRealm ();


    /**
     * 
     * @return the local hostname
     */
    String getLocalHostname ();


    /**
     * 
     * @return the realm type
     */
    RealmType getRealmType ();


    /**
     * 
     * @return the admin server
     */
    String getAdminServer ();


    /**
     * 
     * @return the kpasswd server
     */
    String getKpasswdServer ();


    /**
     * @param keyTabId
     * @param servicePrincipal
     * @return a bound keytab
     * @throws KerberosException
     */
    KeyTab getKeytab ( String keyTabId, KerberosPrincipal servicePrincipal ) throws KerberosException;


    /**
     * 
     * @param keyTabId
     * @return an unbound keytab
     * @throws KerberosException
     */
    KeyTab getUnboundKeytab ( String keyTabId ) throws KerberosException;


    /**
     * @return the number of auth factors someone authenticated via this realm is assumed to have
     */
    int getAuthFactors ();


    /**
     * @param keytab
     * @param domain
     * @param user
     * @param password
     * @return a gssapi subject factory with proper credentials
     * @throws KerberosException
     */
    GSSAPISubjectFactory getInitiatorSubjectFactory ( String keytab, String domain, String user, String password ) throws KerberosException;

}
