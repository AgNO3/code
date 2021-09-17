/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 30, 2017 by mbechler
 */
package eu.agno3.runtime.net.krb5.internal;


import java.util.Dictionary;
import java.util.Locale;

import javax.security.auth.kerberos.KerberosPrincipal;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;

import eu.agno3.runtime.net.krb5.GSSAPISubjectFactory;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.KerberosRealm;
import eu.agno3.runtime.net.krb5.KerberosRealmConfig;
import eu.agno3.runtime.net.krb5.KeyTabGSSAPIFactory;
import eu.agno3.runtime.net.krb5.RealmType;
import eu.agno3.runtime.net.krb5.UserPasswordGSSAPIFactory;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.util.net.LocalHostUtil;


/**
 * @author mbechler
 *
 */
public abstract class AbstractKerberosRealmImpl implements KerberosRealm {

    private static final Logger log = Logger.getLogger(AbstractKerberosRealmImpl.class);

    private String krbRealm;
    private RealmType realmType;
    private String adminServer;
    private String kpasswdServer;
    private String localHostname;
    private int authFactors;


    /**
     * @param ctx
     * @return
     */
    protected String setupRealm ( ComponentContext ctx ) {
        Dictionary<String, Object> cfg = ctx.getProperties();
        String instanceId = ConfigUtil.parseString(cfg, "instanceId", null); //$NON-NLS-1$
        String realm = ConfigUtil.parseString(cfg, "realm", instanceId); //$NON-NLS-1$
        if ( StringUtils.isBlank(realm) ) {
            log.error("No realm configured"); //$NON-NLS-1$
            return null;
        }
        this.krbRealm = realm;
        return realm;
    }


    /**
     * @param realm
     * @param check
     * @param realmConfig
     */
    protected void configure ( KerberosRealmConfig realmConfig, boolean check ) {
        setupHostname(realmConfig.getOverrideLocalHostname(), realmConfig.getRealm().toLowerCase(Locale.ROOT), check);
        this.adminServer = realmConfig.getAdminServer();
        this.kpasswdServer = realmConfig.getKpasswdServer();
        this.realmType = realmConfig.getRealmType();
        this.authFactors = realmConfig.getAuthFactors();
    }


    /**
     * @param autoDetect
     * @return
     */
    private static boolean isIpAddress ( String autoDetect ) {
        if ( autoDetect.indexOf(':') >= 0 ) {
            return true;
        }

        int lastDot = autoDetect.lastIndexOf('.', 1);
        int end = autoDetect.charAt(autoDetect.length() - 1) == '.' ? autoDetect.length() : autoDetect.length() - 1;

        if ( lastDot >= 0 && StringUtils.isNumeric(autoDetect.substring(lastDot, end)) ) {
            return true;
        }
        return false;
    }


    protected void setupHostname ( String overrideHost, String domainName, boolean check ) {
        if ( overrideHost != null ) {
            this.localHostname = overrideHost;
        }
        else {
            String autoDetect = LocalHostUtil.guessPrimaryHostName().toLowerCase(Locale.ROOT);

            if ( check && isIpAddress(autoDetect) ) {
                log.error("Could not get canonical host name"); //$NON-NLS-1$
                return;
            }

            if ( check && !autoDetect.endsWith("." + domainName) ) { //$NON-NLS-1$
                log.warn(
                    String.format(
                        "Canonical hostname %s is not inside domain %s, " //$NON-NLS-1$
                                + "this will require additional client realm mapping configuration to work", //$NON-NLS-1$
                        autoDetect,
                        domainName));
            }

            this.localHostname = autoDetect;
        }
    }


    /**
     * 
     */
    public AbstractKerberosRealmImpl () {
        super();
    }


    /**
     * @param realmName
     */
    public AbstractKerberosRealmImpl ( String realmName ) {
        setKrbRealm(realmName);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealm#getInitiatorSubjectFactory(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public GSSAPISubjectFactory getInitiatorSubjectFactory ( String keytab, String realm, String user, String password ) throws KerberosException {
        String actualRealm = !StringUtils.isBlank(realm) ? realm : this.getKrbRealm();
        if ( StringUtils.isBlank(keytab) ) {
            if ( !StringUtils.isBlank(user) && !StringUtils.isBlank(password) ) {
                return new UserPasswordGSSAPIFactory(user, password, actualRealm);
            }
            // no credentials configured
            return null;
        }

        if ( !StringUtils.isBlank(user) ) {
            String princName = user;
            if ( princName.indexOf('@') < 0 ) {
                princName = String.format("%s@%s", user, actualRealm); //$NON-NLS-1$
            }
            KerberosPrincipal princ = new KerberosPrincipal(princName);
            return new KeyTabGSSAPIFactory(this.getKeytab(keytab, princ));
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealm#getKrbRealm()
     */
    @Override
    public String getKrbRealm () {
        return this.krbRealm;
    }


    /**
     * @param krbRealm
     *            the krbRealm to set
     */
    protected void setKrbRealm ( String krbRealm ) {
        this.krbRealm = krbRealm;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealm#getRealmType()
     */
    @Override
    public RealmType getRealmType () {
        return this.realmType;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealm#getLocalHostname()
     */
    @Override
    public String getLocalHostname () {
        return this.localHostname;
    }


    /**
     * @param localHostname
     *            the localHostname to set
     */
    public void setLocalHostname ( String localHostname ) {
        this.localHostname = localHostname;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealm#getAdminServer()
     */
    @Override
    public String getAdminServer () {
        return this.adminServer;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealm#getKpasswdServer()
     */
    @Override
    public String getKpasswdServer () {
        return this.kpasswdServer;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealm#getAuthFactors()
     */
    @Override
    public int getAuthFactors () {
        return this.authFactors;
    }

}