/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 30, 2017 by mbechler
 */
package eu.agno3.runtime.net.krb5.internal;


import java.io.File;

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
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = KerberosRealm.class, configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = "krb.standalone" )
public class StandaloneKerberosRealmImpl extends AbstractKerberosRealmImpl {

    private static final Logger log = Logger.getLogger(StandaloneKerberosRealmImpl.class);
    private File keytab;
    private KerberosConfig kerberosConfig;


    @Reference
    protected void setKerberosConfig ( KerberosConfig kc ) {
        this.kerberosConfig = kc;
    }


    protected void unsetKerberosConfig ( KerberosConfig kc ) {
        if ( this.kerberosConfig == kc ) {
            this.kerberosConfig = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        String realm = setupRealm(ctx);
        if ( realm == null ) {
            return;
        }

        StandaloneKerberosRealmConfigImpl kc = new StandaloneKerberosRealmConfigImpl(realm, ctx.getProperties());
        this.kerberosConfig.ensureConfigured(kc);
        configure(kc, true);
        String keytabPath = ConfigUtil.parseString(
            ctx.getProperties(),
            "keytabPath", //$NON-NLS-1$
            "/etc/krb5.keytab"); //$NON-NLS-1$

        File f = new File(keytabPath);
        if ( !f.exists() || !f.canRead() ) {
            log.error("Cannot read keytab " + keytabPath); //$NON-NLS-1$
        }
        else {
            this.keytab = f;
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


    private File getKeytabFile ( String keyTabId ) throws KerberosException {
        File f = this.keytab;
        if ( f == null ) {
            throw new KerberosException("Keytab not initialized"); //$NON-NLS-1$
        }
        return f;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealm#getKeytab(java.lang.String,
     *      javax.security.auth.kerberos.KerberosPrincipal)
     */
    @Override
    public KeyTab getKeytab ( String keyTabId, KerberosPrincipal servicePrincipal ) throws KerberosException {
        return KeyTab.getInstance(servicePrincipal, getKeytabFile(keyTabId));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealm#getUnboundKeytab(java.lang.String)
     */
    @Override
    public KeyTab getUnboundKeytab ( String keyTabId ) throws KerberosException {
        return KeyTab.getUnboundInstance(getKeytabFile(keyTabId));
    }

}
