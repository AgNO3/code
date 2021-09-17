/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.internal;


import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.unboundid.ldap.sdk.LDAPException;

import eu.agno3.orchestrator.agent.realms.ADRealmManager;
import eu.agno3.orchestrator.agent.realms.KeyTabManager;
import eu.agno3.orchestrator.agent.realms.RealmManager;
import eu.agno3.orchestrator.agent.realms.RealmsManager;
import eu.agno3.orchestrator.realms.KeyData;
import eu.agno3.orchestrator.realms.KeyInfo;
import eu.agno3.orchestrator.realms.KeytabInfo;
import eu.agno3.orchestrator.realms.RealmInfo;
import eu.agno3.orchestrator.realms.RealmManagementException;
import eu.agno3.orchestrator.realms.RealmManagementMXBean;
import eu.agno3.orchestrator.realms.RealmType;
import eu.agno3.runtime.jmx.MBean;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.krb5.ETypesUtil;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.security.credentials.WrappedCredentials;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    MBean.class, RealmManagementBean.class
}, property = {
    "objectName=eu.agno3.agent.realms:type=RealmManagementBean"
} )
public class RealmManagementBean implements RealmManagementMXBean, MBean {

    private static final Logger log = Logger.getLogger(RealmManagementBean.class);

    private RealmsManager realmsManager;


    @Reference
    protected synchronized void setRealmsManager ( RealmsManager rm ) {
        this.realmsManager = rm;
    }


    protected synchronized void unsetRealmsManager ( RealmsManager rm ) {
        if ( this.realmsManager == rm ) {
            this.realmsManager = null;
        }
    }


    @Override
    public List<RealmInfo> getRealms () throws RealmManagementException {
        try {
            List<RealmInfo> info = new LinkedList<>();
            for ( Entry<String, eu.agno3.runtime.net.krb5.RealmType> e : this.realmsManager.listRealms().entrySet() ) {
                try {
                    info.add(makeRealmInfo(e.getKey(), e.getValue()));
                }
                catch ( KerberosException ex ) {
                    log.warn("Failed to load realm data for " + e.getKey(), ex); //$NON-NLS-1$
                }
            }
            return info;
        }
        catch ( KerberosException e ) {
            log.warn("Failed to enumerate realms", e); //$NON-NLS-1$
            throw new RealmManagementException("Failed to enumerate realms"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.realms.RealmManagementMXBean#getRealm(java.lang.String)
     */
    @Override
    public RealmInfo getRealm ( String realm ) throws RealmManagementException {
        try {
            RealmManager realmManager = this.realmsManager.getRealmManager(realm);
            return makeRealmInfo(realm, realmManager.getType());
        }
        catch ( KerberosException e ) {
            log.debug("Realm does not exist or is invalid " + realm, e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param e
     * @return
     * @throws KerberosException
     */
    private RealmInfo makeRealmInfo ( String name, eu.agno3.runtime.net.krb5.RealmType realmType ) throws KerberosException {
        RealmInfo ri = new RealmInfo();
        ri.setRealmName(name);
        ri.setType(RealmType.valueOf(realmType.name()));
        RealmManager realmManager = this.realmsManager.getRealmManager(name, realmType);

        if ( realmManager instanceof ADRealmManager ) {
            try {
                ri.setJoined( ( (ADRealmManager) realmManager ).isJoined());
            }
            catch ( ADException e1 ) {
                throw new KerberosException("Failed to get AD join status", e1); //$NON-NLS-1$
            }
        }

        List<KeytabInfo> ktis = new LinkedList<>();
        for ( String ktName : realmManager.listKeytabs() ) {
            ktis.add(makeKeytabInfo(realmManager, ktName));
        }
        ri.setKeytabs(ktis);
        return ri;
    }


    /**
     * @param realmManager
     * @param ktName
     * @return
     * @throws KerberosException
     */
    private static KeytabInfo makeKeytabInfo ( RealmManager realmManager, String ktName ) throws KerberosException {
        KeytabInfo kti = new KeytabInfo();
        KeyTabManager ktm = realmManager.getKeytabManager(ktName);
        kti.setId(ktName);
        List<KeyInfo> kis = new LinkedList<>();
        for ( KerberosKey k : ktm.listKeys() ) {
            kis.add(makeKeyInfo(k));
        }

        kti.setKeys(kis);
        return kti;
    }


    /**
     * @param k
     * @return
     * @throws KerberosException
     */
    private static KeyInfo makeKeyInfo ( KerberosKey k ) throws KerberosException {
        KeyInfo ki = new KeyInfo();
        ki.setKvno(k.getVersionNumber());
        ki.setAlgorithm(ETypesUtil.mapEType(k.getKeyType()));
        ki.setPrincipal(k.getPrincipal().toString());
        return ki;
    }


    /**
     * @param type
     * @return
     */
    private static eu.agno3.runtime.net.krb5.RealmType mapRealmType ( RealmType type ) {
        return eu.agno3.runtime.net.krb5.RealmType.valueOf(type.name());
    }


    @Override
    public void addKeys ( String realm, RealmType type, String keytab, List<KeyData> keys ) throws RealmManagementException {
        try {

            List<KerberosKey> kks = wrapKeys(keys);
            KeyTabManager keytabManager = this.realmsManager.getRealmManager(realm, mapRealmType(type)).getKeytabManager(keytab);
            keytabManager.addKeys(kks);
            keytabManager.save();
        }
        catch (
            KerberosException |
            IOException e ) {
            log.warn("Failed to add keys", e); //$NON-NLS-1$
            throw new RealmManagementException("Failed to add keys"); //$NON-NLS-1$
        }
    }


    /**
     * @param keys
     * @return
     */
    private static List<KerberosKey> wrapKeys ( List<KeyData> keys ) {
        List<KerberosKey> kks = new LinkedList<>();
        for ( KeyData k : keys ) {
            kks.add(
                new KerberosKey(
                    new KerberosPrincipal(k.getPrincipal()),
                    Base64.decodeBase64(k.getData()),
                    ETypesUtil.eTypeFromMITString(k.getAlgorithm()),
                    (int) k.getKvno()));
        }
        return kks;
    }


    @Override
    public void createKeytab ( String realm, RealmType type, String keytab, List<KeyData> initialKeys ) throws RealmManagementException {
        try {
            List<KerberosKey> kks = wrapKeys(initialKeys);
            KeyTabManager keytabManager = this.realmsManager.getRealmManager(realm, mapRealmType(type)).getKeytabManager(keytab);
            keytabManager.addKeys(kks);
            keytabManager.save();
        }
        catch (
            KerberosException |
            IOException e ) {
            log.warn("Failed to create keytab", e); //$NON-NLS-1$
            throw new RealmManagementException("Failed to create keytab"); //$NON-NLS-1$
        }
    }


    @Override
    public void deleteKeytab ( String realm, RealmType type, String keytab ) throws RealmManagementException {
        try {
            KeyTabManager keytabManager = this.realmsManager.getRealmManager(realm, mapRealmType(type)).getKeytabManager(keytab);
            keytabManager.delete();
        }
        catch (
            KerberosException |
            IOException e ) {
            log.warn("Failed to remove keytab", e); //$NON-NLS-1$
            throw new RealmManagementException("Failed to remove keytab"); //$NON-NLS-1$
        }
    }


    @Override
    public void removeKeys ( String realm, RealmType type, String keytab, List<KeyInfo> keys ) throws RealmManagementException {
        try {
            KeyTabManager keytabManager = this.realmsManager.getRealmManager(realm, mapRealmType(type)).getKeytabManager(keytab);
            for ( KeyInfo ki : keys ) {
                KerberosPrincipal princ = new KerberosPrincipal(ki.getPrincipal());
                if ( ki.getAlgorithm() != null && ki.getKvno() >= 0 && ki.getPrincipal() != null ) {
                    keytabManager.removeKey(princ, ki.getKvno(), ETypesUtil.eTypeFromMITString(ki.getAlgorithm()));
                }
                else if ( ki.getAlgorithm() == null && ki.getKvno() < 0 ) {
                    keytabManager.removeKeys(princ);
                }
                else if ( ki.getAlgorithm() == null ) {
                    keytabManager.removeKeys(princ, ki.getKvno());
                }
            }

            keytabManager.save();
        }
        catch (
            KerberosException |
            IOException e ) {
            log.warn("Failed to remove keys", e); //$NON-NLS-1$
            throw new RealmManagementException("Failed to remove keys"); //$NON-NLS-1$
        }
    }


    @Override
    public void joinAD ( String realm, WrappedCredentials creds ) throws RealmManagementException {
        try {
            ADRealmManager realmManager = (ADRealmManager) this.realmsManager.getRealmManager(realm, mapRealmType(RealmType.AD));
            realmManager.joinDomain(creds);
        }
        catch (
            KerberosException |
            ADException e ) {
            throw wrapException("Failed to join AD domain: ", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.realms.RealmManagementMXBean#joinADWithMachinePassword(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void joinADWithMachinePassword ( String realm, String machinePassword ) throws RealmManagementException {
        try {
            ADRealmManager realmManager = (ADRealmManager) this.realmsManager.getRealmManager(realm, mapRealmType(RealmType.AD));
            realmManager.joinDomainWithMachinePassword(machinePassword);
        }
        catch (
            KerberosException |
            ADException e ) {
            throw wrapException("Failed to join AD domain: ", e); //$NON-NLS-1$
        }
    }


    @Override
    public void rekeyAD ( String realm ) throws RealmManagementException {
        try {
            ADRealmManager realmManager = (ADRealmManager) this.realmsManager.getRealmManager(realm, mapRealmType(RealmType.AD));
            realmManager.rekey();
        }
        catch (
            KerberosException |
            ADException e ) {
            throw wrapException("Failed to rekey AD domain: ", e); //$NON-NLS-1$
        }
    }


    @Override
    public void leaveAD ( String realm, WrappedCredentials creds ) throws RealmManagementException {
        try {
            ADRealmManager realmManager = (ADRealmManager) this.realmsManager.getRealmManager(realm, mapRealmType(RealmType.AD));
            realmManager.leave(creds);
        }
        catch (
            KerberosException |
            ADException e ) {
            throw wrapException("Failed to leave AD domain: ", e); //$NON-NLS-1$
        }
    }


    /**
     * @param string
     * @param e
     * @return
     */
    private static RealmManagementException wrapException ( String string, Exception e ) {
        Throwable t = e;
        if ( e instanceof ADException && ( e.getCause() instanceof LDAPException || e.getCause() instanceof KerberosException ) ) {
            t = e.getCause();
        }
        log.debug("Origina1l exception", e); //$NON-NLS-1$
        log.warn(string, t);
        return new RealmManagementException(string + t.getMessage());
    }
}
