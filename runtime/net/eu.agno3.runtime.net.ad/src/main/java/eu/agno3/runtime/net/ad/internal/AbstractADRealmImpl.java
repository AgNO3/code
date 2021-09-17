/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 30, 2017 by mbechler
 */
package eu.agno3.runtime.net.ad.internal;


import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.crypto.ShortBufferException;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.unboundid.ldap.sdk.BindRequest;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.random.SecureRandomProvider;
import eu.agno3.runtime.ldap.client.LDAPClient;
import eu.agno3.runtime.ldap.client.LDAPClientBuilder;
import eu.agno3.runtime.ldap.client.LDAPClientFactory;
import eu.agno3.runtime.ldap.client.LDAPConfiguration;
import eu.agno3.runtime.net.ad.ADConnector;
import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.ad.ADRealm;
import eu.agno3.runtime.net.ad.CIFSConfiguration;
import eu.agno3.runtime.net.ad.JCIFSSID;
import eu.agno3.runtime.net.ad.NetlogonConnection;
import eu.agno3.runtime.net.ad.SubjectKerb5Authenticator;
import eu.agno3.runtime.net.ad.msgs.DomainPasswordInformation;
import eu.agno3.runtime.net.ad.msgs.SamrCloseHandle;
import eu.agno3.runtime.net.ad.msgs.SamrConnect2;
import eu.agno3.runtime.net.ad.msgs.SamrCreateUser2InDomain;
import eu.agno3.runtime.net.ad.msgs.SamrLookupDomainInSamServer;
import eu.agno3.runtime.net.ad.msgs.SamrLookupNamesInDomain;
import eu.agno3.runtime.net.ad.msgs.SamrOpenDomain;
import eu.agno3.runtime.net.ad.msgs.SamrOpenUser;
import eu.agno3.runtime.net.ad.msgs.SamrQueryDomainInfo;
import eu.agno3.runtime.net.ad.msgs.SamrQueryInformationUser2;
import eu.agno3.runtime.net.ad.msgs.SamrSetInformationUser2;
import eu.agno3.runtime.net.ad.msgs.SamrUserControlInformation;
import eu.agno3.runtime.net.ad.msgs.SamrUserInternal5InformationNew;
import eu.agno3.runtime.net.ad.msgs.SamrUserLogonInformation;
import eu.agno3.runtime.net.dns.SRVEntries;
import eu.agno3.runtime.net.dns.SRVUtil;
import eu.agno3.runtime.net.krb5.ETypesUtil;
import eu.agno3.runtime.net.krb5.GSSAPISubjectFactory;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.KeyTab;
import eu.agno3.runtime.net.krb5.KeyTabEntry;
import eu.agno3.runtime.net.krb5.RealmType;
import eu.agno3.runtime.net.krb5.StaticSubjectGSSAPIFactory;
import eu.agno3.runtime.net.krb5.UserPasswordGSSAPIFactory;
import eu.agno3.runtime.net.krb5.internal.AbstractKerberosRealmImpl;
import eu.agno3.runtime.security.credentials.CredentialType;
import eu.agno3.runtime.security.credentials.CredentialUnwrapper;
import eu.agno3.runtime.security.credentials.UnwrappedCredentials;
import eu.agno3.runtime.security.credentials.UsernamePasswordCredential;
import eu.agno3.runtime.security.credentials.WrappedCredentials;
import eu.agno3.runtime.util.sid.SID;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.context.BaseContext;
import jcifs.dcerpc.DcerpcException;
import jcifs.dcerpc.DcerpcHandle;
import jcifs.dcerpc.rpc.policy_handle;
import jcifs.smb.Kerb5Authenticator;


/**
 * @author mbechler
 *
 */
public abstract class AbstractADRealmImpl extends AbstractKerberosRealmImpl implements ADRealm, LDAPClientFactory {

    private static final Logger log = Logger.getLogger(AbstractADRealmImpl.class);

    private static final String GROUP_FILE_READ_PERMS_STR = "rw-r-----"; //$NON-NLS-1$
    private static final Set<PosixFilePermission> GROUP_FILE_READ_PERMS = PosixFilePermissions.fromString(GROUP_FILE_READ_PERMS_STR);

    private LDAPClientBuilder ldapClientBuilder;
    private ADConnector connector;

    private String domainName;
    private String netbiosDomainName;
    private String localNetbiosHostname;
    private SRVEntries domainControllers;
    private String machineAccount;

    private DateTime dcLifetime;
    private SortedMap<Integer, String> machinePasswords = new TreeMap<>();
    private KerberosPrincipal hostPrincipal;

    private LDAPConnectionPool ldapPool;
    private LDAPConfiguration ldapCfg;

    private SID machineSID;
    private GenericObjectPool<NetlogonConnection> netlogonPool;
    private boolean joinChecked;
    private String accountDN;
    private Integer machineKVNO;

    private boolean allowLegacyCrypto;

    private CIFSContext cifsContext;

    private Collection<String> allowedETypes = Collections.EMPTY_LIST;

    private DomainPasswordInformation domainPasswordInformation;

    private SecureRandom secureRandomX;

    private SecureRandomProvider secureRandomProvider;

    private CredentialUnwrapper credentialUnwrapper;


    @Reference
    protected synchronized void setLDAPClientBuilder ( LDAPClientBuilder lcb ) {
        this.ldapClientBuilder = lcb;
    }


    protected synchronized void unsetLDAPClientBuilder ( LDAPClientBuilder lcb ) {
        if ( this.ldapClientBuilder == lcb ) {
            this.ldapClientBuilder = null;
        }
    }


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


    @Reference
    protected synchronized void setSecureRandomProvider ( SecureRandomProvider srp ) {
        this.secureRandomProvider = srp;
    }


    protected synchronized void unsetSecureRandomProvider ( SecureRandomProvider srp ) {
        if ( this.secureRandomProvider == srp ) {
            this.secureRandomProvider = null;
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


    /**
     * @return the secureRandomX
     */
    public SecureRandom getSecureRandom () {
        if ( this.secureRandomX == null ) {
            this.secureRandomX = this.secureRandomProvider.getSecureRandom();
        }
        return this.secureRandomX;
    }


    /**
     * Create a new netlogon connection
     * 
     * @return new netlogon connection
     * @throws KerberosException
     * @throws ADException
     */
    public NetlogonConnection createNetlogonConnection () throws ADException, KerberosException {
        try {
            log.debug("Creating new netlogon connection"); //$NON-NLS-1$
            return this.connector.getNetlogonConnection(this, this.getMachineCIFSContext());
        }
        catch ( IOException e ) {
            throw new ADException("Failed to get netlogon connection", e); //$NON-NLS-1$
        }
    }


    /**
     * @return
     */
    protected abstract ADOSInfo getOSInfo ();


    /**
     * 
     */
    protected abstract Collection<Integer> getPermittedETypes ();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.internal.AbstractKerberosRealmImpl#getKrbRealm()
     */
    @Override
    public String getKrbRealm () {
        return this.domainName.toUpperCase(Locale.ROOT);
    }


    /**
     * @param creds
     * @return unwrapped credentials
     * @throws ADException
     */
    public UsernamePasswordCredential unwrapCreds ( WrappedCredentials creds ) throws ADException {
        try {
            CredentialUnwrapper cu = this.credentialUnwrapper;
            if ( cu == null ) {
                throw new ADException("Credential unwrapper unavailable"); //$NON-NLS-1$
            }
            UnwrappedCredentials unwrapped = cu.unwrap(creds);
            if ( unwrapped.getType() != CredentialType.USERNAME_PASSWORD ) {
                throw new ADException("Need username/password credentials, incompatible type " + unwrapped.getType()); //$NON-NLS-1$
            }
            return (UsernamePasswordCredential) unwrapped;
        }
        catch (
            IOException |
            CryptoException e ) {
            throw new ADException("Failed to unwrap credentials", e); //$NON-NLS-1$
        }
    }


    /**
     * @param realm
     * @return an LDAP pool for the realms AD DS
     * @throws ADException
     */
    protected LDAPConnectionPool createLDAPPool () throws ADException {
        try {
            return this.ldapClientBuilder.createConnectionPool(new ActiveDirectoryLDAPConfiguration(this), null);
        }
        catch (
            LDAPException |
            CryptoException |
            KerberosException |
            ADException e ) {
            throw new ADException("Failed to create LDAP pool", e); //$NON-NLS-1$
        }
    }


    /**
     * @param realm
     * @param subj
     * @return a single ldap connection
     * @throws ADException
     */
    protected LDAPClient createLDAPConnection ( GSSAPISubjectFactory subj ) throws ADException {
        try {
            return this.ldapClientBuilder.createSingleConnection(new ActiveDirectoryLDAPConfiguration(this, subj), null);
        }
        catch ( LDAPException e ) {
            if ( e.getCause() instanceof KerberosException ) {
                throw new ADException("Authentication failed", e.getCause()); //$NON-NLS-1$
            }
            throw new ADException("Failed to open LDAP connection", e); //$NON-NLS-1$
        }
        catch (
            CryptoException |
            KerberosException e ) {
            throw new ADException("Failed to open LDAP connection", e); //$NON-NLS-1$
        }
    }


    /**
     * @param pool
     * @param connection
     * @param cfg
     * @return a wrapped pool connection
     */
    protected LDAPClient wrapPoolConnection ( LDAPConnectionPool pool, LDAPConnection connection, LDAPConfiguration cfg ) {
        return this.ldapClientBuilder.wrapPoolConnection(pool, connection, cfg);
    }


    /**
     * 
     */
    public AbstractADRealmImpl () {}


    /**
     * @param domain
     */
    protected void configureFromDomain ( String domain, boolean check ) {
        this.domainName = domain.toLowerCase(Locale.ROOT);

        AbstractADRealmConfigImpl realmConfig;
        try {
            realmConfig = getConfig();
        }
        catch ( ADException e ) {
            log.error("Failed to get realm config " + getDomainName(), e); //$NON-NLS-1$
            return;
        }

        configure(realmConfig, check);
    }


    /**
     * @param domain
     * @return realm config
     */
    protected abstract AbstractADRealmConfigImpl getConfig () throws ADException;


    /**
     * @param realmConfig
     * @param check
     */
    protected void configure ( AbstractADRealmConfigImpl realmConfig, boolean check ) {
        try {
            configureBase(realmConfig, check);
        }
        catch ( IOException e ) {
            log.error("Failed to configure realm", e); //$NON-NLS-1$
        }

        try {
            this.cifsContext = new BaseContext(
                new CIFSConfiguration(this.getLocalNetbiosHostname(), this.isAllowLegacyCrypto(), Collections.EMPTY_MAP));
        }
        catch ( CIFSException e ) {
            log.error("Failed to create CIFS context", e); //$NON-NLS-1$
        }
    }


    /**
     * @param domain
     * @param realmConfig
     * @param check
     * @throws IOException
     */
    protected void configureBase ( AbstractADRealmConfigImpl realmConfig, boolean check ) throws IOException {

        setKrbRealm(this.domainName.toUpperCase(Locale.ROOT));
        setupHostname(realmConfig.getOverrideLocalHostname(), this.domainName, check);

        if ( realmConfig.getOverrideNetbiosDomainName() != null ) {
            this.netbiosDomainName = realmConfig.getOverrideNetbiosDomainName();
        }
        else {
            this.netbiosDomainName = this.domainName.substring(0, this.domainName.indexOf('.')).toUpperCase(Locale.ROOT);
        }

        if ( realmConfig.getOverrideNetbiosHostname() != null ) {
            this.localNetbiosHostname = realmConfig.getOverrideNetbiosHostname();
        }
        else {
            this.localNetbiosHostname = this.getLocalHostname().substring(0, this.getLocalHostname().indexOf('.'));
        }

        if ( realmConfig.getOverrideMachineAccount() != null ) {
            this.machineAccount = realmConfig.getOverrideMachineAccount();
        }
        else {
            this.machineAccount = this.localNetbiosHostname.toLowerCase(Locale.ROOT) + "$"; //$NON-NLS-1$
        }

        if ( realmConfig.getMachineBaseDN() != null ) {
            this.accountDN = realmConfig.getMachineBaseDN();
        }
        else {
            this.accountDN = "cn=Computers"; //$NON-NLS-1$
        }

        this.hostPrincipal = makeServicePrincipal("host"); //$NON-NLS-1$

        Path machineKVNOFile = realmConfig.getMachineKVNOFile();
        if ( Files.exists(machineKVNOFile) ) {
            byte[] data = Files.readAllBytes(machineKVNOFile);
            this.machineKVNO = Integer.valueOf(new String(data, StandardCharsets.UTF_8));
        }

        Integer kvno = this.machineKVNO;
        if ( kvno != null ) {
            Path machinePasswordFile = realmConfig.getSecretFile(kvno);
            if ( Files.exists(machinePasswordFile) ) {
                this.machinePasswords = loadMachinePasswords(realmConfig, kvno);
                log.debug("Machine is already joined"); //$NON-NLS-1$
            }
        }

        this.machineSID = getSID(realmConfig);

        this.allowLegacyCrypto = realmConfig.isAllowLegacyCrypto();
        try {
            this.allowedETypes = ETypesUtil.mapETypes(realmConfig.getDefaultTicketEnctypes());
        }
        catch ( KerberosException e ) {
            log.error("Failed to configure allowed ETypes", e); //$NON-NLS-1$
        }
    }


    /**
     * @param realmConfig
     * @return
     * @throws IOException
     */
    protected SID getSID ( AbstractADRealmConfigImpl realmConfig ) throws IOException {
        Path machineSIDFile = realmConfig.getMachineSIDFile();
        if ( Files.exists(machineSIDFile) ) {
            return SID.fromBinary(Files.readAllBytes(machineSIDFile));
        }
        return null;
    }


    /**
     * 
     */
    protected void reloadMachinePassword () {
        try {
            AbstractADRealmConfigImpl realmConfig = getConfig();
            Path machineKVNOFile = realmConfig.getMachineKVNOFile();
            if ( Files.exists(machineKVNOFile) ) {
                byte[] data = Files.readAllBytes(machineKVNOFile);
                int newKVNO = Integer.parseInt(new String(data, StandardCharsets.UTF_8));
                log.info("Reloading machine passwords"); //$NON-NLS-1$
                this.machinePasswords = loadMachinePasswords(realmConfig, newKVNO);
                this.machineKVNO = newKVNO;
                checkJoin();
            }
        }
        catch (
            IOException |
            ADException e ) {
            log.error("Failed to reload machine passwords", e); //$NON-NLS-1$
        }
    }


    protected DateTime getMachinePasswordLastChange () throws ADException {
        Integer kvno = this.machineKVNO;
        if ( kvno != null ) {
            Path machinePasswordFile = getConfig().getSecretFile(kvno);
            if ( Files.exists(machinePasswordFile) ) {
                try {
                    return new DateTime(Files.getLastModifiedTime(machinePasswordFile).toMillis());
                }
                catch ( IOException e ) {
                    log.error("Failed to get machine password last modification", e); //$NON-NLS-1$
                }
            }
        }
        return null;
    }


    /**
     * 
     */
    @Override
    public void close () {
        if ( this.ldapPool != null ) {
            log.debug("Closing ldap pool"); //$NON-NLS-1$
            this.ldapPool.close();
            this.ldapPool = null;
        }

        if ( this.netlogonPool != null ) {
            log.debug("Closing netlogon pool"); //$NON-NLS-1$
            this.netlogonPool.close();
            this.netlogonPool = null;
        }

        if ( this.cifsContext != null ) {
            try {
                log.debug("Closing CIFS context"); //$NON-NLS-1$
                this.cifsContext.close();
            }
            catch ( CIFSException e ) {
                log.warn("Failed to close CIFS context", e); //$NON-NLS-1$
            }
            this.cifsContext = null;
        }
        log.debug("Finished"); //$NON-NLS-1$
    }


    /**
     * @return the allowLegacyCrypto
     */
    @Override
    public boolean isAllowLegacyCrypto () {
        return this.allowLegacyCrypto;
    }


    /**
     * @return the allowedETypes
     */
    @Override
    public Collection<String> getAllowedETypes () {
        return this.allowedETypes;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.internal.KerberosRealmImpl#getAdminServer()
     */
    @Override
    public String getAdminServer () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.internal.KerberosRealmImpl#getKpasswdServer()
     */
    @Override
    public String getKpasswdServer () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.internal.KerberosRealmImpl#getRealmType()
     */
    @Override
    public RealmType getRealmType () {
        return RealmType.AD;
    }


    /**
     * @param realmConfig
     * @param latestKVNO
     * @return
     * @throws ADException
     */
    private static SortedMap<Integer, String> loadMachinePasswords ( AbstractADRealmConfigImpl realmConfig, int latestKVNO ) throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Latest KVNO is %d for %s", latestKVNO, realmConfig.getRealm())); //$NON-NLS-1$
        }
        SortedMap<Integer, String> mps = new TreeMap<>();
        for ( int i = latestKVNO; i >= 0; i-- ) {
            try {
                mps.put(i, new String(Files.readAllBytes(realmConfig.getSecretFile(i)), StandardCharsets.UTF_8));
            }
            catch ( IOException e ) {
                if ( i == latestKVNO ) {
                    throw new IOException("Failed to read latest machine password " + realmConfig.getSecretFile(i)); //$NON-NLS-1$
                }
                if ( log.isTraceEnabled() ) {
                    log.trace("Cannot read machine password file for KVNO " + i, e); //$NON-NLS-1$
                }
                break;
            }
        }
        return mps;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#getMachinePasswords()
     */
    @Override
    public SortedMap<Integer, String> getMachinePasswords () throws ADException {
        return this.machinePasswords;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#getMachinePassword()
     */
    @Override
    public String getMachinePassword () throws ADException {
        Integer lastKey = this.machinePasswords.lastKey();
        if ( lastKey == null ) {
            throw new ADException("No key known"); //$NON-NLS-1$
        }
        return this.machinePasswords.get(lastKey);
    }


    /**
     * @return the host principal
     */
    @Override
    public KerberosPrincipal getHostPrincipal () {
        return this.hostPrincipal;
    }


    /**
     * 
     * @param service
     * @return the service principal name
     */
    @Override
    public KerberosPrincipal makeServicePrincipal ( String service ) {
        return new KerberosPrincipal(String.format(
            "%s/%s@%s", //$NON-NLS-1$
            service,
            this.getLocalHostname(),
            this.getKrbRealm()), KerberosPrincipal.KRB_NT_PRINCIPAL);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#getSalts(javax.security.auth.kerberos.KerberosPrincipal)
     */
    @Override
    public Map<Integer, String> getSalts ( KerberosPrincipal servicePrincipal ) {
        // this is only true for principals that are bound to the computer account
        // Others should be either client use or provided via a keytab (already salted)
        String macct = this.getMachineAccount();
        if ( macct.charAt(macct.length() - 1) == '$' ) {
            macct = macct.substring(0, macct.length() - 1);
        }
        String salt = String.format(
            "%shost%s.%s", //$NON-NLS-1$
            getKrbRealm().toUpperCase(Locale.ROOT),
            macct.toLowerCase(Locale.ROOT),
            getDomainName().toLowerCase(Locale.ROOT));

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Using salt %s for %s", salt, servicePrincipal)); //$NON-NLS-1$
        }

        Map<Integer, String> salts = new HashMap<>();
        for ( int etype : ETypesUtil.ALL_ETYPE_CODES ) {
            salts.put(etype, salt);
        }
        return salts;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPClientFactory#getConnection()
     */
    @Override
    public LDAPClient getConnection () throws LDAPException {
        try {
            ensurePoolsInit();
        }
        catch ( ADException e ) {
            throw new LDAPException(ResultCode.CONNECT_ERROR, e);
        }

        return wrapPoolConnection(this.ldapPool, this.ldapPool.getConnection(), this.ldapCfg);
    }


    @Override
    public LDAPClient getIndependedConnection ( BindRequest bindReq ) throws LDAPException {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ldap.client.LDAPClientFactory#tryBind(com.unboundid.ldap.sdk.BindRequest)
     */
    @Override
    public BindResult tryBind ( BindRequest req ) throws LDAPException {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#getCIFSContext()
     */
    @Override
    public CIFSContext getCIFSContext () {
        return this.cifsContext;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#getMachineCIFSContext()
     */
    @Override
    public CIFSContext getMachineCIFSContext () {
        CIFSContext tc = this.getCIFSContext();
        return tc.withCredentials(new SubjectKerb5Authenticator(new ADGSSAPISubjectFactory(this)));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.internal.KerberosRealmImpl#getInitiatorSubjectFactory(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public GSSAPISubjectFactory getInitiatorSubjectFactory ( String keytab, String realm, String user, String password ) throws KerberosException {
        if ( StringUtils.isBlank(keytab) && StringUtils.isBlank(realm) && StringUtils.isBlank(user) && StringUtils.isBlank(password) ) {
            return new ADGSSAPISubjectFactory(this);
        }
        return super.getInitiatorSubjectFactory(keytab, realm, user, password);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#getDomainName()
     */
    @Override
    public String getDomainName () {
        return this.domainName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#getDomainSid()
     */
    @Override
    public SID getDomainSid () {
        if ( this.machineSID == null ) {
            return null;
        }
        return this.machineSID.getParent();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#getNetbiosDomainName()
     */
    @Override
    public String getNetbiosDomainName () {
        return this.netbiosDomainName;
    }


    /**
     * @param netbiosDomainName
     *            the netbiosDomainName to set
     */
    public void setNetbiosDomainName ( String netbiosDomainName ) {
        this.netbiosDomainName = netbiosDomainName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#getLocalNetbiosHostname()
     */
    @Override
    public String getLocalNetbiosHostname () {
        return this.localNetbiosHostname;
    }


    /**
     * @param localNetbiosHostname
     *            the localNetbiosHostname to set
     */
    public void setLocalNetbiosHostname ( String localNetbiosHostname ) {
        this.localNetbiosHostname = localNetbiosHostname;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#getMachineSid()
     */
    @Override
    public SID getMachineSid () {
        return this.machineSID;
    }


    /**
     * @return the machineAccount
     */
    @Override
    public String getMachineAccount () {
        return this.machineAccount;
    }


    /**
     * @param machineAccount
     *            the machineAccount to set
     */
    public void setMachineAccount ( String machineAccount ) {
        this.machineAccount = machineAccount;
    }


    /**
     * @return the machineSID
     */
    public SID getMachineSID () {
        return this.machineSID;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#getAccountDN()
     */
    @Override
    public String getAccountDN () {
        return this.accountDN;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#getDomainControllers()
     */
    @Override
    public SRVEntries getDomainControllers () throws ADException {
        if ( this.domainControllers == null || ( this.dcLifetime.isBeforeNow() ) ) {

            try {
                this.domainControllers = SRVUtil.lookup(this.domainName, "_ldap._tcp.dc._msdcs"); //$NON-NLS-1$
                this.dcLifetime = DateTime.now().plusSeconds(this.domainControllers.getMinTTL());
            }
            catch ( NamingException e ) {
                log.debug("Failed to lookup domain controllers", e); //$NON-NLS-1$
                // add 60 second delay
                this.dcLifetime = ( this.dcLifetime != null ? this.dcLifetime : DateTime.now() ).plusSeconds(60);
            }
        }

        if ( this.domainControllers == null ) {
            throw new ADException("Failed to lookup domain controllers"); //$NON-NLS-1$
        }

        return this.domainControllers;

    }


    /**
     * @throws ADException
     * 
     */
    private void ensurePoolsInit () throws ADException {
        try {
            if ( this.ldapCfg == null ) {
                this.ldapCfg = new ActiveDirectoryLDAPConfiguration(this);
            }
            if ( this.ldapPool == null ) {
                this.ldapPool = createLDAPPool();
            }
        }
        catch ( KerberosException e ) {
            throw new ADException("Failed to connect to AD LDAP", e); //$NON-NLS-1$
        }

        if ( this.netlogonPool == null ) {
            ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                GenericObjectPool<NetlogonConnection> netlogonConnPool = new GenericObjectPool<>(new NetlogonConnectionFactory(this));
                netlogonConnPool.setTestOnBorrow(true);
                netlogonConnPool.setTestOnReturn(true);
                netlogonConnPool.setMaxIdle(1);

                this.netlogonPool = netlogonConnPool;
            }
            finally {
                Thread.currentThread().setContextClassLoader(oldTCCL);
            }
        }
    }


    /**
     * @return whether the domain has been joined (not checking whether the credentials are still valid)
     * 
     */
    @Override
    public boolean isJoined () {
        if ( this.machinePasswords == null || this.machinePasswords.isEmpty() ) {
            return false;
        }

        if ( this.machineSID == null ) {
            return false;
        }

        return this.machineKVNO != null && this.machinePasswords.get(this.machineKVNO) != null;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#ensureJoined()
     */
    @Override
    public void ensureJoined () throws ADException {
        if ( this.joinChecked ) {
            return;
        }
        if ( !this.isJoined() ) {
            throw new ADException("Not joined to domain " + this.domainName); //$NON-NLS-1$
        }

        checkKVNO();

        try {
            try ( NetlogonConnection netlogonConnection = createNetlogonConnection() ) {
                // do nothing
                netlogonConnection.check();
            }
        }
        catch ( Exception e ) {
            throw new ADException("Could not obtain netlogon connection", e); //$NON-NLS-1$
        }

        this.ensurePoolsInit();

        this.joinChecked = true;
    }


    /**
     * @throws ADException
     */
    protected void checkKVNO () throws ADException {
        try ( LDAPClient connection = this.getConnection() ) {
            long kvno = ADLDAPOperations.getKVNO(connection, this.machineSID);
            if ( log.isDebugEnabled() ) {
                log.debug("KVNO is " + kvno); //$NON-NLS-1$
            }
            if ( kvno != this.machineKVNO ) {
                throw new ADException("Machine KNVO mismatch, password was externally changed"); //$NON-NLS-1$
            }
        }
        catch ( LDAPException e ) {
            throw new ADException("Failed to check KVNO", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ADException
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#getNetlogonConnection()
     */
    @Override
    public NetlogonConnection getNetlogonConnection () throws ADException {
        this.ensurePoolsInit();
        try {
            return new NetlogonConnectionPoolWrapper(this.netlogonPool, this.netlogonPool.borrowObject());
        }
        catch ( Exception e ) {
            throw new ADException("Failed to get netlogon connection", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#getHostKeytab()
     */
    @Override
    public Path getHostKeytab () throws ADException {
        if ( !this.isJoined() ) {
            throw new ADException("Not joined to domain " + getDomainName()); //$NON-NLS-1$
        }
        return getConfig().getHostKeytabFile();
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ADException
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#ensureHostSPN(javax.security.auth.kerberos.KerberosPrincipal)
     */
    @Override
    public void ensureHostSPN ( KerberosPrincipal servicePrincipal ) throws ADException {
        try ( LDAPClient connection = this.getConnection() ) {
            ADLDAPOperations.ensureHostSPN(connection, servicePrincipal, this.getMachineSID(), this.getMachineAccount());
        }
        catch ( LDAPException e ) {
            throw new ADException("Failed to ensure SPN registration", e); //$NON-NLS-1$
        }

    }


    protected AbstractADRealmConfigImpl checkJoin () throws ADException {
        return getConfig();
    }


    /**
     * @param machinePasswordFile
     * @param machineSidFile
     * @param machineKVNOFile
     * @param joinResult
     * @param hostGroup
     * @param realmGroup
     * @throws ADException
     */
    protected synchronized void handleJoinResult ( AbstractADRealmConfigImpl realmConfig, JoinResult joinResult, GroupPrincipal realmGroup,
            GroupPrincipal hostGroup ) throws ADException {

        this.machineSID = joinResult.getMachineSID();
        int newKVNO = joinResult.getKvno();
        String newPassword = joinResult.getNewMachinePassword();
        this.machinePasswords.put(newKVNO, newPassword);
        this.machineKVNO = newKVNO;
        try {
            Path machinePasswordFile = realmConfig.getSecretFile(newKVNO);
            writeData(machinePasswordFile, newPassword.getBytes(StandardCharsets.UTF_8), hostGroup);
        }
        catch ( IOException e ) {
            throw new ADException("Password changed, but write failed.", e); //$NON-NLS-1$
        }

        writeMachineKeytab(this.machinePasswords, hostGroup, realmConfig.getHostKeytabFile());

        try {
            writeData(realmConfig.getMachineSIDFile(), joinResult.getMachineSID().toBinary(), hostGroup);
        }
        catch ( IOException e ) {
            throw new ADException("Failed to write machine SID.", e); //$NON-NLS-1$
        }

        try {
            writeData(realmConfig.getDomainSIDFile(), joinResult.getDomainSID().toBinary(), realmGroup);
        }
        catch ( IOException e ) {
            throw new ADException("Failed to write domain SID.", e); //$NON-NLS-1$
        }

        try {
            writeData(realmConfig.getMachineKVNOFile(), String.valueOf(newKVNO).getBytes(StandardCharsets.UTF_8), hostGroup);
        }
        catch ( IOException e ) {
            throw new ADException("Failed to write kvno.", e); //$NON-NLS-1$
        }
    }


    /**
     * @param joinResult
     * @param machinePasswords2
     * @param hostGroup
     * @param machineKeytabFile
     * @throws ADException
     */
    private void writeMachineKeytab ( SortedMap<Integer, String> mps, GroupPrincipal hostGroup, Path ktPath ) throws ADException {
        KeyTab kt;
        try {
            kt = makeMachineKeytab(getHostPrincipal(), mps);
        }
        catch ( KerberosException e ) {
            throw new ADException("Failed to build principal keytab", e); //$NON-NLS-1$
        }
        try ( FileChannel ch = FileChannel.open(
            ktPath,
            EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
            PosixFilePermissions.asFileAttribute(GROUP_FILE_READ_PERMS)); // $NON-NLS-1$
              OutputStream os = Channels.newOutputStream(ch) ) {

            kt.write(os);
            PosixFileAttributeView attrs = Files.getFileAttributeView(ktPath, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
            if ( hostGroup != null ) {
                attrs.setGroup(hostGroup);
                attrs.setPermissions(GROUP_FILE_READ_PERMS); // $NON-NLS-1$
            }
        }
        catch ( IOException e ) {
            throw new ADException("Failed to write machine keytab", e); //$NON-NLS-1$
        }
    }


    /**
     * @param princ
     * @param mps
     * @return the generated keytab
     * @throws KerberosException
     */
    public KeyTab makeMachineKeytab ( KerberosPrincipal princ, SortedMap<Integer, String> mps ) throws KerberosException {
        KeyTab kt = new KeyTab();
        Collection<Integer> etypes = getPermittedETypes();
        Set<String> algos = new HashSet<>();
        for ( int etype : etypes ) {
            algos.add(ETypesUtil.getAlgoFromEtype(etype));
        }
        for ( Entry<Integer, String> password : mps.entrySet() ) {
            for ( String algo : algos ) {
                KeyTabEntry e = new KeyTabEntry(princ, algo, password.getValue(), password.getKey());
                if ( !kt.getEntries().contains(e) ) {
                    kt.getEntries().add(e);
                }
            }
        }
        return kt;
    }


    @Override
    public synchronized boolean runMaintenance ( GroupPrincipal realmGroup, GroupPrincipal hostGroup ) throws ADException {
        SortedMap<Integer, String> mps = this.getMachinePasswords();
        if ( mps == null || mps.size() <= 1 || !isJoined() ) {
            return false;
        }

        int latestKVNO = mps.lastKey();

        AbstractADRealmConfigImpl realmConfig = getConfig();
        if ( log.isDebugEnabled() ) {
            log.debug("Running maintenance for " + realmConfig.getRealm()); //$NON-NLS-1$
        }

        return doRunMaintenance(realmGroup, hostGroup, latestKVNO, realmConfig);

    }


    /**
     * @param realmGroup
     * @param hostGroup
     * @param latestKVNO
     * @param realmConfig
     * @return
     * @throws ADException
     */
    protected boolean doRunMaintenance ( GroupPrincipal realmGroup, GroupPrincipal hostGroup, int latestKVNO, AbstractADRealmConfigImpl realmConfig )
            throws ADException {
        boolean modified = false;
        if ( realmConfig.isRekeyMachineAccount() ) {
            DateTime kvnoTimestamp = getKVNOTimestamp(realmConfig, latestKVNO);
            if ( kvnoTimestamp != null && kvnoTimestamp.plus(realmConfig.getMachineRekeyInterval()).isBeforeNow() ) {
                log.info("Rekeying machine account for " + realmConfig.getRealm()); //$NON-NLS-1$
                JoinResult result = rekey();
                handleJoinResult(realmConfig, result, realmGroup, hostGroup);
                modified = true;
            }
        }

        modified |= cleanKeys(realmConfig);
        return modified;
    }


    private synchronized boolean cleanKeys ( AbstractADRealmConfigImpl config ) throws ADException {
        SortedMap<Integer, String> mps = this.getMachinePasswords();
        if ( mps.size() <= 1 ) {
            return false;
        }

        boolean modified = false;
        for ( int i = mps.lastKey() - 1; i >= 0; i-- ) {
            Path secretFile = config.getSecretFile(i);
            if ( !Files.exists(secretFile) ) {
                continue;
            }
            DateTime kvnoTimestamp = getKVNOTimestamp(config, i);
            if ( kvnoTimestamp == null || kvnoTimestamp.plus(config.getMaxiumumTicketLifetime()).isBeforeNow() ) {
                try {
                    Files.delete(secretFile);
                }
                catch ( IOException e ) {
                    log.warn("Failed to remove key file " + secretFile, e); //$NON-NLS-1$
                }
                if ( mps.remove(i) != null ) {
                    log.info("Removed KVNO " + i); //$NON-NLS-1$
                }
                modified = true;
            }
        }
        return modified;
    }


    private static DateTime getKVNOTimestamp ( AbstractADRealmConfigImpl config, int kvno ) {
        Path secretFile = config.getSecretFile(kvno);
        if ( !Files.exists(secretFile) ) {
            return null;
        }

        try {
            FileTime lastModifiedTime = Files.getLastModifiedTime(secretFile);
            return new DateTime(lastModifiedTime.toMillis());
        }
        catch ( IOException e ) {
            log.warn("Failed to get key last modified", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param realmConfig
     * @throws ADException
     */
    protected void handleLeaveResult ( AbstractADRealmConfigImpl realmConfig ) throws ADException {
        Integer kvno = this.machineKVNO;
        Path machinePasswordFile = realmConfig.getSecretFile(kvno);

        this.machineKVNO = null;
        this.machineSID = null;
        if ( this.machinePasswords != null ) {
            this.machinePasswords.clear();
        }

        boolean success = true;
        success &= tryDelete(machinePasswordFile);
        success &= tryDelete(realmConfig.getHostKeytabFile());
        success &= tryDelete(realmConfig.getMachineSIDFile());
        success &= tryDelete(realmConfig.getDomainSIDFile());
        success &= tryDelete(realmConfig.getMachineKVNOFile());

        for ( int i = kvno - 1; i >= 0; i-- ) {
            Path secretFile = realmConfig.getSecretFile(i);
            if ( !Files.exists(secretFile) ) {
                break;
            }
            success &= tryDelete(secretFile);
        }

        if ( !success ) {
            throw new ADException("Failed to remove some domain files"); //$NON-NLS-1$
        }
    }


    /**
     * @param file
     * @return
     */
    private static boolean tryDelete ( Path file ) {
        try {
            return Files.deleteIfExists(file);
        }
        catch ( IOException e ) {
            log.debug("Failed to remove file " + file, e); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * @param file
     * @param data
     * @throws IOException
     */
    private static void writeData ( Path path, byte[] data, GroupPrincipal group ) throws IOException {

        try ( FileChannel fc = FileChannel.open(
            path,
            EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING),
            PosixFilePermissions.asFileAttribute(GROUP_FILE_READ_PERMS));
              OutputStream os = Channels.newOutputStream(fc) ) {
            os.write(data);
        }

        Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        PosixFileAttributeView attrs = Files.getFileAttributeView(path, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        if ( group != null ) {
            attrs.setGroup(group);
            attrs.setPermissions(GROUP_FILE_READ_PERMS); // $NON-NLS-1$
        }
    }


    /**
     * @param adminUser
     * @param adminPassword
     * @throws ADException
     */
    @Override
    public void updateDNSHostname ( String adminUser, String adminPassword ) throws ADException {
        updateDNSHostname(new UserPasswordGSSAPIFactory(adminUser, adminPassword, this.getKrbRealm()));
    }


    @Override
    public void updateDNSHostname ( Subject subj ) throws ADException {
        updateDNSHostname(new StaticSubjectGSSAPIFactory(subj));
    }


    /**
     * @param subj
     * @throws ADException
     */
    private void updateDNSHostname ( GSSAPISubjectFactory subj ) throws ADException {
        try ( LDAPClient cl = createLDAPConnection(subj) ) {
            ADLDAPOperations.updateDNSHostname(cl, getMachineSID(), getLocalHostname());
        }
        catch ( LDAPException e ) {
            throw new ADException("Failed to update dns hostname", e); //$NON-NLS-1$
        }
    }


    @Override
    public void updateOS ( String adminUser, String adminPassword ) throws ADException {
        updateOS(new UserPasswordGSSAPIFactory(adminUser, adminPassword, getKrbRealm()));
    }


    @Override
    public void updateOS ( Subject subj ) throws ADException {
        updateOS(new StaticSubjectGSSAPIFactory(subj));
    }


    /**
     * @param subj
     * @throws ADException
     */
    private void updateOS ( GSSAPISubjectFactory subj ) throws ADException {
        try ( LDAPClient cl = createLDAPConnection(subj) ) {
            ADLDAPOperations.setOperatingSystem(cl, getMachineSID(), getOSInfo());
        }
        catch ( LDAPException e ) {
            throw new ADException("Failed to update dns hostname", e); //$NON-NLS-1$
        }
    }


    /**
     * @param file
     * @return
     */
    protected static boolean canWriteFile ( Path file ) {
        boolean exists = Files.exists(file);
        return ( !exists && Files.isWritable(file.getParent()) ) || ( exists && Files.isWritable(file) );
    }


    /**
     * 
     * @param subject
     * @return the join information
     * @throws ADException
     */
    public JoinResult joinDomain ( Subject subject ) throws ADException {
        CIFSContext tc = getCIFSContext();
        DcerpcHandle handle = this.connector.getSAMREndpoint(this, tc.withCredentials(new Kerb5Authenticator(subject)));
        try ( LDAPClient cl = createLDAPConnection(new StaticSubjectGSSAPIFactory(subject)) ) {
            return joinDomain(handle, cl);
        }
    }


    /**
     * 
     * Joins or rejoins the domain
     * 
     * If the machine account already exists it's password is changed to a new random value.
     * 
     * @param adminUsername
     * @param adminPassword
     * @return the join information
     * @throws ADException
     */
    public JoinResult joinDomain ( String adminUsername, String adminPassword ) throws ADException {
        DcerpcHandle handle = this.connector.getSAMREndpointWithPassword(this, adminUsername, adminPassword);
        try ( LDAPClient cl = createLDAPConnection(new UserPasswordGSSAPIFactory(adminUsername, adminPassword, getKrbRealm())) ) {
            return joinDomain(handle, cl);
        }
    }


    /**
     * @param realm
     * @param rpcHandle
     * @return
     * @throws ADException
     */
    private JoinResult joinDomain ( DcerpcHandle rpcHandle, LDAPClient cl ) throws ADException {
        if ( log.isInfoEnabled() ) {
            log.info("Joining domain " + getDomainName()); //$NON-NLS-1$
        }
        try {
            ADLDAPOperations.updateOrCreateMachineAccount(
                cl,
                getMachineAccount(),
                getLocalHostname(),
                getLocalNetbiosHostname(),
                getAccountDN(),
                getAllowedETypes());
        }
        catch ( LDAPException e ) {
            throw new ADException("Failed to create machine object", e); //$NON-NLS-1$
        }

        String passwordToSet = makeRandomPassword(getSecureRandom());
        try {
            SamrConnect2 connect = new SamrConnect2(rpcHandle.getServer(), 0x30);
            rpcHandle.sendrecv(connect);

            SamrLookupDomainInSamServer lookupDom = new SamrLookupDomainInSamServer(connect.getServerHandle(), getNetbiosDomainName());
            rpcHandle.sendrecv(lookupDom);

            SID domainSid = JCIFSSID.fromJCIFS(lookupDom.getSid());

            SamrOpenDomain openDom = new SamrOpenDomain(connect.getServerHandle(), 0x211, lookupDom.getSid());
            rpcHandle.sendrecv(openDom);

            UserResult userRes = getUser(getMachineAccount(), rpcHandle, openDom);

            setPassword(passwordToSet, rpcHandle, userRes);

            // set machine account, no password expiry
            try {
                SamrUserControlInformation accountFlagsInfo = new SamrUserControlInformation(0x80);
                SamrSetInformationUser2 setAccountFlags = new SamrSetInformationUser2(userRes.getHandle(), accountFlagsInfo);
                rpcHandle.sendrecv(setAccountFlags);
            }
            catch ( DcerpcException e ) {
                log.warn("Failed to set machine account flags", e); //$NON-NLS-1$
            }

            rpcHandle.sendrecv(new SamrCloseHandle(userRes.getHandle()));
            rpcHandle.sendrecv(new SamrCloseHandle(openDom.getDomainHandle()));
            rpcHandle.sendrecv(new SamrCloseHandle(connect.getServerHandle()));

            SID newMachineSID = new SID(domainSid, userRes.getRid());
            int kvno = ADLDAPOperations.getKVNO(cl, newMachineSID);
            return new JoinResult(passwordToSet, domainSid, newMachineSID, kvno);
        }
        catch (
            IOException |
            NoSuchAlgorithmException |
            LDAPException |
            ShortBufferException e ) {
            throw new ADException("Failed to join domain " + getDomainName(), e); //$NON-NLS-1$
        }
        finally {
            try {
                rpcHandle.close();
            }
            catch ( IOException e ) {
                log.warn("Failed to close SAMR handle", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param subj
     * @throws ADException
     */
    public void leaveDomain ( Subject subj ) throws ADException {
        try ( LDAPClient cl = createLDAPConnection(new StaticSubjectGSSAPIFactory(subj)) ) {
            leaveDomain(cl);
        }
    }


    /**
     * @param adminUsername
     * @param adminPassword
     * @throws ADException
     */
    public void leaveDomain ( String adminUsername, String adminPassword ) throws ADException {
        try ( LDAPClient cl = createLDAPConnection(new UserPasswordGSSAPIFactory(adminUsername, adminPassword, getKrbRealm())) ) {
            leaveDomain(cl);
        }
    }


    /**
     * @param realm
     * @param cl
     * @throws ADException
     */
    private void leaveDomain ( LDAPClient cl ) throws ADException {
        try {
            if ( log.isInfoEnabled() ) {
                log.info("Leaving domain " + getDomainName()); //$NON-NLS-1$
            }
            if ( !ADLDAPOperations.deleteMachineAccount(cl, getMachineSid()) ) {
                ADLDAPOperations.disableMachineAccount(cl, getMachineSid());
            }
        }
        catch (
            LDAPException |
            ADException e ) {
            throw new ADException("Failed to remove machine account", e); //$NON-NLS-1$
        }
    }


    /**
     * @param initialMachinePassword
     * @return join result
     * @throws ADException
     */
    public JoinResult joinWithMachinePassword ( String initialMachinePassword ) throws ADException {
        String machineAcct = getMachineAccount();
        String machinePassword = initialMachinePassword;

        if ( machinePassword == null ) {
            machinePassword = machineAcct;

            if ( machinePassword.charAt(machinePassword.length() - 1) == '$' ) {
                machinePassword = machinePassword.substring(0, machinePassword.length() - 1);
            }
        }

        if ( log.isInfoEnabled() ) {
            log.info(String.format(
                "Joining with reset password, domain %s machine account %s", //$NON-NLS-1$
                getDomainName(),
                machineAcct));
        }

        String passwordToSet = makeRandomPassword(getSecureRandom());
        try ( LDAPClient cl = createLDAPConnection(new UserPasswordGSSAPIFactory(machineAcct, machinePassword, getKrbRealm())) ) {
            return ADLDAPOperations.joinWithResetPassword(cl, this, passwordToSet, machinePassword, getAllowedETypes());
        }
        catch (
            LDAPException |
            IOException e ) {
            throw new ADException("Failed to set password", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * @param realm
     * @return join result
     * @throws ADException
     */
    protected JoinResult rekey () throws ADException {
        if ( log.isInfoEnabled() ) {
            log.info("Rekeying domain " + getDomainName()); //$NON-NLS-1$
        }
        String passwordToSet = makeRandomPassword(getSecureRandom());
        try ( LDAPClient cl = createLDAPConnection(new UserPasswordGSSAPIFactory(getMachineAccount(), getMachinePassword(), getKrbRealm())) ) {
            int newKvno = ADLDAPOperations.changeMachinePassword(cl, getMachineSid(), passwordToSet, getMachinePassword(), getAllowedETypes());
            return new JoinResult(passwordToSet, getDomainSid(), getMachineSid(), newKvno);
        }
        catch (
            LDAPException |
            IOException e ) {
            throw new ADException("Failed to set password", e); //$NON-NLS-1$
        }
    }


    /**
     * @param realm
     * @return the machine account logon information
     * @throws DcerpcException
     * @throws IOException
     * @throws ADException
     */
    public SamrUserLogonInformation getMachineLogonInformation ( ADRealm realm ) throws DcerpcException, IOException, ADException {
        try ( DcerpcHandle dcerpcHandle = this.connector.getSAMREndpointWithPassword(realm, realm.getMachineAccount(), realm.getMachinePassword()) ) {
            SamrConnect2 connect = new SamrConnect2(dcerpcHandle.getServer(), 0x30);
            dcerpcHandle.sendrecv(connect);

            SamrLookupDomainInSamServer lookupDom = new SamrLookupDomainInSamServer(connect.getServerHandle(), realm.getNetbiosDomainName());
            dcerpcHandle.sendrecv(lookupDom);

            SamrOpenDomain openDom = new SamrOpenDomain(connect.getServerHandle(), 0x211, lookupDom.getSid());
            dcerpcHandle.sendrecv(openDom);

            UserResult userRes = getUser(realm.getMachineAccount(), dcerpcHandle, openDom);
            SamrUserLogonInformation logonInfo = new SamrUserLogonInformation();
            SamrQueryInformationUser2 queryUserInfo = new SamrQueryInformationUser2(userRes.getHandle(), logonInfo);

            dcerpcHandle.sendrecv(queryUserInfo);

            dcerpcHandle.sendrecv(new SamrCloseHandle(userRes.getHandle()));
            dcerpcHandle.sendrecv(new SamrCloseHandle(openDom.getDomainHandle()));
            dcerpcHandle.sendrecv(new SamrCloseHandle(connect.getServerHandle()));
            return logonInfo;
        }
        catch ( IOException e ) {
            throw new ADException("Failed to get logon information", e); //$NON-NLS-1$
        }
    }


    private void setPassword ( String passwordToSet, DcerpcHandle dcerpcHandle, UserResult userRes )
            throws UnsupportedEncodingException, NoSuchAlgorithmException, DcerpcException, IOException, ShortBufferException {
        byte[] sessionKey = dcerpcHandle.getSessionKey();
        SamrUserInternal5InformationNew setPasswordInfo = SamrUserInternal5InformationNew
                .makePasswordChangeInfo(getSecureRandom(), passwordToSet, sessionKey);
        SamrSetInformationUser2 setPassword = new SamrSetInformationUser2(userRes.getHandle(), setPasswordInfo);
        dcerpcHandle.sendrecv(setPassword);
    }


    protected DomainPasswordInformation loadDomainPasswordInformation () throws ADException {
        try ( DcerpcHandle dcerpcHandle = this.connector.getSAMREndpointWithPassword(this, getMachineAccount(), getMachinePassword()) ) {
            SamrConnect2 connect = new SamrConnect2(dcerpcHandle.getServer(), 0x30);
            dcerpcHandle.sendrecv(connect);
            SamrLookupDomainInSamServer lookupDom = new SamrLookupDomainInSamServer(connect.getServerHandle(), getNetbiosDomainName());
            dcerpcHandle.sendrecv(lookupDom);
            SamrOpenDomain openDom = new SamrOpenDomain(connect.getServerHandle(), 0x211, lookupDom.getSid());
            dcerpcHandle.sendrecv(openDom);

            DomainPasswordInformation info = new DomainPasswordInformation();
            SamrQueryDomainInfo domInfo = new SamrQueryDomainInfo(openDom.getDomainHandle(), info);
            dcerpcHandle.sendrecv(domInfo);
            return (DomainPasswordInformation) domInfo.getInformation();
        }
        catch ( IOException e ) {
            throw new ADException("Failed to get domain password information", e); //$NON-NLS-1$
        }
    }


    /**
     * @return the domainPasswordInformation
     * @throws ADException
     */
    @Override
    public DomainPasswordInformation getDomainPasswordInformation () throws ADException {
        if ( !isJoined() ) {
            throw new ADException("Not joined to domain"); //$NON-NLS-1$
        }
        if ( this.domainPasswordInformation == null ) {
            this.domainPasswordInformation = loadDomainPasswordInformation();
        }
        return this.domainPasswordInformation;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#join(java.lang.String, java.lang.String,
     *      java.nio.file.attribute.GroupPrincipal, java.nio.file.attribute.GroupPrincipal)
     */
    @Override
    public void join ( String adminUser, String adminPassword, GroupPrincipal realmGroup, GroupPrincipal hostGroup ) throws ADException {
        AbstractADRealmConfigImpl realmConfig = checkJoin();
        JoinResult joinResult = joinDomain(adminUser, adminPassword);
        handleJoinResult(realmConfig, joinResult, realmGroup, hostGroup);
        updateDNSHostname(adminUser, adminPassword);
        updateOS(adminUser, adminPassword);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#join(eu.agno3.runtime.security.credentials.WrappedCredentials,
     *      java.nio.file.attribute.GroupPrincipal, java.nio.file.attribute.GroupPrincipal)
     */
    @Override
    public void join ( WrappedCredentials creds, GroupPrincipal realmGroup, GroupPrincipal hostGroup ) throws ADException {
        AbstractADRealmConfigImpl realmConfig = checkJoin();
        UsernamePasswordCredential cred = unwrapCreds(creds);
        JoinResult joinResult = joinDomain(cred.getUsername(), cred.getPassword());
        handleJoinResult(realmConfig, joinResult, realmGroup, hostGroup);
        updateDNSHostname(cred.getUsername(), cred.getPassword());
        updateOS(cred.getUsername(), cred.getPassword());
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#join(javax.security.auth.Subject, java.nio.file.attribute.GroupPrincipal,
     *      java.nio.file.attribute.GroupPrincipal)
     */
    @Override
    public void join ( Subject subj, GroupPrincipal realmGroup, GroupPrincipal hostGroup ) throws ADException {
        AbstractADRealmConfigImpl realmConfig = checkJoin();
        JoinResult joinResult = joinDomain(subj);
        handleJoinResult(realmConfig, joinResult, realmGroup, hostGroup);
        updateDNSHostname(subj);
        updateOS(subj);
    }


    @Override
    public void joinWithMachinePassword ( String machinePassword, GroupPrincipal realmGroup, GroupPrincipal hostGroup ) throws ADException {
        AbstractADRealmConfigImpl config = checkJoin();
        JoinResult result = joinWithMachinePassword(machinePassword);
        handleJoinResult(config, result, realmGroup, hostGroup);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#leave(java.lang.String, java.lang.String,
     *      java.nio.file.attribute.GroupPrincipal, java.nio.file.attribute.GroupPrincipal)
     */
    @Override
    public void leave ( String adminUser, String adminPassword, GroupPrincipal realmGroup, GroupPrincipal hostGroup ) throws ADException {
        AbstractADRealmConfigImpl realmConfig = checkJoin();
        leaveDomain(adminUser, adminPassword);
        handleLeaveResult(realmConfig);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#leave(eu.agno3.runtime.security.credentials.WrappedCredentials,
     *      java.nio.file.attribute.GroupPrincipal, java.nio.file.attribute.GroupPrincipal)
     */
    @Override
    public void leave ( WrappedCredentials creds, GroupPrincipal realmGroup, GroupPrincipal hostGroup ) throws ADException {
        AbstractADRealmConfigImpl realmConfig = checkJoin();
        UsernamePasswordCredential c = unwrapCreds(creds);
        leaveDomain(c.getUsername(), c.getPassword());
        handleLeaveResult(realmConfig);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.ADRealm#leave(javax.security.auth.Subject, java.nio.file.attribute.GroupPrincipal,
     *      java.nio.file.attribute.GroupPrincipal)
     */
    @Override
    public void leave ( Subject subj, GroupPrincipal realmGroup, GroupPrincipal hostGroup ) throws ADException {
        AbstractADRealmConfigImpl realmConfig = checkJoin();
        leaveDomain(subj);
        handleLeaveResult(realmConfig);
    }


    /**
     * 
     * @throws ADException
     */
    @Override
    public void rekey ( GroupPrincipal realmGroup, GroupPrincipal hostGroup ) throws ADException {
        AbstractADRealmConfigImpl config = checkJoin();
        JoinResult result = rekey();
        handleJoinResult(config, result, realmGroup, hostGroup);
    }


    /**
     * @param random
     * @return
     */
    private static String makeRandomPassword ( SecureRandom random ) {
        String passwordToSet;
        byte[] randomPasswordBytes = new byte[32];
        for ( int i = 0; i < randomPasswordBytes.length; i++ ) {
            // range is printable ASCII: 32 <= x < 128
            randomPasswordBytes[ i ] = (byte) ( random.nextInt(127 - 32) + 32 );
        }
        passwordToSet = new String(randomPasswordBytes, Charset.forName("ASCII")); //$NON-NLS-1$
        return passwordToSet;
    }


    /**
     * @param createUsername
     * @param dcerpcHandle
     * @param openDom
     * @return user handle
     * @throws IOException
     * @throws DcerpcException
     */
    static UserResult createUser ( String createUsername, DcerpcHandle dcerpcHandle, SamrOpenDomain openDom ) throws IOException, DcerpcException {
        SamrCreateUser2InDomain createUser = new SamrCreateUser2InDomain(openDom.getDomainHandle(), createUsername, 0x80, 0xe00500b0);
        dcerpcHandle.sendrecv(createUser);
        return new UserResult(createUser.getUserHandle(), createUser.getRelativeId());
    }


    /**
     * 
     * @param createUsername
     * @param dcerpcHandle
     * @param openDom
     * @return
     * @throws IOException
     * @throws DcerpcException
     */
    private static UserResult getUser ( String createUsername, DcerpcHandle dcerpcHandle, SamrOpenDomain openDom )
            throws IOException, DcerpcException {
        SamrLookupNamesInDomain lookupUser = new SamrLookupNamesInDomain(openDom.getDomainHandle(), 1, Arrays.asList(createUsername));
        dcerpcHandle.sendrecv(lookupUser);

        int rid = lookupUser.getRelativeIds().get(0);

        SamrOpenUser openUser = new SamrOpenUser(openDom.getDomainHandle(), 0x02000000, lookupUser.getRelativeIds().get(0));
        dcerpcHandle.sendrecv(openUser);
        policy_handle userHandle = openUser.getUserHandle();

        return new UserResult(userHandle, rid);

    }

}
