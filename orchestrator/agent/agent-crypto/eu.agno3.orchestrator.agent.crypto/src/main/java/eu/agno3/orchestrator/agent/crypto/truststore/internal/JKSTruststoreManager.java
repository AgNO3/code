/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.truststore.internal;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManagerException;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;


/**
 * @author mbechler
 *
 */
public class JKSTruststoreManager implements TruststoreManager {

    /**
     * 
     */
    private static final String TRUSTSTORE_PASS = "changeit"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(JKSTruststoreManager.class);

    /**
     * 
     */
    private static final String JKS = "JKS"; //$NON-NLS-1$

    private File trustStorePath;
    private File trustStoreFile;


    /**
     * @param trustStorePath
     * 
     */
    public JKSTruststoreManager ( File trustStorePath ) {
        this.trustStorePath = trustStorePath;
        this.trustStoreFile = new File(trustStorePath, "jks/trust.jks"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#setRevocationConfig(eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig)
     */
    @Override
    public void setRevocationConfig ( RevocationConfig config ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#getRevocationConfig()
     */
    @Override
    public RevocationConfig getRevocationConfig () {
        return null;
    }


    KeyStore loadKeyStore () throws TruststoreManagerException {
        try ( FileInputStream fin = new FileInputStream(this.trustStoreFile) ) {
            KeyStore ks = KeyStore.getInstance(JKS);
            ks.load(fin, TRUSTSTORE_PASS.toCharArray());
            return ks;
        }
        catch (
            KeyStoreException |
            IOException |
            NoSuchAlgorithmException |
            CertificateException e ) {
            throw new TruststoreManagerException("Failed to load trust store", e); //$NON-NLS-1$
        }
    }


    private void saveKeyStore ( KeyStore ks ) throws TruststoreManagerException {
        try ( FileChannel ch = FileChannel.open(
            this.trustStoreFile.toPath(),
            EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
            PosixFilePermissions.asFileAttribute(FileSecurityUtils.getWorldReadableFilePermissions())); // $NON-NLS-1$
              OutputStream fos = Channels.newOutputStream(ch) ) {
            ks.store(fos, TRUSTSTORE_PASS.toCharArray());
        }
        catch (
            KeyStoreException |
            NoSuchAlgorithmException |
            CertificateException |
            IOException e ) {
            throw new TruststoreManagerException("Failed to write to trust store", e); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     * 
     * @throws TruststoreManagerException
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#init()
     */
    @Override
    public void init () throws TruststoreManagerException {
        File jksPath = new File(this.trustStorePath, "jks"); //$NON-NLS-1$
        try {
            if ( !jksPath.exists() ) {
                Files.createDirectory(jksPath.toPath(), PosixFilePermissions.asFileAttribute(FileSecurityUtils.getWorldReadableDirPermissions()));
            }

            KeyStore ks = KeyStore.getInstance(JKS);
            ks.load(null);
            this.saveKeyStore(ks);
        }
        catch (
            KeyStoreException |
            NoSuchAlgorithmException |
            CertificateException |
            IOException e ) {
            throw new TruststoreManagerException("Failed to initialize trust store", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#deleteAllCertificates()
     */
    @Override
    public void deleteAllCertificates () {
        try {
            KeyStore ks = KeyStore.getInstance(JKS);
            ks.load(null);
            this.saveKeyStore(ks);
        }
        catch (
            KeyStoreException |
            NoSuchAlgorithmException |
            CertificateException |
            IOException |
            TruststoreManagerException e ) {
            log.warn("Failed to -reinitialize trust store", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws TruststoreManagerException
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#hasCertificate(java.security.cert.X509Certificate)
     */
    @Override
    public boolean hasCertificate ( X509Certificate cert ) throws TruststoreManagerException {
        KeyStore ks = this.loadKeyStore();
        try {
            String alias = ks.getCertificateAlias(cert);
            if ( alias == null ) {
                return false;
            }
            return ks.isCertificateEntry(alias);
        }
        catch ( KeyStoreException e ) {
            throw new TruststoreManagerException("Failed to access keystore", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws TruststoreManagerException
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#listCertificates()
     */
    @Override
    public Set<X509Certificate> listCertificates () throws TruststoreManagerException {
        KeyStore ks = this.loadKeyStore();
        Set<X509Certificate> certs = new HashSet<>();

        try {
            Enumeration<String> aliases = ks.aliases();
            while ( aliases.hasMoreElements() ) {
                listAlias(ks, certs, aliases);
            }
        }
        catch ( KeyStoreException e ) {
            throw new TruststoreManagerException("Failed to enumerate certificates", e); //$NON-NLS-1$
        }

        return certs;
    }


    /**
     * @param ks
     * @param certs
     * @param aliases
     * @throws KeyStoreException
     */
    private static void listAlias ( KeyStore ks, Set<X509Certificate> certs, Enumeration<String> aliases ) throws KeyStoreException {
        String alias = aliases.nextElement();

        if ( ks.isCertificateEntry(alias) ) {
            Certificate cert = ks.getCertificate(alias);

            if ( cert instanceof X509Certificate ) {
                certs.add((X509Certificate) cert);
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#updateCRL(java.security.cert.X509CRL)
     */
    @Override
    public void updateCRL ( X509CRL crl ) throws TruststoreManagerException {}


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#listCRLs()
     */
    @Override
    public Set<X509CRL> listCRLs () throws TruststoreManagerException {
        return Collections.EMPTY_SET;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws TruststoreManagerException
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#addCertificate(java.security.cert.X509Certificate)
     */
    @Override
    public void addCertificate ( X509Certificate cert ) throws TruststoreManagerException {
        KeyStore ks = this.loadKeyStore();

        try {
            if ( ks.getCertificateAlias(cert) != null ) {
                throw new TruststoreManagerException("Certificate does already exist in truststore"); //$NON-NLS-1$
            }

            ks.setCertificateEntry(TruststoreUtil.deriveCertAlias(cert, ks), cert);
            this.saveKeyStore(ks);
        }
        catch (
            KeyStoreException |
            CertificateEncodingException e ) {
            throw new TruststoreManagerException("Failed to add certificate", e); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     * 
     * @throws TruststoreManagerException
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#removeCertificate(java.security.cert.X509Certificate)
     */
    @Override
    public void removeCertificate ( X509Certificate cert ) throws TruststoreManagerException {
        KeyStore ks = this.loadKeyStore();

        try {
            String certificateAlias = ks.getCertificateAlias(cert);
            if ( certificateAlias == null || !ks.isCertificateEntry(certificateAlias) ) {
                throw new TruststoreManagerException("Certificate does not exist in truststore"); //$NON-NLS-1$
            }

            ks.deleteEntry(certificateAlias);
            this.saveKeyStore(ks);
        }
        catch ( KeyStoreException e ) {
            throw new TruststoreManagerException("Failed to remove certificate", e); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     * 
     * @throws TruststoreManagerException
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#setCertificates(java.util.Set)
     */
    @Override
    public void setCertificates ( Set<X509Certificate> trustAnchors ) throws TruststoreManagerException {
        try {
            KeyStore ks = KeyStore.getInstance(JKS);
            ks.load(null);
            for ( X509Certificate cert : trustAnchors ) {
                ks.setCertificateEntry(TruststoreUtil.deriveCertAlias(cert, ks), cert);
            }
            this.saveKeyStore(ks);
        }
        catch (
            KeyStoreException |
            NoSuchAlgorithmException |
            CertificateException |
            IOException e ) {
            throw new TruststoreManagerException("Failed to synchronize certificates", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#getVersion()
     */
    @Override
    public long getVersion () {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#isReadOnly()
     */
    @Override
    public boolean isReadOnly () {
        return false;
    }

}
