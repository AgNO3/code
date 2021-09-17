/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.01.2017 by mbechler
 */
package eu.agno3.runtime.ldap.client;


import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPURL;
import com.unboundid.ldap.sdk.RDN;

import eu.agno3.runtime.net.krb5.UserPasswordGSSAPIFactory;


/**
 * @author mbechler
 *
 */
public final class LDAPConfigUtil {

    private static final Logger log = Logger.getLogger(LDAPConfigUtil.class);

    /**
     * Service name for LDAP SRV lookups
     */
    public static final String SRV_SERVICE_DEFAULT = "_ldap._tcp"; //$NON-NLS-1$

    /**
     * Service name for ActiveDirectory LDAP SRV lookups
     */
    public static final String SRV_SERVICE_AD = "_ldap._tcp.dc._msdcs"; //$NON-NLS-1$


    /**
     * 
     */
    private LDAPConfigUtil () {}


    /**
     * @param specs
     * @param ssl
     * @param startTls
     * @return parsed server list
     * @throws LDAPConfigurationException
     */
    public static List<LDAPURL> parseServerList ( List<URI> specs, boolean ssl, boolean startTls ) throws LDAPConfigurationException {
        return parseServerList(
            specs.stream().map(x -> x.toASCIIString()).collect(Collectors.toList()).toArray(new String[specs.size()]),
            ssl,
            startTls);
    }


    /**
     * @param specs
     * @param ssl
     * @param startTls
     * @return parsed server list
     * @throws LDAPConfigurationException
     */
    public static List<LDAPURL> parseServerList ( String[] specs, boolean ssl, boolean startTls ) throws LDAPConfigurationException {
        List<LDAPURL> servs = new ArrayList<>();

        boolean foundSSL = false;
        boolean foundStartTLS = false;
        boolean foundPlain = false;

        for ( String spec : specs ) {
            try {
                LDAPURL ldapurl = new LDAPURL(spec);

                if ( "ldap".equals(ldapurl.getScheme()) ) { //$NON-NLS-1$
                    foundPlain = true;
                }
                else if ( "ldaps".equals(ldapurl.getScheme()) ) { //$NON-NLS-1$
                    foundSSL = true;
                }
                else if ( "ldap+tls".equals(ldapurl.getScheme()) ) { //$NON-NLS-1$
                    foundStartTLS = true;
                }

                servs.add(ldapurl);
            }
            catch ( LDAPException e ) {
                log.warn("Failed to parse LDAP server URL", e); //$NON-NLS-1$
            }
        }

        if ( servs.isEmpty() ) {
            throw new LDAPConfigurationException("No servers could be parsed"); //$NON-NLS-1$
        }

        int diffProtos = ( foundSSL ? 1 : 0 ) + ( foundStartTLS ? 1 : 0 ) + ( foundPlain ? 1 : 0 );
        if ( diffProtos > 1 ) {
            throw new LDAPConfigurationException("Pool servers cannot have different protocols"); //$NON-NLS-1$
        }
        return servs;
    }


    /**
     * @param domain
     * @return default baseDN for active directory servers
     */
    public static DN getActiveDirectoryDefaultBase ( String domain ) {
        String[] dcs = StringUtils.split(domain, '.');
        List<RDN> rdns = new LinkedList<>();
        for ( String dc : dcs ) {
            rdns.add(new RDN("dc", dc)); //$NON-NLS-1$
        }
        return new DN(rdns);
    }


    /**
     * 
     * @param mech
     * @return default SASL options
     */
    public static Map<String, Object> getDefaultSASLOptions ( String mech ) {
        return Collections.EMPTY_MAP;
    }


    /**
     * 
     * @return sasl mechanisms supported
     */
    public static String[] getSupportedSASLMechanisms () {
        return new String[] {
            "PLAIN", //$NON-NLS-1$
            "GSSAPI", //$NON-NLS-1$
            "CRAM-MD5", //$NON-NLS-1$
            "DIGEST-MD5", //$NON-NLS-1$
            "ANONYMOUS", //$NON-NLS-1$
        };
    }


    /**
     * 
     * @param mech
     * @return whether the machanism supports QOP
     */
    public static boolean mechanismSupportsQOP ( String mech ) {
        return "GSSAPI".equals(mech) || "NTLM".equals(mech); //$NON-NLS-1$ //$NON-NLS-2$
    }


    /**
     * @param username
     * @param domain
     * @param password
     * @return gssapi factory for user authentication
     */
    public static UserPasswordGSSAPIFactory makeClientUserPasswordGSSAPIFactory ( String username, String domain, String password ) {
        return makeUserPasswordGSSAPIFactory(username, domain, password, true, Collections.EMPTY_MAP);
    }


    /**
     * @param username
     * @param domain
     * @param password
     * @param clientOnly
     * @param overrideSalt
     * @return gssapi factory for user authentication
     */
    public static UserPasswordGSSAPIFactory makeUserPasswordGSSAPIFactory ( String username, String domain, String password, boolean clientOnly,
            Map<Integer, String> overrideSalt ) {
        String princ = username;
        if ( princ.indexOf('@') < 0 && !StringUtils.isBlank(domain) ) {
            princ += "@" + domain.toUpperCase(Locale.ROOT); //$NON-NLS-1$
        }
        return new UserPasswordGSSAPIFactory(princ, password, clientOnly, overrideSalt);
    }

}
