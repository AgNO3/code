/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.truststore.internal;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManagerException;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.file.util.FileTemporaryUtils;
import eu.agno3.orchestrator.system.file.util.FileUtil;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.openssl.AllHashedMatchingFilter;
import eu.agno3.runtime.crypto.openssl.HashMatchingFilter;
import eu.agno3.runtime.crypto.openssl.NameHashUtil;
import eu.agno3.runtime.crypto.truststore.revocation.RevocationConfig;


/**
 * @author mbechler
 *
 */
public class OpenSSLTruststoreManager implements TruststoreManager {

    private static final Logger log = Logger.getLogger(OpenSSLTruststoreManager.class);

    /**
     * 
     */
    private static final String X509 = "X509"; //$NON-NLS-1$
    private static final Charset PEM_CHARSET = Charset.forName("US-ASCII"); //$NON-NLS-1$
    private File openSSLPath;
    private File openSSLCRLPath;


    /**
     * @param trustStoreFile
     * 
     */
    public OpenSSLTruststoreManager ( File trustStoreFile ) {
        this.openSSLPath = new File(trustStoreFile, "openssl"); //$NON-NLS-1$
        this.openSSLCRLPath = new File(trustStoreFile, "opensslCRL"); //$NON-NLS-1$
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
        try {
            if ( !this.openSSLPath.exists() ) {
                Files.createDirectory(
                    this.openSSLPath.toPath(),
                    PosixFilePermissions.asFileAttribute(FileSecurityUtils.getWorldReadableDirPermissions()));
            }
            if ( !this.openSSLCRLPath.exists() ) {
                Files.createDirectory(
                    this.openSSLCRLPath.toPath(),
                    PosixFilePermissions.asFileAttribute(FileSecurityUtils.getWorldReadableDirPermissions()));
            }
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException("Failed to create directories", e); //$NON-NLS-1$
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
            FileUtils.deleteDirectory(this.openSSLPath);
        }
        catch ( IOException e ) {
            log.warn("Failed to remove certificate directory", e); //$NON-NLS-1$
        }

        try {
            FileUtils.deleteDirectory(this.openSSLCRLPath);
        }
        catch ( IOException e ) {
            log.warn("Failed to remove CRL directory", e); //$NON-NLS-1$
        }

        try {
            init();
        }
        catch ( TruststoreManagerException e ) {
            log.warn("Failed to re-initialize truststore", e); //$NON-NLS-1$
        }
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


    /**
     * {@inheritDoc}
     * 
     * @throws TruststoreManagerException
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#hasCertificate(java.security.cert.X509Certificate)
     */
    @Override
    public boolean hasCertificate ( X509Certificate cert ) throws TruststoreManagerException {
        if ( findCert(cert) != null ) {
            return true;
        }
        return false;
    }


    /**
     * @param cert
     * @param cf
     * @throws TruststoreManagerException
     * @throws CertificateException
     * @throws IOException
     * @throws FileNotFoundException
     */
    protected File findCert ( X509Certificate cert ) throws TruststoreManagerException {
        CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance(X509);
        }
        catch ( CertificateException e ) {
            throw new TruststoreManagerException("Failed to get certificate factory", e); //$NON-NLS-1$
        }
        return findCert(getHash(cert), cert, cf);
    }


    /**
     * @param hash
     * @param cert
     * @param cf
     * @return
     * @throws CertificateException
     * @throws TruststoreManagerException
     * @throws IOException
     * @throws FileNotFoundException
     */
    protected File findCert ( String hash, X509Certificate cert, CertificateFactory cf ) throws TruststoreManagerException {
        File[] files = this.openSSLPath.listFiles(new HashMatchingFilter(hash));
        if ( files == null ) {
            return null;
        }
        for ( File candFile : files ) {
            if ( certInCertFile(cert, cf, candFile) ) {
                return candFile;
            }
        }

        return null;
    }


    /**
     * @param cert
     * @param cf
     * @param candFile
     * @throws CertificateException
     * @throws TruststoreManagerException
     * @throws IOException
     * @throws FileNotFoundException
     */
    private static boolean certInCertFile ( X509Certificate cert, CertificateFactory cf, File candFile ) throws TruststoreManagerException {
        try {
            if ( !candFile.isFile() || !candFile.canRead() ) {
                return false;
            }

            try ( FileInputStream fis = new FileInputStream(candFile) ) {
                Certificate candCert = cf.generateCertificate(fis);
                checkCert(candCert);

                if ( cert != null && cert.equals(candCert) ) {
                    return true;
                }
            }
            return false;
        }
        catch (
            IOException |
            CertificateException e ) {
            throw new TruststoreManagerException("Failed to read certificate", e); //$NON-NLS-1$
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
        Set<X509Certificate> certs = new HashSet<>();
        try {
            CertificateFactory cf = CertificateFactory.getInstance(X509);
            File[] files = this.openSSLPath.listFiles(new AllHashedMatchingFilter());
            if ( files == null ) {
                return Collections.EMPTY_SET;
            }

            addCertFiles(certs, cf, files);
        }
        catch ( CertificateException e ) {
            throw new TruststoreManagerException("Failed to read certificates", e); //$NON-NLS-1$
        }
        return certs;
    }


    /**
     * @param certs
     * @param cf
     * @param files
     * @throws CertificateException
     * @throws TruststoreManagerException
     * @throws IOException
     * @throws FileNotFoundException
     */
    private static void addCertFiles ( Set<X509Certificate> certs, CertificateFactory cf, File[] files ) throws TruststoreManagerException {
        for ( File candFile : files ) {
            try ( FileInputStream fis = new FileInputStream(candFile) ) {
                Certificate cert = cf.generateCertificate(fis);
                checkCert(cert);
                certs.add((X509Certificate) cert);
            }
            catch (
                IOException |
                CertificateException e ) {
                throw new TruststoreManagerException("Failed to read certificate file " + candFile, e); //$NON-NLS-1$
            }
        }
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
        try {
            CertificateFactory cf = CertificateFactory.getInstance(X509);
            String hash = getHash(cert);
            File certFile = findCert(hash, cert, cf);

            if ( certFile != null ) {
                throw new TruststoreManagerException("Certificate does already exist"); //$NON-NLS-1$
            }
            File file = findFreeFile(this.openSSLPath, hash);
            try ( FileChannel ch = FileChannel.open(
                file.toPath(),
                EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                PosixFilePermissions.asFileAttribute(FileSecurityUtils.getWorldReadableFilePermissions()));
                  OutputStream fos = Channels.newOutputStream(ch);
                  JcaPEMWriter wr = new JcaPEMWriter(new OutputStreamWriter(fos, PEM_CHARSET)) ) {
                wr.writeObject(cert);
            }

            try {
                Files.setPosixFilePermissions(file.toPath(), FileSecurityUtils.getWorldReadableFilePermissions());
            }
            catch ( IOException e ) {
                throw new TruststoreManagerException("Failed to set permissions", e); //$NON-NLS-1$
            }
        }
        catch (
            CertificateException |
            TruststoreManagerException |
            IOException e ) {
            throw new TruststoreManagerException("Failed to add certificate", e); //$NON-NLS-1$
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
            Path tempDir = FileTemporaryUtils.createRelatedTemporaryDirectory(this.openSSLPath.toPath());
            Path bakDir = FileTemporaryUtils.createRelatedTemporaryDirectory(this.openSSLPath.toPath());

            try {
                for ( X509Certificate cert : trustAnchors ) {
                    writeToFile(tempDir, cert);
                }

                replaceTruststore(tempDir, bakDir);
            }
            finally {
                FileUtils.deleteQuietly(tempDir.toFile());
                FileUtils.deleteQuietly(bakDir.toFile());
            }
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException("Failed to replace cert store", e); //$NON-NLS-1$
        }
    }


    /**
     * @param tempDir
     * @param bakDir
     * @throws IOException
     */
    private void replaceTruststore ( Path tempDir, Path bakDir ) throws IOException {
        FileUtil.safeMove(this.openSSLPath.toPath(), bakDir, true);
        try {
            FileUtil.safeMove(tempDir, this.openSSLPath.toPath(), true);
        }
        catch ( IOException e ) {
            FileUtil.safeMove(bakDir, this.openSSLPath.toPath(), true);
            throw e;
        }
    }


    /**
     * @param tempDir
     * @param cert
     * @throws TruststoreManagerException
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void writeToFile ( Path tempDir, X509Certificate cert ) throws TruststoreManagerException {
        String hash = getHash(cert);
        File file = findFreeFile(tempDir.toFile(), hash);
        try ( FileChannel ch = FileChannel.open(
            file.toPath(),
            EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
            PosixFilePermissions.asFileAttribute(FileSecurityUtils.getWorldReadableFilePermissions()));
              OutputStream fos = Channels.newOutputStream(ch);
              JcaPEMWriter wr = new JcaPEMWriter(new OutputStreamWriter(fos, PEM_CHARSET)) ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Writing certificate %s to %s", cert.getSubjectX500Principal().getName(), file.toString())); //$NON-NLS-1$
            }
            wr.writeObject(cert);
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException("Failed to write certificate", e); //$NON-NLS-1$
        }
        try {
            Files.setPosixFilePermissions(file.toPath(), FileSecurityUtils.getWorldReadableFilePermissions());
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException("Failed to set permissions", e); //$NON-NLS-1$
        }
    }


    /**
     * @param hash
     * @return
     * @throws TruststoreManagerException
     */
    protected File findFreeFile ( File base, String hash ) throws TruststoreManagerException {
        for ( int idx = 0; idx < 10; idx++ ) {
            String fname = String.format("%s.%d", hash, idx); //$NON-NLS-1$
            File f = new File(base, fname);

            if ( !f.exists() ) {
                return f;
            }
        }
        throw new TruststoreManagerException("Failed to find a free file to store entry"); //$NON-NLS-1$
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
        try {
            File certFile = findCert(cert);

            if ( certFile == null ) {
                throw new TruststoreManagerException("Certificate to remove does not exist"); //$NON-NLS-1$
            }

            certFile.delete();
        }
        catch ( TruststoreManagerException e ) {
            throw new TruststoreManagerException("Failed to remove certificate", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#listCRLs()
     */
    @Override
    public Set<X509CRL> listCRLs () throws TruststoreManagerException {

        try {
            File[] crlFiles = this.openSSLCRLPath.listFiles(new AllHashedMatchingFilter());

            if ( crlFiles == null ) {
                return Collections.EMPTY_SET;
            }

            CertificateFactory cf = CertificateFactory.getInstance(X509);
            return listCRLsInternal(crlFiles, cf);
        }
        catch ( CertificateException e ) {
            throw new TruststoreManagerException("Failed to list CRLs", e); //$NON-NLS-1$
        }
    }


    /**
     * @param crlFiles
     * @param cf
     * @return
     * @throws TruststoreManagerException
     */
    private static Set<X509CRL> listCRLsInternal ( File[] crlFiles, CertificateFactory cf ) throws TruststoreManagerException {
        Set<X509CRL> crls = new HashSet<>();
        for ( File crlFile : crlFiles ) {
            try ( FileInputStream fis = new FileInputStream(crlFile) ) {
                crls.add((X509CRL) cf.generateCRL(fis));
            }
            catch (
                IOException |
                CRLException e ) {
                throw new TruststoreManagerException("Failed to read CRL " + crlFile, e); //$NON-NLS-1$
            }
        }
        return crls;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager#updateCRL(java.security.cert.X509CRL)
     */
    @Override
    public void updateCRL ( X509CRL crl ) throws TruststoreManagerException {
        try {
            String hash = NameHashUtil.opensslNameHash(crl.getIssuerX500Principal());

            CertificateFactory cf = CertificateFactory.getInstance(X509);
            File replaceFile = findExistingCRL(crl, hash, cf);

            if ( replaceFile == null ) {
                replaceFile = findFreeFile(this.openSSLCRLPath, hash);
            }

            writeCRL(crl, replaceFile);
        }
        catch (
            CryptoException |
            CertificateException e ) {
            throw new TruststoreManagerException("Failed to update CRL", e); //$NON-NLS-1$
        }
    }


    /**
     * @param crl
     * @throws TruststoreManagerException
     */
    private static void writeCRL ( X509CRL crl, File file ) throws TruststoreManagerException {
        try ( FileChannel ch = FileChannel.open(
            file.toPath(),
            EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
            PosixFilePermissions.asFileAttribute(FileSecurityUtils.getWorldReadableFilePermissions()));
              OutputStream fos = Channels.newOutputStream(ch);
              OutputStreamWriter fw = new OutputStreamWriter(fos, PEM_CHARSET);
              JcaPEMWriter wr = new JcaPEMWriter(fw) ) {
            wr.writeObject(crl);
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException("Failed to write CRL", e); //$NON-NLS-1$
        }
        try {
            Files.setPosixFilePermissions(file.toPath(), FileSecurityUtils.getWorldReadableFilePermissions());
        }
        catch ( IOException e ) {
            throw new TruststoreManagerException("Failed to set permissions", e); //$NON-NLS-1$
        }
    }


    /**
     * @param crl
     * @param hash
     * @param cf
     * @return
     * @throws TruststoreManagerException
     */
    protected File findExistingCRL ( X509CRL crl, String hash, CertificateFactory cf ) throws TruststoreManagerException {
        File[] files = this.openSSLCRLPath.listFiles(new HashMatchingFilter(hash));
        if ( files != null ) {
            for ( File candFile : files ) {
                if ( isCRLInFile(crl, cf, candFile) ) {
                    return candFile;
                }
            }
        }
        return null;
    }


    /**
     * @param crl
     * @param cf
     * @param candFile
     * @return
     * @throws TruststoreManagerException
     */
    private static boolean isCRLInFile ( X509CRL crl, CertificateFactory cf, File candFile ) throws TruststoreManagerException {
        try ( FileInputStream fis = new FileInputStream(candFile) ) {
            CRL candCrl = cf.generateCRL(fis);

            if ( ! ( candCrl instanceof X509CRL ) ) {
                return false;
            }
            X509CRL x509CandCrl = (X509CRL) candCrl;

            if ( x509CandCrl.getIssuerX500Principal().equals(crl.getIssuerX500Principal()) ) {
                if ( x509CandCrl.getThisUpdate().after(crl.getThisUpdate()) ) {
                    throw new TruststoreManagerException("Trying to update a CRL with an older version"); //$NON-NLS-1$
                }
                return true;
            }
        }
        catch (
            IOException |
            CRLException e ) {
            throw new TruststoreManagerException("Failed to read CRL", e); //$NON-NLS-1$
        }
        return false;
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


    private static void checkCert ( Certificate cert ) throws TruststoreManagerException {
        if ( ! ( cert instanceof X509Certificate ) ) {
            throw new TruststoreManagerException("Truststore contain non X509 certificate " + cert.getType()); //$NON-NLS-1$
        }
    }


    /**
     * @param cert
     * @return
     * @throws CryptoException
     */
    protected String getHash ( X509Certificate cert ) throws TruststoreManagerException {
        try {
            return NameHashUtil.opensslSubjectHash(cert);
        }
        catch ( CryptoException e ) {
            throw new TruststoreManagerException("Failed to get subject hash", e); //$NON-NLS-1$
        }
    }

}
