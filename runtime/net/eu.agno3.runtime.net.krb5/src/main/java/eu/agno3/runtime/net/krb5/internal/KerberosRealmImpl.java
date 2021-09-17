/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.04.2015 by mbechler
 */
package eu.agno3.runtime.net.krb5.internal;


import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KeyTab;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.KerberosRealm;


/**
 * @author mbechler
 *
 */
@Component ( service = KerberosRealm.class, configurationPid = "krb", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class KerberosRealmImpl extends AbstractKerberosRealmImpl implements KerberosRealm {

    private static final Logger log = Logger.getLogger(KerberosRealmImpl.class);

    private KrbRealmManagerImpl realmManager;


    @Reference
    protected synchronized void setRealmManager ( KrbRealmManagerImpl rlm ) {
        this.realmManager = rlm;
    }


    protected synchronized void unsetRealmManager ( KrbRealmManagerImpl rlm ) {
        if ( this.realmManager == rlm ) {
            this.realmManager = null;
        }
    }


    /**
     * 
     */
    public KerberosRealmImpl () {}


    /**
     * @param realmName
     * @param rlm
     * @throws KerberosException
     */
    public KerberosRealmImpl ( String realmName, KrbRealmManagerImpl rlm ) throws KerberosException {
        super(realmName);
        this.realmManager = rlm;
        setupRealmConfig(realmName, false);
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        String realm = setupRealm(ctx);
        if ( realm == null ) {
            return;
        }
        try {
            setupRealmConfig(realm, true);
        }
        catch ( KerberosException e ) {
            log.error("Failed to configure realm", e); //$NON-NLS-1$
        }
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        // update should trigger credential reload by users
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        // ignored, no state
    }


    /**
     * @param realm
     * @param check
     * @throws KerberosException
     */
    private void setupRealmConfig ( String realm, boolean check ) throws KerberosException {
        configure(this.realmManager.getRealmConfig(realm), check);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealm#getKeytab(java.lang.String,
     *      javax.security.auth.kerberos.KerberosPrincipal)
     */
    @Override
    public KeyTab getKeytab ( String keyTabId, KerberosPrincipal servicePrincipal ) throws KerberosException {
        return this.realmManager.getKeytab(this.getKrbRealm(), keyTabId, servicePrincipal);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealm#getUnboundKeytab(java.lang.String)
     */
    @Override
    public KeyTab getUnboundKeytab ( String keyTabId ) throws KerberosException {
        return this.realmManager.getUnboundKeytab(this.getKrbRealm(), keyTabId);
    }

}
