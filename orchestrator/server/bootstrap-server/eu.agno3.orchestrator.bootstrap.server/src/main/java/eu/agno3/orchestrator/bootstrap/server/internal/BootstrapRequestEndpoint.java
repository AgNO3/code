/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.orchestrator.bootstrap.server.internal;


import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.persistence.EntityManager;
import javax.transaction.Status;
import javax.transaction.SystemException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import eu.agno3.orchestrator.bootstrap.BootstrapPlugin;
import eu.agno3.orchestrator.bootstrap.msg.BootstrapRequestMessage;
import eu.agno3.orchestrator.bootstrap.msg.BootstrapResponseMessage;
import eu.agno3.orchestrator.bootstrap.service.BootstrapService;
import eu.agno3.orchestrator.config.crypto.truststore.TruststoreResourceLibraryDescriptor;
import eu.agno3.orchestrator.config.crypto.truststore.TruststoreUtil;
import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.HostConfigurationImpl;
import eu.agno3.orchestrator.config.hostconfig.HostIdentification;
import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectMutable;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectState;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrary;
import eu.agno3.orchestrator.config.model.realm.server.service.DefaultRealmServicesContext;
import eu.agno3.orchestrator.config.model.realm.server.service.ResourceLibraryServerService;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfiguration;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfigurationImpl;
import eu.agno3.orchestrator.config.orchestrator.desc.OrchestratorServiceTypeDescriptor;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.orchestrator.server.security.LocalSecurityInitializer;
import eu.agno3.runtime.crypto.scrypt.SCryptResult;
import eu.agno3.runtime.crypto.tls.TrustConfiguration;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.listener.MessageProcessingException;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;
import eu.agno3.runtime.security.SecurityManagementException;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;
import eu.agno3.runtime.transaction.TransactionContext;
import eu.agno3.runtime.util.detach.Detach;
import eu.agno3.runtime.util.detach.DetachedRunnable;
import eu.agno3.runtime.util.net.LocalHostUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = RequestEndpoint.class, property = "msgType=eu.agno3.orchestrator.bootstrap.msg.BootstrapRequestMessage" )
public class BootstrapRequestEndpoint
        implements RequestEndpoint<BootstrapRequestMessage, BootstrapResponseMessage, DefaultXmlErrorResponseMessage>, Runnable {

    /**
     * 
     */
    private static final Charset FOR_NAME = Charset.forName("UTF-8"); //$NON-NLS-1$
    private static final String ADMIN_USER = "admin"; //$NON-NLS-1$
    private static final String ADMIN_ROLE = "ADMIN"; //$NON-NLS-1$

    private static final @NonNull String INTERNAL_TRUST_LIBRARY = "internal"; //$NON-NLS-1$
    private static final @NonNull String GLOBAL_TRUST_LIBRARY = "global"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(BootstrapRequestEndpoint.class);
    private static Path BOOTSTRAP_PATH = null;

    private Optional<@NonNull ServerMessageSource> msgSource = Optional.empty();

    private ExecutorService executor;

    private BootstrapService bootstrapService;
    private BootstrapRequestMessage bootstrapRequest;

    private DefaultServerServiceContext context;
    private DefaultRealmServicesContext realmContext;

    private LocalSecurityInitializer securityIntializer;
    private TrustConfiguration trustConfiguration;
    private ResourceLibraryServerService resourceLibraryService;
    private PasswordPolicyChecker passwordPolicy;
    private Set<BootstrapPlugin> plugins = new HashSet<>();


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.executor = Executors.newSingleThreadExecutor();
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        this.executor.shutdownNow();
        try {
            this.executor.awaitTermination(1, TimeUnit.SECONDS);
        }
        catch ( InterruptedException e ) {
            log.warn("Interrupted while waiting for bootstrap process termination", e); //$NON-NLS-1$
        }

        this.executor = null;
    }


    @Reference
    protected synchronized void setMessageSource ( @NonNull MessageSource ms ) {
        this.msgSource = Optional.of((ServerMessageSource) ms);
    }


    protected synchronized void unsetMessageSource ( MessageSource ms ) {
        if ( this.msgSource.equals(ms) ) {
            this.msgSource = Optional.empty();
        }
    }


    @Reference
    protected synchronized void setContext ( DefaultServerServiceContext ctx ) {
        this.context = ctx;
    }


    protected synchronized void unsetContext ( DefaultServerServiceContext ctx ) {
        if ( this.context == ctx ) {
            this.context = null;
        }
    }


    @Reference
    protected synchronized void setRealmContext ( DefaultRealmServicesContext rctx ) {
        this.realmContext = rctx;
    }


    protected synchronized void unsetRealmContext ( DefaultRealmServicesContext rctx ) {
        if ( this.realmContext == rctx ) {
            this.realmContext = null;
        }
    }


    @Reference
    protected synchronized void setSecurityInitializer ( LocalSecurityInitializer si ) {
        this.securityIntializer = si;
    }


    protected synchronized void unsetSecurityInitializer ( LocalSecurityInitializer si ) {
        if ( this.securityIntializer == si ) {
            this.securityIntializer = null;
        }
    }


    @Reference ( target = "(instanceId=internal)" )
    protected synchronized void setTrustConfiguration ( TrustConfiguration tc ) {
        this.trustConfiguration = tc;
    }


    protected synchronized void unsetTrustConfiguration ( TrustConfiguration tc ) {
        if ( this.trustConfiguration == tc ) {
            this.trustConfiguration = null;
        }
    }


    @Reference
    protected synchronized void setResourceLibraryService ( ResourceLibraryServerService rls ) {
        this.resourceLibraryService = rls;
    }


    protected synchronized void unsetResourceLibraryService ( ResourceLibraryServerService rls ) {
        if ( this.resourceLibraryService == rls ) {
            this.resourceLibraryService = null;
        }
    }


    @Reference
    protected synchronized void setPasswordPolicy ( PasswordPolicyChecker ppc ) {
        this.passwordPolicy = ppc;
    }


    protected synchronized void unsetPasswordPolicy ( PasswordPolicyChecker ppc ) {
        if ( this.passwordPolicy == ppc ) {
            this.passwordPolicy = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void bindBootstrapPlugin ( BootstrapPlugin bp ) {
        this.plugins.add(bp);
    }


    protected synchronized void unbindBootstrapPlugin ( BootstrapPlugin bp ) {
        this.plugins.remove(bp);
    }


    @Reference
    protected synchronized void setBootstrapService ( BootstrapService bs ) {
        this.bootstrapService = bs;
    }


    protected synchronized void unsetBootstrapService ( BootstrapService bs ) {
        if ( this.bootstrapService == bs ) {
            this.bootstrapService = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#onReceive(eu.agno3.runtime.messaging.msg.RequestMessage)
     */
    @Override
    public synchronized BootstrapResponseMessage onReceive ( @NonNull BootstrapRequestMessage msg ) throws MessageProcessingException {
        log.debug("Recieved bootstrap request"); //$NON-NLS-1$
        this.bootstrapRequest = msg;
        final Executor exec = this.executor;

        try {
            // make sure not to inherit the outer transaction
            Detach.runDetached(new DetachedRunnable<Object>() {

                @Override
                public Object run () throws Exception {
                    exec.execute(BootstrapRequestEndpoint.this);
                    return null;
                }

            });
        }
        catch ( Exception e ) {
            log.error("Failed to run detached", e); //$NON-NLS-1$
        }

        return new BootstrapResponseMessage(this.msgSource.get(), msg);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#getMessageType()
     */
    @Override
    public Class<BootstrapRequestMessage> getMessageType () {
        return BootstrapRequestMessage.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {
        SCryptResult passwordHash;
        clearMessages();
        int entropy;
        try {
            String adminPassword = this.bootstrapRequest.getAdminPassword();
            passwordHash = this.securityIntializer.generatePasswordHash(adminPassword);
            entropy = this.passwordPolicy.estimateEntropy(adminPassword);
        }
        catch ( Exception e ) {
            log.error("Failed to generate password hash", e); //$NON-NLS-1$
            return;
        }

        try {
            try ( TransactionContext ctx = this.context.getTransactionService().ensureTransacted() ) {
                log.info("Running bootstrap process"); //$NON-NLS-1$
                EntityManager em = this.context.createConfigEM();
                doEnsureSecurity(passwordHash);
                doEnsureTrust(em);
                if ( !doBootstrapHost(em, entropy) ) {
                    log.error("Failed to create configurations"); //$NON-NLS-1$
                    this.context.getTransactionService().getTransactionManager().rollback();
                    return;
                }
                em.flush();

                // really force the transaction to commit
                this.context.getTransactionService().getTransactionManager().commit();
                log.info("Running bootstrap complete"); //$NON-NLS-1$
                notifyOfSuccess();
            }
            catch ( Exception e ) {
                notifyError("Unexpected error", e); //$NON-NLS-1$
                try {
                    if ( this.context.getTransactionService().getTransactionManager().getStatus() != Status.STATUS_NO_TRANSACTION ) {
                        this.context.getTransactionService().getTransactionManager().rollback();
                    }
                }
                catch (
                    IllegalStateException |
                    SecurityException |
                    SystemException e1 ) {
                    log.warn("Failed to rollback transaction", e1); //$NON-NLS-1$
                }
                return;
            }
            if ( this.bootstrapRequest.getAutoRun() ) {
                makeAutoRunComplete();
            }
        }
        finally {
            this.bootstrapRequest = null;
        }
    }


    /**
     * 
     */
    private void makeAutoRunComplete () {
        try ( TransactionContext tc = this.context.getTransactionService().ensureTransacted() ) {
            this.bootstrapService.autoCompleteBootstrap();
            tc.commit();
        }
        catch ( Exception e ) {
            log.error("Failed to automatically complete the configuration", e); //$NON-NLS-1$
        }

    }


    /**
     * @param em
     * @throws ModelServiceException
     * @throws ModelObjectConflictException
     * @throws ModelObjectValidationException
     * @throws ModelObjectReferentialIntegrityException
     * @throws ModelObjectNotFoundException
     */
    private void doEnsureTrust ( @NonNull EntityManager em ) throws ModelServiceException, ModelObjectNotFoundException,
            ModelObjectReferentialIntegrityException, ModelObjectValidationException, ModelObjectConflictException {
        StructuralObjectMutable structureRoot = this.realmContext.getStructureService().getStructureRoot(em);
        setupInternalTrustLibrary(em, structureRoot);
        ensureGlobalTrustLibrary(em, structureRoot);
    }


    /**
     * @param em
     * @param structureRoot
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectValidationException
     * @throws ModelObjectReferentialIntegrityException
     * @throws ModelObjectConflictException
     * 
     */
    private void ensureGlobalTrustLibrary ( @NonNull EntityManager em, @NonNull StructuralObject structureRoot ) throws ModelObjectNotFoundException,
            ModelServiceException, ModelObjectReferentialIntegrityException, ModelObjectValidationException, ModelObjectConflictException {
        ResourceLibrary trustLibrary = null;
        for ( ResourceLibrary resourceLibrary : this.resourceLibraryService.getResourceLibraries(em, structureRoot) ) {
            if ( TruststoreResourceLibraryDescriptor.RESOURCE_LIBRARY_TYPE.equals(resourceLibrary.getType())
                    && GLOBAL_TRUST_LIBRARY.equals(resourceLibrary.getName()) ) {
                trustLibrary = resourceLibrary;
                break;
            }
        }

        if ( trustLibrary == null ) {
            log.debug("Global trust library did not exist"); //$NON-NLS-1$
            trustLibrary = this.resourceLibraryService
                    .create(em, structureRoot, null, GLOBAL_TRUST_LIBRARY, TruststoreResourceLibraryDescriptor.RESOURCE_LIBRARY_TYPE, true);
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Global trust library is " + trustLibrary); //$NON-NLS-1$
        }
    }


    private void setupInternalTrustLibrary ( @NonNull EntityManager em, @NonNull StructuralObject structureRoot ) throws ModelServiceException,
            ModelObjectNotFoundException, ModelObjectReferentialIntegrityException, ModelObjectValidationException, ModelObjectConflictException {

        ResourceLibrary trustLibrary = null;
        for ( ResourceLibrary resourceLibrary : this.resourceLibraryService.getResourceLibraries(em, structureRoot) ) {
            if ( TruststoreResourceLibraryDescriptor.RESOURCE_LIBRARY_TYPE.equals(resourceLibrary.getType())
                    && INTERNAL_TRUST_LIBRARY.equals(resourceLibrary.getName()) ) {
                trustLibrary = resourceLibrary;
                break;
            }
        }

        if ( trustLibrary == null ) {
            log.debug("Internal trust library did not exist"); //$NON-NLS-1$
            trustLibrary = this.resourceLibraryService
                    .create(em, structureRoot, null, INTERNAL_TRUST_LIBRARY, TruststoreResourceLibraryDescriptor.RESOURCE_LIBRARY_TYPE, true);
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Internal trust library is " + trustLibrary); //$NON-NLS-1$
        }

        try {
            for ( X509Certificate cert : getTrustedCertificates() ) {
                try {
                    log.info("Adding trusted certificate " + cert.getSubjectX500Principal().toString()); //$NON-NLS-1$
                    this.resourceLibraryService.putFile(
                        em,
                        trustLibrary,
                        TruststoreUtil.makeCertificatePath(cert),
                        false,
                        new DataHandler(new ByteArrayDataSource(cert.getEncoded(), "application/octet-stream"))); //$NON-NLS-1$
                }
                catch (
                    ModelObjectConflictException |
                    CertificateEncodingException e ) {
                    log.error("Failed to add certificate", e); //$NON-NLS-1$
                }
            }
        }
        catch ( KeyStoreException e ) {
            log.error("Failed to get trusted certificates", e); //$NON-NLS-1$
            throw new ModelServiceException(e);
        }
    }


    /**
     * @return
     * @throws KeyStoreException
     */
    private Set<X509Certificate> getTrustedCertificates () throws KeyStoreException {
        KeyStore trustStore = this.trustConfiguration.getTrustStore();
        Enumeration<String> aliases = trustStore.aliases();
        Set<X509Certificate> certs = new HashSet<>();

        while ( aliases.hasMoreElements() ) {
            String alias = aliases.nextElement();
            if ( !trustStore.isCertificateEntry(alias) ) {
                continue;
            }

            Certificate certificate = trustStore.getCertificate(alias);
            if ( ! ( certificate instanceof X509Certificate ) ) {
                continue;
            }

            certs.add((X509Certificate) certificate);
        }
        return certs;
    }


    private static void notifyError ( String msg, Throwable t ) {
        log.error(msg, t);
        writeMessage(msg);
        writeHTMLStatus("msg-error", msg); //$NON-NLS-1$
        try {
            Files.write(getBasePath().resolve("failed"), new byte[] {}, StandardOpenOption.CREATE); //$NON-NLS-1$
        }
        catch ( IOException e ) {
            log.error("Failed to create status file", e); //$NON-NLS-1$
        }
    }


    /**
     * @param msg
     */
    private static void writeMessage ( String msg ) {
        try {
            Files.write(getMessageFile(), msg.getBytes(FOR_NAME), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        }
        catch ( IOException e ) {
            log.error("Failed to write message file", e); //$NON-NLS-1$
        }
    }


    @SuppressWarnings ( "nls" )
    private static void writeHTMLStatus ( String style, String data ) {
        try {
            Path htmlStatusFile = getHTMLStatusFile();
            Set<PosixFilePermission> filePerms = PosixFilePermissions.fromString("rw-r--r--");
            Path tmp = Files.createTempFile(htmlStatusFile.getParent(), "status", ".html", PosixFilePermissions.asFileAttribute(filePerms));
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("<!DOCTYPE html>");
                sb.append("<html>");
                sb.append("<head>");
                sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/style.css\"/>");
                sb.append("</head>");
                sb.append("<body>");
                sb.append("<div class=\"");
                sb.append(style);
                sb.append("\">");
                sb.append(data);
                sb.append("</div>");
                sb.append("</body>");
                sb.append("</html>");
                Files.write(tmp, sb.toString().getBytes(StandardCharsets.UTF_8));
                Files.setPosixFilePermissions(tmp, filePerms);
                Files.move(tmp, htmlStatusFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
            finally {
                Files.deleteIfExists(tmp);
            }
        }
        catch ( Exception e ) {
            log.error("Failed to write HTML status", e);
        }

    }


    private static void clearMessages () {
        try {
            Files.deleteIfExists(getMessageFile());
        }
        catch ( IOException e ) {
            log.error("Failed to clear message file", e); //$NON-NLS-1$
        }
    }


    /**
     * @return
     * @throws IOException
     */
    private static Path getMessageFile () throws IOException {
        return getBasePath().resolve("messages"); //$NON-NLS-1$
    }


    private static Path getHTMLStatusFile () throws IOException {
        return getBasePath().resolve("html/status.html"); //$NON-NLS-1$
    }


    /**
     * @return
     * @throws IOException
     */
    private static Path getBasePath () throws IOException {

        if ( BOOTSTRAP_PATH == null ) {
            Path base = Paths.get("/run/bootstrap/"); //$NON-NLS-1$

            if ( !Files.isDirectory(base) || !Files.isWritable(base) ) {
                base = Files.createTempDirectory("bootstrap"); //$NON-NLS-1$
                Files.createDirectory(base.resolve("html")); //$NON-NLS-1$
            }

            BOOTSTRAP_PATH = base;
        }

        return BOOTSTRAP_PATH;
    }


    /**
     * 
     */
    private void notifyOfSuccess () {
        String uiLocation = String.format("https://%s:8443/gui/", LocalHostUtil.guessPrimaryHostName()); //$NON-NLS-1$

        writeConsoleSuccessMessage(uiLocation);

        writeHTMLStatus(
            "msg-success", //$NON-NLS-1$
            String.format(
                "Bootstrap complete, you may now complete the configuration at <a target=\"_top\" href=\"%s\">%s</a>.", //$NON-NLS-1$
                uiLocation,
                uiLocation));

        try {
            Files.deleteIfExists(getBasePath().resolve("running")); //$NON-NLS-1$
        }
        catch ( IOException e ) {
            log.error("Failed to remove running lock", e); //$NON-NLS-1$
        }
    }


    /**
     * @param uiLocation
     */
    void writeConsoleSuccessMessage ( String uiLocation ) {
        StringBuilder sb = new StringBuilder();
        sb.append("You can now configure the system using the web interface at").append(System.lineSeparator()).append(System.lineSeparator()); //$NON-NLS-1$
        sb.append(uiLocation).append(System.lineSeparator());
        sb.append(System.lineSeparator());

        X509Certificate caCert = this.bootstrapRequest.getCaCertificate();
        if ( caCert != null ) {
            printCACertificateFingerprint(sb, caCert);
        }
        else {
            log.error("CA Certificate not known"); //$NON-NLS-1$
        }

        X509Certificate webCert = this.bootstrapRequest.getWebCertificate();
        if ( webCert != null ) {
            printWebCertificateFingerprint(sb, webCert);
        }
        else {
            log.error("Web Certificate not known"); //$NON-NLS-1$
        }

        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());

        writeMessage(sb.toString());
    }


    /**
     * @param sb
     * @param webCert
     */
    private static void printWebCertificateFingerprint ( StringBuilder sb, X509Certificate webCert ) {
        sb.append("The generated web certificate fingerprint is").append(System.lineSeparator()); //$NON-NLS-1$
        try {
            printDigest(sb, webCert);
            sb.append("You should check that web interface certificate matches this hash.").append(System.lineSeparator()); //$NON-NLS-1$
        }
        catch (
            NoSuchAlgorithmException |
            CertificateEncodingException e ) {
            log.error("Failed to create web certificate digest", e); //$NON-NLS-1$
        }
    }


    /**
     * @param sb
     * @param caCert
     */
    private static void printCACertificateFingerprint ( StringBuilder sb, X509Certificate caCert ) {
        sb.append("The generated CA's fingerprint is").append(System.lineSeparator()); //$NON-NLS-1$
        try {
            printDigest(sb, caCert);
            sb.append("You should check that web interface certificate is signed by this CA.").append(System.lineSeparator()); //$NON-NLS-1$
        }
        catch (
            NoSuchAlgorithmException |
            CertificateEncodingException e ) {
            log.error("Failed to create ca certificate digest", e); //$NON-NLS-1$
        }
    }


    /**
     * @param sb
     * @param caCert
     * @throws NoSuchAlgorithmException
     * @throws CertificateEncodingException
     */
    static void printDigest ( StringBuilder sb, X509Certificate caCert ) throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256"); //$NON-NLS-1$
        byte[] digest = md.digest(caCert.getEncoded());
        boolean first = true;
        for ( int i = 0; i < digest.length; i += 2 ) {
            if ( !first ) {
                sb.append(':');
            }
            else {
                first = false;
            }
            sb.append(String.format("%02X%02X", digest[ i ], digest[ i + 1 ])); //$NON-NLS-1$
        }
        sb.append(System.lineSeparator());
    }


    /**
     * @param ctx
     * @param em
     * @param passwordHash
     */
    private void doEnsureSecurity ( SCryptResult passwordHash ) {
        try {
            this.securityIntializer.ensureAdminPermissions();
        }
        catch ( SecurityManagementException e ) {
            notifyError("Failed to reset admin permissions", e); //$NON-NLS-1$
        }

        try {
            this.securityIntializer.ensureUserExists(ADMIN_USER, passwordHash, Collections.singleton(ADMIN_ROLE));
        }
        catch ( SecurityManagementException e ) {
            notifyError("Failed to ensure admin user exists", e); //$NON-NLS-1$
        }
    }


    /**
     * @param em
     * @param entropy
     * @param ut
     */
    protected boolean doBootstrapHost ( @NonNull EntityManager em, int entropy ) {
        InstanceStructuralObjectImpl instance = getOrCreateInstance(em, entropy);
        if ( instance == null ) {
            return false;
        }

        // we might check whether the services are valid and create another instance instead if they are not.
        return doBootstrapInstance(em, instance);
    }


    /**
     * @param em
     * @param instance
     * @return
     */
    boolean doBootstrapInstance ( @NonNull EntityManager em, @NonNull InstanceStructuralObjectImpl instance ) {
        Set<@NonNull String> createdServiceTypes = new HashSet<>();
        HostConfiguration hc;
        try {
            ServiceStructuralObject hcService = this.bootstrapRequest.getHostConfigService();
            ServiceStructuralObjectImpl service = this.realmContext.getServiceService()
                    .getOrCreateService(em, instance, HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE, hcService.getId());
            hc = createHostConfiguration(em, service);
            createdServiceTypes.add(HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE);
        }
        catch (
            ModelServiceException |
            ModelObjectException |
            UndeclaredThrowableException e ) {
            log.error("Failed to update/create host service", e); //$NON-NLS-1$
            return false;
        }

        ServiceStructuralObject orchService = this.bootstrapRequest.getServerService();
        if ( orchService != null ) {
            try {
                @NonNull
                ServiceStructuralObjectImpl oc = this.realmContext.getServiceService()
                        .getOrCreateService(em, instance, OrchestratorServiceTypeDescriptor.ORCHESTRATOR_SERVICE_TYPE, orchService.getId());
                createServerConfiguration(em, oc);
                createdServiceTypes.add(OrchestratorServiceTypeDescriptor.ORCHESTRATOR_SERVICE_TYPE);
            }
            catch (
                ModelObjectNotFoundException |
                ModelObjectConflictException |
                ModelObjectReferentialIntegrityException |
                ModelObjectValidationException |
                ModelServiceException |
                UndeclaredThrowableException e ) {
                log.error("Failed to update/create orchestrator service", e); //$NON-NLS-1$
                return false;
            }
        }

        try {
            this.realmContext.getStructureService().createRequirements(em, instance, createdServiceTypes);
        }
        catch (
            ModelObjectNotFoundException |
            ModelObjectReferentialIntegrityException |
            ModelObjectValidationException |
            ModelServiceException |
            UndeclaredThrowableException e ) {
            log.error("Failed to create auxillary services", e); //$NON-NLS-1$
        }

        for ( BootstrapPlugin plug : this.plugins ) {
            try {
                plug.setupServices(em, instance, hc);
            }
            catch (
                ModelObjectException |
                ModelServiceException |
                UndeclaredThrowableException e ) {
                log.error("Failed to run plugin", e); //$NON-NLS-1$
            }
        }
        return true;
    }


    /**
     * @param em
     * @param entropy
     * @return
     */
    protected InstanceStructuralObjectImpl getOrCreateInstance ( @NonNull EntityManager em, int entropy ) {
        UUID agentId = this.bootstrapRequest.getOrigin().getAgentId();
        InstanceStructuralObjectImpl instance = null;
        @Nullable
        HostIdentification hostIdentification = this.bootstrapRequest.getBootstrapHostConfig().getHostIdentification();
        String displayName = null;
        if ( hostIdentification != null ) {
            log.info("Setting display name to " + hostIdentification.getHostName()); //$NON-NLS-1$
            displayName = hostIdentification.getHostName();
        }
        try {
            instance = this.realmContext.getInstanceService().getInstanceForAgent(em, agentId);
            this.realmContext.getStructureService().setObjectState(em, instance, StructuralObjectState.BOOTSTRAPPING);
            log.info("An instance for this agent does already exist: " + instance); //$NON-NLS-1$

            try {
                if ( !StringUtils.isBlank(displayName) ) {
                    instance.setDisplayName(displayName);
                }
                instance.setBootstrapPasswordEntropy(entropy);
                this.realmContext.getStructureService().update(em, instance);
            }
            catch ( ModelObjectValidationException e ) {
                log.warn("Failed to rename instance", e); //$NON-NLS-1$
            }
        }
        catch ( ModelObjectNotFoundException e ) {
            log.debug("Instance not found", e); //$NON-NLS-1$
            instance = createNewInstance(agentId, displayName, em, entropy);
        }
        catch ( ModelServiceException e ) {
            notifyError("Failed to check for instance", e); //$NON-NLS-1$
            return null;
        }
        return instance;
    }


    /**
     * @param em
     * @param service
     */
    protected HostConfiguration createHostConfiguration ( @NonNull EntityManager em, @NonNull ServiceStructuralObjectImpl service ) {
        log.info("Creating host configuration"); //$NON-NLS-1$
        HostConfiguration actualConfig = null;
        ConfigurationObject currentConfig = null;
        try {
            currentConfig = this.realmContext.getServiceService().getServiceConfiguration(em, service);
        }
        catch (
            ModelObjectNotFoundException |
            ModelServiceException e ) {
            notifyError("Failed to retrieve current configuration", e); //$NON-NLS-1$
            return null;
        }

        try {
            HostConfiguration bootstrapHostConfig = this.bootstrapRequest.getBootstrapHostConfig();

            if ( bootstrapHostConfig == null ) {
                throw new ModelServiceException("Bootstrap host config is null"); //$NON-NLS-1$
            }

            // set id to current id if such exists
            if ( bootstrapHostConfig instanceof AbstractConfigurationObject && currentConfig.getId() != null ) {
                @SuppressWarnings ( "unchecked" )
                AbstractConfigurationObject<HostConfiguration> cfg = (AbstractConfigurationObject<HostConfiguration>) bootstrapHostConfig;
                cfg.setId(currentConfig.getId());
                cfg.setVersion(currentConfig.getVersion());
            }

            actualConfig = this.realmContext.getServiceService()
                    .updateServiceConfiguration(em, service, (HostConfigurationImpl) bootstrapHostConfig, null);
        }
        catch (
            ModelObjectNotFoundException |
            ModelObjectValidationException |
            ModelServiceException |
            ModelObjectConflictException e ) {
            notifyError("Failed to create configuration from bootstrap config", e); //$NON-NLS-1$
            return null;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Created host configuration " + actualConfig); //$NON-NLS-1$
        }
        return actualConfig;
    }


    /**
     * @param em
     * @param service
     */
    private void createServerConfiguration ( @NonNull EntityManager em, @NonNull ServiceStructuralObjectImpl service ) {
        log.info("Creating server configuration"); //$NON-NLS-1$
        OrchestratorConfiguration actualConfig = null;
        ConfigurationObject currentConfig = null;
        try {
            currentConfig = this.realmContext.getServiceService().getServiceConfiguration(em, service);
        }
        catch (
            ModelObjectNotFoundException |
            ModelServiceException e ) {
            notifyError("Failed to retrieve current configuration", e); //$NON-NLS-1$
            return;
        }

        try {
            OrchestratorConfigurationImpl bootstrapOrchConfig = (OrchestratorConfigurationImpl) this.bootstrapRequest.getBootstrapServerConfig();

            if ( bootstrapOrchConfig == null ) {
                throw new ModelServiceException("Bootstrap server config is null"); //$NON-NLS-1$
            }

            // set id to current id if such exists
            if ( currentConfig.getId() != null ) {
                bootstrapOrchConfig.setId(currentConfig.getId());
                bootstrapOrchConfig.setVersion(currentConfig.getVersion());
            }

            actualConfig = this.realmContext.getServiceService().updateServiceConfiguration(em, service, bootstrapOrchConfig, null);
        }
        catch (
            ModelObjectNotFoundException |
            ModelObjectValidationException |
            ModelServiceException |
            ModelObjectConflictException e ) {
            notifyError("Failed to create configuration from bootstrap config", e); //$NON-NLS-1$
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Created server configuration " + actualConfig); //$NON-NLS-1$
        }

    }


    /**
     * @param agentId
     * @param displayName
     * @param em
     * @param entropy
     * @param instance
     * @param e
     * @return
     */
    protected InstanceStructuralObjectImpl createNewInstance ( UUID agentId, String displayName, @NonNull EntityManager em, int entropy ) {
        log.info("Creating new instance"); //$NON-NLS-1$
        StructuralObjectMutable structureRoot;
        try {
            structureRoot = this.realmContext.getStructureService().getStructureRoot(em);
        }
        catch ( ModelServiceException e ) {
            notifyError("Failed to get structural root", e); //$NON-NLS-1$
            return null;
        }

        InstanceStructuralObjectImpl toCreate = new InstanceStructuralObjectImpl();
        toCreate.setAgentId(agentId);
        toCreate.setBootstrapPasswordEntropy(entropy);
        toCreate.setImageType(this.bootstrapRequest.getImageType());
        if ( displayName != null ) {
            toCreate.setDisplayName(displayName);
        }
        toCreate.setPersistentState(StructuralObjectState.BOOTSTRAPPING);
        try {
            return this.realmContext.getStructureService().create(em, structureRoot, toCreate, true);
        }
        catch (
            ModelObjectNotFoundException |
            ModelObjectConflictException |
            ModelObjectReferentialIntegrityException |
            ModelObjectValidationException |
            ModelServiceException e ) {
            notifyError("Failed to create instance", e); //$NON-NLS-1$
            return null;
        }
    }

}
