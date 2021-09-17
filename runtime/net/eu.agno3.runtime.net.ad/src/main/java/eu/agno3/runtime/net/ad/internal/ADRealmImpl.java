/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.internal;


import java.nio.file.Path;
import java.util.Collection;
import java.util.Dictionary;

import javax.security.auth.kerberos.KerberosPrincipal;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.crypto.random.SecureRandomProvider;
import eu.agno3.runtime.ldap.client.LDAPClientBuilder;
import eu.agno3.runtime.ldap.client.LDAPClientFactory;
import eu.agno3.runtime.net.ad.ADConnector;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.ad.ADRealm;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.KerberosRealm;
import eu.agno3.runtime.security.credentials.CredentialUnwrapper;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    ADRealm.class, KerberosRealm.class, LDAPClientFactory.class
}, configurationPid = "ad", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class ADRealmImpl extends AbstractADRealmImpl {

    private static final Logger log = Logger.getLogger(ADRealmImpl.class);

    private ADRealmManagerImpl adRealmManager;


    /**
     * 
     */
    public ADRealmImpl () {}


    /**
     * @param realm
     * @param adrlm
     * @throws KerberosException
     */
    public ADRealmImpl ( String realm, ADRealmManagerImpl adrlm ) throws KerberosException {
        this.adRealmManager = adrlm;
        configureFromDomain(realm, false);
    }


    @Reference
    protected synchronized void setRealmManager ( ADRealmManagerImpl adrlm ) {
        this.adRealmManager = adrlm;
    }


    protected synchronized void unsetRealmManager ( ADRealmManagerImpl adrlm ) {
        if ( this.adRealmManager == adrlm ) {
            this.adRealmManager = null;
        }
    }


    @Override
    @Reference
    protected synchronized void setLDAPClientBuilder ( LDAPClientBuilder lcb ) {
        super.setLDAPClientBuilder(lcb);
    }


    @Override
    protected synchronized void unsetLDAPClientBuilder ( LDAPClientBuilder lcb ) {
        super.unsetLDAPClientBuilder(lcb);
    }


    @Reference
    @Override
    protected synchronized void setADConnector ( ADConnector adc ) {
        super.setADConnector(adc);
    }


    @Override
    protected synchronized void unsetADConnector ( ADConnector adc ) {
        super.unsetADConnector(adc);
    }


    @Reference
    @Override
    protected synchronized void setCIFSSetup ( CIFSSetup cs ) {
        super.setCIFSSetup(cs);
    }


    @Override
    protected synchronized void unsetCIFSSetup ( CIFSSetup cs ) {
        super.unsetCIFSSetup(cs);
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC )
    @Override
    protected synchronized void setCredentialUnwrapper ( CredentialUnwrapper cu ) {
        super.setCredentialUnwrapper(cu);
    }


    @Override
    protected synchronized void unsetCredentialUnwrapper ( CredentialUnwrapper cu ) {
        super.unsetCredentialUnwrapper(cu);
    }


    @Reference
    @Override
    protected synchronized void setSecureRandomProvider ( SecureRandomProvider srp ) {
        super.setSecureRandomProvider(srp);
    }


    @Override
    protected synchronized void unsetSecureRandomProvider ( SecureRandomProvider srp ) {
        super.unsetSecureRandomProvider(srp);
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        Dictionary<String, Object> cfg = ctx.getProperties();
        String domain = ConfigUtil.parseString(cfg, "instanceId", null); //$NON-NLS-1$
        if ( StringUtils.isBlank(domain) ) {
            log.error("No domain configured"); //$NON-NLS-1$
            return;
        }
        configureFromDomain(domain, true);
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        reloadMachinePassword();
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        close();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealm#getKeytab(java.lang.String,
     *      javax.security.auth.kerberos.KerberosPrincipal)
     */
    @Override
    public javax.security.auth.kerberos.KeyTab getKeytab ( String keyTabId, KerberosPrincipal servicePrincipal ) throws KerberosException {
        return this.adRealmManager.getKeytab(this.getKrbRealm(), keyTabId, servicePrincipal);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealm#getUnboundKeytab(java.lang.String)
     */
    @Override
    public javax.security.auth.kerberos.KeyTab getUnboundKeytab ( String keyTabId ) throws KerberosException {
        return this.adRealmManager.getUnboundKeytab(this.getKrbRealm(), keyTabId);
    }


    @Override
    protected ADRealmConfigImpl getConfig () throws ADException {
        try {
            return (ADRealmConfigImpl) this.adRealmManager.getRealmConfig(getDomainName());
        }
        catch ( KerberosException e ) {
            throw new ADException("Realm config unavailable", e); //$NON-NLS-1$
        }
    }


    /**
     * @return
     * @throws ADException
     */
    @Override
    protected AbstractADRealmConfigImpl checkJoin () throws ADException {
        AbstractADRealmConfigImpl realmConfig = getConfig();
        Path machineKeytabFile = realmConfig.getHostKeytabFile();
        Path machineSidFile = realmConfig.getMachineSIDFile();
        Path domainSidFile = realmConfig.getDomainSIDFile();
        Path machineKVNOFile = realmConfig.getMachineKVNOFile();

        if ( !canWriteFile(machineSidFile) || !canWriteFile(domainSidFile) || !canWriteFile(machineKVNOFile) || !canWriteFile(machineKeytabFile) ) {
            throw new ADException("No permission to write machine password"); //$NON-NLS-1$
        }
        return realmConfig;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.internal.AbstractADRealmImpl#getPermittedETypes()
     */
    @Override
    protected Collection<Integer> getPermittedETypes () {
        return this.adRealmManager.getPermittedETypeAlgos();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.internal.AbstractADRealmImpl#getOSInfo()
     */
    @Override
    protected ADOSInfo getOSInfo () {
        return this.adRealmManager.getOSInfo();
    }

}
