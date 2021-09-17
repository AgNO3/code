/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.internal;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.system.account.util.UnixAccountException;
import eu.agno3.orchestrator.system.account.util.UnixAccountUtil;
import eu.agno3.orchestrator.system.acl.util.ACLGroupSyncUtil;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.ad.ADRealm;
import eu.agno3.runtime.net.ad.ADRealmManager;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.KerberosRealm;
import eu.agno3.runtime.net.krb5.RealmType;
import eu.agno3.runtime.security.credentials.WrappedCredentials;


/**
 * @author mbechler
 *
 */
public class ADRealmManagerImpl extends RealmManagerImpl implements eu.agno3.orchestrator.agent.realms.ADRealmManager {

    private static final Logger log = Logger.getLogger(ADRealmManagerImpl.class);

    private static final String HOST_INSTANCE = "host"; //$NON-NLS-1$
    private ADRealmManager adrm;
    private ServiceManager serviceManager;


    /**
     * @param realm
     * @param type
     * @param realmPath
     * @param rlmManager
     * @param adrm
     * @param serviceManager
     */
    public ADRealmManagerImpl ( String realm, RealmType type, Path realmPath, RealmsManagerImpl rlmManager, ADRealmManager adrm,
            ServiceManager serviceManager ) {
        super(realm, type, realmPath, rlmManager, serviceManager);
        this.adrm = adrm;
        this.serviceManager = serviceManager;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.internal.RealmManagerImpl#create(java.util.Properties, java.util.Set)
     */
    @Override
    public void create ( Properties properties, Set<UserPrincipal> defaultAllowedUsers ) throws KerberosException {
        super.create(properties, defaultAllowedUsers);
        GroupPrincipal gp = createRealmInstanceGroup(HOST_INSTANCE);

        for ( UserPrincipal allow : defaultAllowedUsers ) {
            try {
                UnixAccountUtil.addToGroup(gp, allow);
            }
            catch ( UnixAccountException e ) {
                log.error("Failed to add to allowed group " + allow, e); //$NON-NLS-1$
            }
        }
        try {
            ACLGroupSyncUtil.syncACLRecursive(this.getRealmPath());
        }
        catch ( IOException e ) {
            log.warn("Failed to sync ACL permissions", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.internal.RealmManagerImpl#syncAllowedUsers(java.util.Set)
     */
    @Override
    protected void syncAllowedUsers ( Set<UserPrincipal> defaultAllowUsers ) {
        super.syncAllowedUsers(defaultAllowUsers);
        try {
            GroupPrincipal hostGroup = getRealmInstanceGroup(HOST_INSTANCE);
            if ( hostGroup != null ) {
                Set<UserPrincipal> curAllowedUsers = UnixAccountUtil.getMembers(hostGroup);
                for ( UserPrincipal up : defaultAllowUsers ) {
                    if ( !curAllowedUsers.contains(up) ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug("Adding principal to host key allowed users " + up); //$NON-NLS-1$
                        }
                        UnixAccountUtil.addToGroup(hostGroup, up);
                    }
                }
            }
        }
        catch (
            UnixAccountException |
            KerberosException e ) {
            log.warn("Failed to synchronize host key allowed users", e); //$NON-NLS-1$
        }
    }


    /**
     * Grant realm and host credential access
     * 
     * @param user
     * @throws UnixAccountException
     * @throws KerberosException
     */
    @Override
    public void allowHostUser ( UserPrincipal user ) throws UnixAccountException, KerberosException {
        // also allow access to the host group
        GroupPrincipal hostGroup = getRealmInstanceGroup(HOST_INSTANCE);
        if ( hostGroup != null ) {
            UnixAccountUtil.addToGroup(hostGroup, user);
        }
        super.allowUser(user);
    }


    /**
     * Revoke realm and host credential access
     * 
     * @param user
     * @throws UnixAccountException
     * @throws KerberosException
     */
    @Override
    public void revokeHostUser ( UserPrincipal user ) throws UnixAccountException, KerberosException {
        // remove from host group
        GroupPrincipal hostGroup = getRealmInstanceGroup(HOST_INSTANCE);
        if ( hostGroup != null ) {
            UnixAccountUtil.removeFromGroup(hostGroup, user);
        }
        super.revokeUser(user);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.ADRealmManager#isJoined()
     */
    @Override
    public boolean isJoined () throws ADException {
        return getADRealm().isJoined();
    }


    /**
     * {@inheritDoc}
     * 
     *
     * @see eu.agno3.orchestrator.agent.realms.ADRealmManager#getHostKeytab()
     */
    @Override
    public Path getHostKeytab () throws ADException {
        return getADRealm().getHostKeytab();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.ADRealmManager#joinDomain(javax.security.auth.Subject)
     */
    @Override
    public void joinDomain ( Subject s ) throws ADException {
        try {
            getADRealm().join(s, getRealmGroup(), getRealmInstanceGroup(HOST_INSTANCE));
            syncACL();
            updateDNS();
            notifyServices();
        }
        catch ( KerberosException e ) {
            throw new ADException("Failed to join realm", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.ADRealmManager#joinDomain(java.lang.String, java.lang.String)
     */
    @Override
    public void joinDomain ( String user, String password ) throws ADException {
        try {
            getADRealm().join(user, password, getRealmGroup(), getRealmInstanceGroup(HOST_INSTANCE));
            syncACL();
            updateDNS();
            notifyServices();
        }
        catch ( KerberosException e ) {
            throw new ADException("Failed to join realm", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.ADRealmManager#joinDomain(eu.agno3.runtime.security.credentials.WrappedCredentials)
     */
    @Override
    public void joinDomain ( WrappedCredentials creds ) throws ADException {
        try {
            getADRealm().join(creds, getRealmGroup(), getRealmInstanceGroup(HOST_INSTANCE));
            syncACL();
            updateDNS();
            notifyServices();
        }
        catch ( KerberosException e ) {
            throw new ADException("Failed to join realm", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.ADRealmManager#joinDomainWithMachinePassword(java.lang.String)
     */
    @Override
    public void joinDomainWithMachinePassword ( String machinePassword ) throws ADException {
        try {
            getADRealm().joinWithMachinePassword(machinePassword, getRealmGroup(), getRealmInstanceGroup(HOST_INSTANCE));
            syncACL();
            updateDNS();
            notifyServices();
        }
        catch ( KerberosException e ) {
            throw new ADException("Failed to join realm", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     */
    private void updateDNS () {
        try {
            Map<String, String> props = this.adrm.getProperties(getRealm());
            String updateProp = props.get("updateDNS"); //$NON-NLS-1$
            if ( StringUtils.isBlank(updateProp) || !Boolean.parseBoolean(updateProp) ) {
                return;
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Updating DNS records in " + getRealm()); //$NON-NLS-1$
            }

            if ( !executeScript("/usr/share/agno3-base/scripts/ad-dns-update.sh", getRealm()) ) { //$NON-NLS-1$
                log.warn("Failure updating DNS records for " + getRealm()); //$NON-NLS-1$
            }
        }
        catch ( Exception e ) {
            log.warn("Failed to update DNS records in " + getRealm(), e); //$NON-NLS-1$
        }
    }


    /**
     * 
     */
    private void removeDNS () {
        try {
            Map<String, String> props = this.adrm.getProperties(getRealm());
            String updateProp = props.get("updateDNS"); //$NON-NLS-1$
            if ( StringUtils.isBlank(updateProp) || !Boolean.parseBoolean(updateProp) ) {
                return;
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Removing DNS records in " + getRealm()); //$NON-NLS-1$
            }

            if ( !executeScript("/usr/share/agno3-base/scripts/ad-dns-remove.sh", getRealm()) ) { //$NON-NLS-1$
                log.warn("Failure removing DNS records for " + getRealm()); //$NON-NLS-1$
            }
        }
        catch ( Exception e ) {
            log.warn("Failed to remove DNS records for " + getRealm(), e); //$NON-NLS-1$
        }
    }


    /**
     * @param string
     * @param realm
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private static boolean executeScript ( String... cmd ) throws IOException, InterruptedException {
        File devnull = new File("/dev/null"); //$NON-NLS-1$
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.environment().clear();
        pb.redirectError(devnull);
        pb.redirectOutput(devnull);

        Process p = pb.start();
        p.getInputStream().close();
        try {
            if ( !p.waitFor(10, TimeUnit.SECONDS) ) {
                p.destroyForcibly();
                p.waitFor();
                return false;
            }
            return p.exitValue() == 0;
        }
        catch ( InterruptedException e ) {
            log.debug("Interrupted waiting for script", e); //$NON-NLS-1$
            p.destroyForcibly();
            p.waitFor();
            return false;
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.ADRealmManager#leave(java.lang.String, java.lang.String)
     */
    @Override
    public void leave ( String user, String password ) throws ADException {
        try {
            removeDNS();
            getADRealm().leave(user, password, getRealmGroup(), getRealmInstanceGroup(HOST_INSTANCE));
            syncACL();
            notifyServices();
        }
        catch ( KerberosException e ) {
            throw new ADException("Failed to get leave realm", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.ADRealmManager#leave(eu.agno3.runtime.security.credentials.WrappedCredentials)
     */
    @Override
    public void leave ( WrappedCredentials creds ) throws ADException {
        try {
            removeDNS();
            getADRealm().leave(creds, getRealmGroup(), getRealmInstanceGroup(HOST_INSTANCE));
            syncACL();
            notifyServices();
        }
        catch ( KerberosException e ) {
            throw new ADException("Failed to get leave realm", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.ADRealmManager#leave(javax.security.auth.Subject)
     */
    @Override
    public void leave ( Subject s ) throws ADException {
        try {
            removeDNS();
            getADRealm().leave(s, getRealmGroup(), getRealmInstanceGroup(HOST_INSTANCE));
            syncACL();
            notifyServices();
        }
        catch ( KerberosException e ) {
            throw new ADException("Failed to leave realm", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.ADRealmManager#rekey()
     */
    @Override
    public void rekey () throws ADException {
        try {
            getADRealm().rekey(getRealmGroup(), getRealmInstanceGroup(HOST_INSTANCE));
            syncACL();
            notifyServices();
        }
        catch ( KerberosException e ) {
            throw new ADException("Failed to rekey machine account", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.ADRealmManager#runMaintenance()
     */
    @Override
    public void runMaintenance () throws KerberosException {
        try {
            updateDNS();
            if ( getADRealm().runMaintenance(getRealmGroup(), getRealmInstanceGroup(HOST_INSTANCE)) ) {
                syncACL();
                notifyServices();
            }
        }
        catch ( ADException e ) {
            throw new KerberosException("Failed to get realm groups", e); //$NON-NLS-1$
        }
    }


    /**
     * @throws KerberosException
     * 
     */
    @Override
    public void notifyServices () throws KerberosException {
        try {
            this.serviceManager.forceReloadAll("ad@" + this.getADRealm().getDomainName()); //$NON-NLS-1$
        }
        catch ( ADException e ) {
            throw new KerberosException("Failed to get ad realm", e); //$NON-NLS-1$
        }
    }


    /**
     * @return
     * @throws KerberosException
     */
    private ADRealm getADRealm () throws ADException {
        KerberosRealm realmInstance;
        try {
            realmInstance = this.adrm.getRealmInstance(this.getRealm());
        }
        catch ( KerberosException e ) {
            throw new ADException("Failed to get AD realm", e); //$NON-NLS-1$
        }

        if ( ! ( realmInstance instanceof ADRealm ) ) {
            throw new ADException("Not an AD realm " + this.getRealm()); //$NON-NLS-1$
        }

        return (ADRealm) realmInstance;
    }


    /**
     * Use different namespaces for ad and krb
     * 
     * @return
     */
    @Override
    protected String getRealmGroupPrefix () {
        return "ad-rlm"; //$NON-NLS-1$
    }
}
