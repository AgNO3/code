/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.internal;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Dictionary;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.ldap.client.LDAPClient;
import eu.agno3.runtime.net.ad.ADConnector;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.ad.ADRealm;
import eu.agno3.runtime.net.ad.ADRealmManager;
import eu.agno3.runtime.net.krb5.GSSAPISubjectFactory;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.RealmType;
import eu.agno3.runtime.net.krb5.internal.KerberosConfig;
import eu.agno3.runtime.net.krb5.internal.KrbRealmManagerImpl;
import eu.agno3.runtime.security.credentials.CredentialUnwrapper;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    ADRealmManagerImpl.class, ADRealmManager.class
}, configurationPid = "adrealm", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class ADRealmManagerImpl extends KrbRealmManagerImpl implements ADRealmManager {

    private static final Logger log = Logger.getLogger(ADRealmManagerImpl.class);

    private ADOSInfo operatingSystem;
    private ADConnector connector;
    private CredentialUnwrapper credentialUnwrapper;


    @Reference
    protected synchronized void setCIFSSetup ( CIFSSetup cs ) {
        // dependency only
    }


    protected synchronized void unsetCIFSSetup ( CIFSSetup cs ) {
        // dependency only
    }


    @Reference
    protected synchronized void setADConnector ( ADConnector adc ) {
        this.connector = adc;
    }


    protected synchronized void unsetADConnector ( ADConnector adc ) {
        if ( this.connector == adc ) {
            this.connector = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void setCredentialUnwrapper ( CredentialUnwrapper cu ) {
        this.credentialUnwrapper = cu;
    }


    protected synchronized void unsetCredentialUnwrapper ( CredentialUnwrapper cu ) {
        if ( this.credentialUnwrapper == cu ) {
            this.credentialUnwrapper = null;
        }
    }


    @Override
    @Reference
    protected void setKerberosConfig ( KerberosConfig kc ) {
        super.setKerberosConfig(kc);
    }


    @Override
    protected void unsetKerberosConfig ( KerberosConfig kc ) {
        super.unsetKerberosConfig(kc);
    }


    @Override
    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        Dictionary<String, Object> props = ctx.getProperties();
        try {
            parseConfig(props, "/etc/adrealms"); //$NON-NLS-1$
        }
        catch ( KerberosException e ) {
            log.error("Failed to setup AD realm manager", e); //$NON-NLS-1$
            return;
        }

        this.operatingSystem = ADOSInfo.fromProperties(props);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.internal.KrbRealmManagerImpl#getRealmType(java.lang.String)
     */
    @Override
    public RealmType getRealmType ( String name ) throws KerberosException {
        return RealmType.AD;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.internal.KrbRealmManagerImpl#getRealmInstance(java.lang.String)
     */
    @Override
    public ADRealm getRealmInstance ( String realm ) throws KerberosException {
        return new ADRealmImpl(realm, this);
    }


    /**
     * @param domainName
     * @return the system wide realm configuration
     * @throws KerberosException
     */
    @Override
    public RealmManagerADRealmConfig getRealmConfig ( String domainName ) throws KerberosException {
        Path file = getRealmDir(domainName);

        if ( !Files.isDirectory(file) || !Files.isReadable(file) ) {
            throw new KerberosException("Domain not found: " + domainName); //$NON-NLS-1$
        }
        ADRealmConfigImpl realmConfig = new ADRealmConfigImpl(domainName, file);
        this.getKerberosConfig().ensureConfigured(realmConfig);
        return realmConfig;
    }


    /**
     * @return os information
     */
    public ADOSInfo getOSInfo () {
        return this.operatingSystem;
    }


    /**
     * @param realm
     * @param subj
     * @return a single ldap connection
     * @throws ADException
     */
    protected LDAPClient getLDAPConnection ( ADRealm realm, GSSAPISubjectFactory subj ) throws ADException {
        return ( (AbstractADRealmImpl) realm ).createLDAPConnection(subj);
    }

}
