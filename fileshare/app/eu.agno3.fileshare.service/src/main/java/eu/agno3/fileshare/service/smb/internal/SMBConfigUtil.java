/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2017 by mbechler
 */
package eu.agno3.fileshare.service.smb.internal;


import java.io.IOException;
import java.util.Dictionary;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.ad.ADRealm;
import eu.agno3.runtime.net.ad.SubjectKerb5Authenticator;
import eu.agno3.runtime.net.krb5.GSSAPISubjectFactory;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.KerberosRealm;
import eu.agno3.runtime.net.krb5.UserPasswordGSSAPIFactory;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.util.net.LocalHostUtil;

import jcifs.CIFSContext;
import jcifs.Credentials;
import jcifs.smb.NtlmPasswordAuthenticator;


/**
 * @author mbechler
 *
 */
public final class SMBConfigUtil {

    private static final Logger log = Logger.getLogger(SMBConfigUtil.class);


    /**
     * 
     */
    private SMBConfigUtil () {}


    /**
     * @param baseCtx
     * @param rlm
     * @param properties
     * @return context
     */
    public static CIFSContext configureAuth ( CIFSContext baseCtx, KerberosRealm rlm, Dictionary<String, Object> properties ) {
        Credentials creds = null;
        boolean useRealm = ConfigUtil.parseBoolean(properties, "useRealm", false); //$NON-NLS-1$
        String user = ConfigUtil.parseString(properties, "user", null); //$NON-NLS-1$
        String domain = ConfigUtil.parseString(properties, "domain", null); //$NON-NLS-1$
        String password;
        try {
            password = ConfigUtil.parseSecret(properties, "password", null); //$NON-NLS-1$
        }
        catch ( IOException e ) {
            log.error("Failed to read SMB password", e); //$NON-NLS-1$
            return baseCtx.withGuestCrendentials();
        }
        String keytab = ConfigUtil.parseString(properties, "keytab", null); //$NON-NLS-1$
        boolean alwaysUseKerberos = ConfigUtil.parseBoolean(properties, "useKerberos", false); //$NON-NLS-1$
        if ( useRealm && rlm != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Setting up using %s realm %s", rlm.getRealmType(), rlm.getKrbRealm())); //$NON-NLS-1$
            }
            try {
                if ( ( !StringUtils.isBlank(keytab) && !StringUtils.isBlank(user) ) || alwaysUseKerberos ) {
                    GSSAPISubjectFactory subjectFactory = rlm.getInitiatorSubjectFactory(keytab, domain, user, password);
                    if ( subjectFactory == null ) {
                        throw new KerberosException("Faileds to find CIFS client credentials"); //$NON-NLS-1$
                    }
                    creds = new SubjectKerb5Authenticator(subjectFactory);
                }
                else if ( rlm instanceof ADRealm ) {
                    ADRealm r = (ADRealm) rlm;
                    creds = new NtlmPasswordAuthenticator(r.getDomainName(), r.getMachineAccount(), r.getMachinePassword());
                }
            }
            catch (
                ADException |
                KerberosException e ) {
                log.error("Failed to setup kerberos authentication", e); //$NON-NLS-1$
                return baseCtx.withGuestCrendentials();
            }
        }
        else if ( useRealm && rlm == null ) {
            log.error("Realm/Domain not correctly set up"); //$NON-NLS-1$
            return baseCtx.withGuestCrendentials();
        }
        else if ( !StringUtils.isBlank(user) && password != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("User authentication %s@%s (kerberos: %s)", user, domain, alwaysUseKerberos)); //$NON-NLS-1$
            }
            if ( alwaysUseKerberos && domain != null ) {
                creds = new SubjectKerb5Authenticator(new UserPasswordGSSAPIFactory(user, password, domain.toUpperCase(Locale.ROOT)));
            }
            else if ( alwaysUseKerberos ) {
                log.error("Missing domain for kerberos authentication"); //$NON-NLS-1$
                return baseCtx.withGuestCrendentials();
            }
            else {
                creds = new NtlmPasswordAuthenticator(domain, user, password);
            }
        }
        else {
            log.debug("Falling back to guest credentials"); //$NON-NLS-1$
            return baseCtx.withGuestCrendentials();
        }
        return baseCtx.withCredentials(creds);
    }


    /**
     * @return guessed netbios host name
     */
    public static String getNetbiosHostName () {
        String hostName = LocalHostUtil.guessPrimaryHostName();
        if ( !StringUtils.isBlank(hostName) ) {
            if ( Character.isDigit(hostName.charAt(0)) ) {
                // ip address, cannot use
                hostName = null;
            }
            else {
                int sep = hostName.indexOf('.');
                if ( sep >= 0 ) {
                    hostName = hostName.substring(0, sep);
                }
                hostName = hostName.toUpperCase(Locale.ROOT);
            }
        }
        return hostName;
    }
}
