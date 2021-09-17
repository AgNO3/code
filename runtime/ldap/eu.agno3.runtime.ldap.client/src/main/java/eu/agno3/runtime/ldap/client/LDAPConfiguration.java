/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2015 by mbechler
 */
package eu.agno3.runtime.ldap.client;


import java.util.List;
import java.util.Map;

import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.LDAPURL;

import eu.agno3.runtime.net.krb5.GSSAPISubjectFactory;


/**
 * @author mbechler
 *
 */
public interface LDAPConfiguration {

    /**
     * 
     */
    public static final String PID = "ldap"; //$NON-NLS-1$


    /**
     * @return the ldap configuration id
     */
    String getInstanceId ();


    /**
     * @return the default search base DN
     */
    DN getDefaultBaseDN ();


    /**
     * @return a SRV name to use for resolving servers, instead of a static list
     */
    String getSRVRecord ();


    /**
     * 
     * @return time to live for SRV records
     */
    int getSRVTTL ();


    /**
     * @return the SRV domain name
     */
    String getSRVDomain ();


    /**
     * @return the servers to connect to
     */
    List<LDAPURL> getLDAPServers ();


    /**
     * @return the tls context name
     * 
     */
    String getTLSContextName ();


    /**
     * @return whether to use start tls security
     */
    boolean useStartTLS ();


    /**
     * @return wehther to use SSL security
     */
    boolean useSSL ();


    /**
     * @return the initial pool size
     */
    int getInitialPoolSize ();


    /**
     * @return the maximum pool size
     */
    int getMaxPoolSize ();


    /**
     * @return whether to ignore connection failures when setting up the pool
     */
    boolean ignoreInitialConnectFail ();


    /**
     * @return whether to perform an anonymous bind
     */
    boolean isAnonymous ();


    /**
     * @return the bind dn
     */
    DN getBindDN ();


    /**
     * @return the bind password
     */
    String getBindPassword ();


    /**
     * @return the controls to send when binding
     */
    Control[] getAuthControls ();


    /**
     * @return whether to use sasl authentication
     */
    boolean useSASLAuth ();


    /**
     * @return the SASL mechanism
     */
    String getSASLMechanism ();


    /**
     * @return the SASL authentication ID
     */
    String getSASLAuthID ();


    /**
     * @return the SASL realm
     */
    String getSASLRealm ();


    /**
     * @return custom SASL options
     */
    Map<String, Object> getSASLOptions ();


    /**
     * @return a subject to use
     */
    GSSAPISubjectFactory getGSSAPISubjectFactory ();


    /**
     * @return the response timeout
     */
    long getResponseTimeout ();


    /**
     * @return the connect timeout
     */
    long getConnectionTimeout ();

}
