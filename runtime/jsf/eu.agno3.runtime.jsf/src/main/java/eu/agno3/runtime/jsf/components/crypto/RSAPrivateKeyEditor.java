/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.11.2014 by mbechler
 */
package eu.agno3.runtime.jsf.components.crypto;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.el.MethodExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.security.auth.x500.X500Principal;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.UploadedFile;

import eu.agno3.runtime.jsf.i18n.BaseMessages;


/**
 * @author mbechler
 *
 */
public class RSAPrivateKeyEditor extends UIInput implements NamingContainer {

    /**
     * 
     */
    private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$

    private static final String PKCS12 = "PKCS12"; //$NON-NLS-1$
    private static final String PEM = "PEM"; //$NON-NLS-1$
    private static final String SUN = "SunJSSE"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(RSAPrivateKeyEditor.class);

    private static final String TEXT_INPUT = "textInput"; //$NON-NLS-1$
    private static final String CHOOSE_OTHER = "chooseOther"; //$NON-NLS-1$

    private static final String PASSWORD_REQUIRED = "passwordRequired"; //$NON-NLS-1$
    private static final String SELECTION_REQUIRED = "selectionRequired"; //$NON-NLS-1$

    private static final String KEY_DATA = "keyData"; //$NON-NLS-1$

    private static final Set<String> PKCS12_EXTENSIONS = new HashSet<>();
    private static final String PKCS12_MIME_TYPE = "application/x-pkcs12"; //$NON-NLS-1$

    private static final String KEY_TYPE = "keyType"; //$NON-NLS-1$
    private static final String PASSWORD = "keyPassword"; //$NON-NLS-1$
    private static final String SELECTION_OPTIONS = "selectionOptions"; //$NON-NLS-1$
    private static final String SELECTED_ALIAS = "selectedAlias"; //$NON-NLS-1$

    private static final String FOUND_CERT_CHAIN = "foundCertChain"; //$NON-NLS-1$

    private static final String CHAIN_HANDLER = "chainHandler"; //$NON-NLS-1$

    private static final String MODIFIED = "modified"; //$NON-NLS-1$


    static {
        PKCS12_EXTENSIONS.add("p12"); //$NON-NLS-1$
        PKCS12_EXTENSIONS.add("pfx"); //$NON-NLS-1$
        PKCS12_EXTENSIONS.add("pkcs12"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#resetValue()
     */
    @Override
    public void resetValue () {
        this.getStateHelper().remove(TEXT_INPUT);
        this.getStateHelper().remove(CHOOSE_OTHER);
        this.getStateHelper().remove(KEY_DATA);
        this.getStateHelper().remove(KEY_TYPE);
        this.getStateHelper().remove(SELECTED_ALIAS);
        this.getStateHelper().remove(SELECTION_OPTIONS);
        this.getStateHelper().remove(SELECTION_REQUIRED);
        this.getStateHelper().remove(PASSWORD);
        this.getStateHelper().remove(PASSWORD_REQUIRED);
        this.getStateHelper().remove(FOUND_CERT_CHAIN);
        super.resetValue();
    }


    private MethodExpression getChainHandler () {
        return (MethodExpression) getAttributes().get(CHAIN_HANDLER);
    }


    /**
     * 
     * @return the current text (paste) input
     */
    public String getTextInput () {
        return (String) this.getStateHelper().get(TEXT_INPUT);
    }


    /**
     * 
     * @param input
     */
    public void setTextInput ( String input ) {
        this.getStateHelper().put(TEXT_INPUT, input);
    }


    /**
     * 
     * @return whether the upload/paste part is shown
     */
    public boolean shouldShowUpdate () {
        return this.getKeyData() == null || (Boolean) this.getStateHelper().eval(CHOOSE_OTHER, false);
    }


    /**
     * @return
     */
    private byte[] getKeyData () {
        return (byte[]) this.getStateHelper().get(KEY_DATA);
    }


    /**
     * @param contents
     */
    private void setKeyData ( byte[] contents ) {
        this.getStateHelper().put(KEY_DATA, contents);
    }


    /**
     * 
     * @return the detected key encoding type
     */
    public String getKeyType () {
        return (String) this.getStateHelper().get(KEY_TYPE);
    }


    private void setKeyType ( String type ) {
        this.getStateHelper().put(KEY_TYPE, type);
    }


    /**
     * 
     * @param ev
     */
    public void chooseOther ( ActionEvent ev ) {
        this.getStateHelper().put(CHOOSE_OTHER, Boolean.TRUE);
    }


    /**
     * @return the modified
     */
    public boolean getModified () {
        return (boolean) getStateHelper().eval(MODIFIED, false);
    }


    protected void markModified () {
        getStateHelper().put(MODIFIED, true);
    }


    /**
     * @return whether a password is required to access the key
     */
    public boolean isPasswordRequired () {
        return (Boolean) this.getStateHelper().eval(PASSWORD_REQUIRED, false);
    }


    /**
     * @param required
     */
    public void setPasswordRequired ( boolean required ) {
        this.getStateHelper().put(PASSWORD_REQUIRED, required);
    }


    /**
     * @return whether key selection is required
     */
    public boolean isSelectionRequired () {
        return (Boolean) this.getStateHelper().eval(SELECTION_REQUIRED, false);
    }


    /**
     * 
     * @param required
     */
    public void setSelectionRequired ( boolean required ) {
        this.getStateHelper().put(SELECTION_REQUIRED, required);
    }


    /**
     * @return the selected key alias
     */
    public String getSelectedAlias () {
        return (String) this.getStateHelper().get(SELECTED_ALIAS);
    }


    /**
     * @return the selectable options
     */
    public String[] getSelectionOptions () {
        return (String[]) this.getStateHelper().get(SELECTION_OPTIONS);
    }


    /**
     * 
     * @param options
     */
    private void setSelectionOptions ( String[] options ) {
        this.getStateHelper().put(SELECTION_OPTIONS, options);
    }


    /**
     * @param alias
     */
    public void setSelectedAlias ( String alias ) {
        this.getStateHelper().put(SELECTED_ALIAS, alias);
    }


    /**
     * 
     * @param password
     */
    public void setPassword ( String password ) {
        this.getStateHelper().put(PASSWORD, password);
    }


    /**
     * 
     * @return the key password
     */
    public String getPassword () {
        return (String) this.getStateHelper().get(PASSWORD);
    }


    /**
     * @param chain
     */
    private void setFoundCertificateChain ( Certificate[] chain ) {
        log.debug("Found certificate chain"); //$NON-NLS-1$
        this.getStateHelper().put(FOUND_CERT_CHAIN, chain);
    }


    private Certificate[] getFoundCertificateChain () {
        return (Certificate[]) this.getStateHelper().get(FOUND_CERT_CHAIN);
    }


    /**
     * 
     * @param ev
     */
    public void handleFileUpload ( FileUploadEvent ev ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Got file upload " + ev); //$NON-NLS-1$
        }
        UploadedFile f = ev.getFile();

        if ( f != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Content type: " + f.getContentType()); //$NON-NLS-1$
            }

            byte[] buf = new byte[4096];
            ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.getSize());
            try ( InputStream is = f.getInputstream() ) {
                int read;
                while ( ( read = is.read(buf) ) > 0 ) {
                    bos.write(buf, 0, read);
                }
            }
            catch ( IOException e ) {
                log.debug("Key upload failed", e); //$NON-NLS-1$
                addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, BaseMessages.get("crypto.keyUploadError"), StringUtils.EMPTY)); //$NON-NLS-1$
                return;
            }

            setValue(null);
            setFoundCertificateChain(new Certificate[0]);
            setPassword(null);
            setSelectedAlias(null);
            setKeyType(null);
            parseKeyFormat(bos.toByteArray(), f.getContentType(), f.getFileName());

        }
    }


    /**
     * @param ev
     */
    public void gotPassword ( ActionEvent ev ) {
        log.debug("Got password"); //$NON-NLS-1$
        byte[] keyData = this.getKeyData();

        if ( keyData != null ) {
            parseKeyFormat(keyData, null, null);
        }
    }


    /**
     * @param ev
     */
    public void gotSelection ( SelectEvent ev ) {
        log.debug("Got selection"); //$NON-NLS-1$
        byte[] keyData = this.getKeyData();

        if ( keyData != null ) {
            parseKeyFormat(keyData, null, null);
        }
    }


    /**
     * @param contents
     * @param contentType
     */
    private void parseKeyFormat ( byte[] contents, String contentType, String fileName ) {
        markModified();
        this.setPasswordRequired(false);
        this.setSelectionRequired(false);
        this.setFoundCertificateChain(new Certificate[0]);

        if ( contents == null || contents.length == 0 ) {
            addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, BaseMessages.get("crypto.keyNoKeyData"), StringUtils.EMPTY)); //$NON-NLS-1$
            return;
        }

        if ( PKCS12.equals(this.getKeyType()) || looksLikePKCS12(contents, contentType, fileName) ) {
            log.debug("Looks like PKCS12 data"); //$NON-NLS-1$
            if ( parsePKCS12(contents) ) {
                this.setKeyType(PKCS12);
                this.setKeyData(contents);
                return;
            }
        }

        if ( parsePEM(contents) ) {
            this.setKeyType(PEM);
            this.setKeyData(contents);
            return;
        }

        addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, BaseMessages.get("crypto.keyUnsupportedType"), StringUtils.EMPTY)); //$NON-NLS-1$

    }


    /**
     * @param contents
     * 
     */
    // seems to be a bug in eclipse
    private boolean parsePEM ( byte[] contents ) {
        // try to parse as PEM
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(new BouncyCastleProvider());
        try ( ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(contents);
              Reader ir = new InputStreamReader(byteArrayInputStream, Charset.forName(UTF_8));
              BufferedReader br = new BufferedReader(ir);
              PEMParser pr = new PEMParser(br) ) {

            return scanPEM(converter, pr);
        }
        catch (
            IOException |
            InvalidKeySpecException |
            NoSuchAlgorithmException |
            OperatorCreationException |
            NoSuchProviderException e ) {
            log.debug("Failed to parse as PEM", e); //$NON-NLS-1$
        }
        return false;
    }


    /**
     * @param converter
     * @param pr
     * @return
     * @throws IOException
     * @throws PEMException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws OperatorCreationException
     * @throws NoSuchProviderException
     */
    private boolean scanPEM ( JcaPEMKeyConverter converter, PEMParser pr )
            throws IOException, PEMException, InvalidKeySpecException, NoSuchAlgorithmException, OperatorCreationException, NoSuchProviderException {
        Object obj;
        int num = 0;
        Set<X509CertificateHolder> certs = new HashSet<>();
        while ( ( obj = pr.readObject() ) != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Found PEM object of type " + obj.getClass().getName()); //$NON-NLS-1$
            }

            if ( obj instanceof PEMEncryptedKeyPair ) {
                if ( handlePEMKeyPairEncrypted(converter, obj) ) {
                    num++;
                    return true;
                }
            }
            else if ( obj instanceof PEMKeyPair ) {
                KeyPair kp = handlePEMKeyPair(converter, obj);
                if ( kp != null ) {
                    this.setValue(kp);
                    num++;
                }
            }
            else if ( obj instanceof PrivateKeyInfo ) {
                PrivateKeyInfo keyInfo = (PrivateKeyInfo) obj;
                KeyPair kp = handlePrivateKeyInfo(converter, keyInfo);
                if ( kp != null ) {
                    this.setValue(kp);
                    num++;
                }
            }
            else if ( obj instanceof PKCS8EncryptedPrivateKeyInfo ) {
                if ( handlePEMEncryptedPrivateKeyInfo(converter, obj) ) {
                    num++;
                    return true;
                }
            }
            else if ( obj instanceof X509CertificateHolder ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Found certificate " + obj); //$NON-NLS-1$
                }
                certs.add((X509CertificateHolder) obj);
            }
            else if ( log.isDebugEnabled() ) {
                log.debug("Unhandled PEM object of type " + obj.getClass().getName()); //$NON-NLS-1$
            }

        }

        if ( num == 0 ) {
            addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, BaseMessages.get("crypto.keyPEMNoPrivateKey"), StringUtils.EMPTY)); //$NON-NLS-1$
            return true;
        }

        if ( num == 1 ) {
            buildCertificateChain((KeyPair) this.getValue(), certs);
            return true;
        }
        return false;
    }


    /**
     * @param value
     * @param certs
     */
    private void buildCertificateChain ( KeyPair value, Set<X509CertificateHolder> certs ) {
        if ( value == null || certs.isEmpty() ) {
            setFoundCertificateChain(new Certificate[0]);
            return;
        }

        Set<X509Certificate> pool = new HashSet<>();
        X509Certificate ee = null;
        for ( X509CertificateHolder cert : certs ) {
            try {
                X509Certificate c = (X509Certificate) CertificateFactory.getInstance("X509") //$NON-NLS-1$
                        .generateCertificate(new ByteArrayInputStream(cert.getEncoded()));
                pool.add(c);
                if ( Arrays.equals(c.getPublicKey().getEncoded(), value.getPublic().getEncoded()) ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Found end entity certificate " + c); //$NON-NLS-1$
                    }
                    ee = c;
                }
            }
            catch (
                IOException |
                CertificateException e ) {
                log.debug("Failed to get public key data", e); //$NON-NLS-1$
            }
        }

        if ( ee == null ) {
            return;
        }

        buildCertificateChainFromPool(ee, pool);
    }


    /**
     * @param ee
     * @param pool
     */
    private void buildCertificateChainFromPool ( X509Certificate ee, Set<X509Certificate> pool ) {
        Set<X500Principal> used = new HashSet<>();
        List<X509Certificate> chain = new ArrayList<>();
        chain.add(ee);
        X500Principal issuer = ee.getIssuerX500Principal();
        used.add(issuer);
        X509Certificate c;
        while ( ( c = findIssuerCert(issuer, pool) ) != null ) {
            issuer = c.getIssuerX500Principal();
            if ( used.contains(issuer) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Already used, break with chain " + chain); //$NON-NLS-1$
                }
                setFoundCertificateChain(chain.toArray(new Certificate[chain.size()]));
                return;
            }
            chain.add(c);
            used.add(issuer);
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Chain is " + chain); //$NON-NLS-1$
        }
        setFoundCertificateChain(chain.toArray(new Certificate[chain.size()]));
    }


    /**
     * @param issuer
     * @param pool
     * @return
     */
    private static X509Certificate findIssuerCert ( X500Principal issuer, Set<X509Certificate> pool ) {
        for ( X509Certificate c : pool ) {
            if ( c.getSubjectX500Principal().equals(issuer) ) {
                return c;
            }
        }
        return null;
    }


    /**
     * @param converter
     * @param obj
     * @return
     * @throws OperatorCreationException
     * @throws PEMException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    private boolean handlePEMEncryptedPrivateKeyInfo ( JcaPEMKeyConverter converter, Object obj )
            throws OperatorCreationException, PEMException, InvalidKeySpecException, NoSuchAlgorithmException {
        PKCS8EncryptedPrivateKeyInfo keyInfo = (PKCS8EncryptedPrivateKeyInfo) obj;
        if ( this.getPassword() == null ) {
            this.setPasswordRequired(true);
            return true;
        }

        InputDecryptorProvider decProv = new JceOpenSSLPKCS8DecryptorProviderBuilder().build(this.getPassword().toCharArray());

        try {
            PrivateKeyInfo decryptPrivateKeyInfo = keyInfo.decryptPrivateKeyInfo(decProv);
            KeyPair kp = handlePrivateKeyInfo(converter, decryptPrivateKeyInfo);

            if ( kp != null ) {
                this.setValue(kp);
                return true;
            }
        }
        catch (
            PKCSException |
            NoSuchProviderException e ) {
            log.debug("Failed to decrypt private key info", e); //$NON-NLS-1$
            addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, BaseMessages.get("crypto.keyPasswordWrong"), StringUtils.EMPTY)); //$NON-NLS-1$
            return true;
        }

        return false;
    }


    /**
     * @param converter
     * @param obj
     * @return
     * @throws IOException
     * @throws PEMException
     */
    private boolean handlePEMKeyPairEncrypted ( JcaPEMKeyConverter converter, Object obj ) throws IOException, PEMException {
        if ( this.getPassword() == null ) {
            this.setPasswordRequired(true);
            return true;
        }

        PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(this.getPassword().toCharArray());
        PEMKeyPair decryptKeyPair = ( (PEMEncryptedKeyPair) obj ).decryptKeyPair(decProv);
        KeyPair kp = handlePEMKeyPair(converter, decryptKeyPair);
        if ( kp != null ) {
            this.setValue(kp);
            return true;
        }

        return false;
    }


    /**
     * @param converter
     * @param obj
     * @return
     * @throws PEMException
     */
    private static KeyPair handlePEMKeyPair ( JcaPEMKeyConverter converter, Object obj ) throws PEMException {
        KeyPair kp;
        KeyPair keyPair = converter.getKeyPair((PEMKeyPair) obj);

        if ( ! ( keyPair.getPrivate() instanceof RSAPrivateKey ) ) {
            return null;
        }
        kp = keyPair;
        return kp;
    }


    /**
     * @param converter
     * @param keyInfo
     * @return
     * @throws PEMException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    private static KeyPair handlePrivateKeyInfo ( JcaPEMKeyConverter converter, PrivateKeyInfo keyInfo )
            throws PEMException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
        KeyPair kp;
        PrivateKey privateKey = converter.getPrivateKey(keyInfo);
        if ( ! ( privateKey instanceof RSAPrivateCrtKey ) ) {
            return null;
        }
        RSAPrivateCrtKey privKey = (RSAPrivateCrtKey) privateKey;

        RSAPublicKeySpec pubSpec = new RSAPublicKeySpec(privKey.getModulus(), privKey.getPublicExponent());
        PublicKey pub = KeyFactory.getInstance("RSA", SUN).generatePublic(pubSpec); //$NON-NLS-1$
        kp = new KeyPair(pub, privKey);
        return kp;
    }


    /**
     * @param facesMessage
     */
    private void addMessage ( FacesMessage facesMessage ) {
        FacesContext.getCurrentInstance().addMessage(this.getClientId(FacesContext.getCurrentInstance()), facesMessage);
    }


    /**
     * @param contents
     */
    private boolean parsePKCS12 ( byte[] contents ) {
        try {
            KeyStore p12ks = KeyStore.getInstance(PKCS12);

            String password = this.getPassword();

            if ( log.isDebugEnabled() ) {
                log.debug("Binary length is " + contents.length); //$NON-NLS-1$
            }

            if ( password == null ) {
                try {
                    p12ks.load(new ByteArrayInputStream(contents), StringUtils.EMPTY.toCharArray());
                }
                catch ( IOException e ) {
                    log.debug("Parsing without password failed", e); //$NON-NLS-1$
                    setPasswordRequired(true);
                    return true;
                }
            }
            else {
                p12ks.load(new ByteArrayInputStream(contents), password.toCharArray());
            }

            String selectedAlias = this.getSelectedAlias();
            Set<String> keyAliases = new HashSet<>();

            if ( selectedAlias != null ) {
                return parsePKCS12WithAlias(p12ks, password, selectedAlias, keyAliases);
            }

            return scanPKCS12(p12ks, password, keyAliases);
        }
        catch (
            KeyStoreException |
            IOException |
            NoSuchAlgorithmException |
            CertificateException e ) {
            addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, BaseMessages.get("crypto.keyPKCS12CannotLoad"), e.getMessage())); //$NON-NLS-1$
            log.debug("Failed to load PKCS12", e); //$NON-NLS-1$
        }
        return false;
    }


    /**
     * @param p12ks
     * @param password
     * @param keyAliases
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     */
    private boolean scanPKCS12 ( KeyStore p12ks, String password, Set<String> keyAliases ) throws KeyStoreException, NoSuchAlgorithmException {
        Enumeration<String> aliases = p12ks.aliases();
        Map<String, KeyPair> keyPairs = new HashMap<>();

        while ( aliases.hasMoreElements() ) {
            String alias = aliases.nextElement();
            keyPairs.put(alias, handleP12Entry(p12ks, alias, keyAliases, password));
        }

        if ( keyAliases.isEmpty() ) {
            addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, BaseMessages.get("crypto.keyPKCS12NoPrivateKey"), StringUtils.EMPTY)); //$NON-NLS-1$
        }

        if ( keyAliases.size() > 1 ) {
            this.setSelectionRequired(true);
            this.setSelectionOptions(keyAliases.toArray(new String[] {}));
        }
        else {
            KeyPair kp = keyPairs.get(keyAliases.iterator().next());
            if ( kp != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("recovered single key pair " + kp); //$NON-NLS-1$
                }
                this.setValue(kp);
            }
        }

        return true;
    }


    /**
     * @param p12ks
     * @param password
     * @param selectedAlias
     * @param keyAliases
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     */
    private boolean parsePKCS12WithAlias ( KeyStore p12ks, String password, String selectedAlias, Set<String> keyAliases )
            throws KeyStoreException, NoSuchAlgorithmException {
        KeyPair kp = handleP12Entry(p12ks, selectedAlias, keyAliases, password);
        if ( kp != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("recovered key pair from selected alias" + kp); //$NON-NLS-1$
            }
            this.setValue(kp);
        }
        return true;
    }


    /**
     * @param p12ks
     * @param aliases
     * @param keyAliases
     * @param password
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     */
    private KeyPair handleP12Entry ( KeyStore p12ks, String alias, Set<String> keyAliases, String password )
            throws KeyStoreException, NoSuchAlgorithmException {

        if ( p12ks.isKeyEntry(alias) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Found key " + alias); //$NON-NLS-1$
            }
            keyAliases.add(alias);
            try {
                return tryLoadKey(p12ks, alias, password);
            }
            catch ( UnrecoverableKeyException e ) {
                log.trace("Key needs password " + alias, e); //$NON-NLS-1$
                this.setPasswordRequired(true);

                if ( password != null ) {
                    addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, BaseMessages.get("crypto.keyPasswordWrong"), StringUtils.EMPTY)); //$NON-NLS-1$
                }

                if ( log.isDebugEnabled() ) {
                    log.debug("Key needs password " + alias); //$NON-NLS-1$
                }
            }

        }

        return null;
    }


    /**
     * @param p12ks
     * @param alias
     * @param password
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    private KeyPair tryLoadKey ( KeyStore p12ks, String alias, String password )
            throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        Key key;
        if ( password == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Trying to load key without password " + alias); //$NON-NLS-1$
            }
            key = p12ks.getKey(alias, new char[] {});
        }
        else {
            if ( log.isDebugEnabled() ) {
                log.debug("Trying to load key with password " + alias); //$NON-NLS-1$
            }
            key = p12ks.getKey(alias, password.toCharArray());
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Recovered key " + alias); //$NON-NLS-1$
        }
        return getKeyPair(p12ks, alias, key);
    }


    /**
     * @param p12ks
     * @param alias
     * @param key
     */
    private KeyPair getKeyPair ( KeyStore p12ks, String alias, Key key ) {
        if ( ! ( key instanceof RSAPrivateKey ) ) {
            log.warn("Not an RSA key " + alias); //$NON-NLS-1$
            addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, BaseMessages.get("crypto.keyNoRSA"), StringUtils.EMPTY)); //$NON-NLS-1$
            return null;
        }

        try {
            Certificate certificate = p12ks.getCertificate(alias);
            if ( ! ( certificate instanceof X509Certificate ) || ! ( certificate.getPublicKey() instanceof RSAPublicKey ) ) {
                log.warn("No RSA public key for private key " + alias); //$NON-NLS-1$
                addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, BaseMessages.get("crypto.keyNoPubAvail"), StringUtils.EMPTY)); //$NON-NLS-1$
                return null;
            }

            Certificate[] chain = p12ks.getCertificateChain(alias);

            if ( chain != null && chain.length > 0 ) {
                this.setFoundCertificateChain(chain);
            }

            RSAPrivateKey privKey = (RSAPrivateKey) key;
            RSAPublicKey pubKey = (RSAPublicKey) certificate.getPublicKey();

            if ( privKey.getModulus().compareTo(pubKey.getModulus()) != 0 ) {
                log.warn("Public and private modulus do not match"); //$NON-NLS-1$
                if ( log.isDebugEnabled() ) {
                    log.debug("Public " + pubKey.getModulus()); //$NON-NLS-1$
                    log.debug("Private " + privKey.getModulus()); //$NON-NLS-1$
                }
                addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, BaseMessages.get("crypto.keyPubPrivMismatch"), StringUtils.EMPTY)); //$NON-NLS-1$
                return null;
            }

            return new KeyPair(pubKey, privKey);
        }
        catch ( GeneralSecurityException e ) {
            log.warn("Failed to get certificate for private key " + alias, e); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * @param contents
     * @param contentType
     * @param fileName
     * @return
     */
    protected boolean looksLikePKCS12 ( byte[] contents, String contentType, String fileName ) {

        if ( PKCS12_MIME_TYPE.equalsIgnoreCase(contentType) ) {
            return true;
        }

        if ( fileName != null && PKCS12_EXTENSIONS.contains(fileName.substring(fileName.lastIndexOf('.') + 1)) ) {
            return true;
        }

        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#processValidators(javax.faces.context.FacesContext)
     */
    @Override
    public void processValidators ( FacesContext context ) {
        this.pushComponentToEL(context, this);
        try {
            super.processValidators(context);
        }
        finally {
            this.popComponentFromEL(context);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#processUpdates(javax.faces.context.FacesContext)
     */
    @Override
    public void processUpdates ( FacesContext context ) {
        this.pushComponentToEL(context, this);
        try {
            super.processUpdates(context);
        }
        finally {
            this.popComponentFromEL(context);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#updateModel(javax.faces.context.FacesContext)
     */
    @Override
    public void updateModel ( FacesContext ctx ) {
        super.updateModel(ctx);

        if ( this.isRendered() && this.isValid() ) {
            Certificate[] chain = getFoundCertificateChain();
            if ( chain != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Have certificate chain of length " + chain.length); //$NON-NLS-1$
                }

                if ( chain.length == 0 ) {
                    chain = null;
                }

                MethodExpression m = this.getChainHandler();
                if ( m != null ) {
                    m.invoke(ctx.getELContext(), new Object[] {
                        chain
                    });
                }
            }

        }
    }


    /**
     * 
     * @param ev
     */
    public void useTextInput ( ActionEvent ev ) {
        String textInput = this.getTextInput();
        if ( textInput != null ) {
            this.setPassword(null);
            this.setSelectedAlias(null);
            this.setFoundCertificateChain(null);
            this.setValue(null);
            this.parseKeyFormat(textInput.getBytes(Charset.forName(UTF_8)), null, null);
        }
        else {
            this.getStateHelper().put(CHOOSE_OTHER, Boolean.FALSE);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#getFamily()
     */
    @Override
    public String getFamily () {
        return UINamingContainer.COMPONENT_FAMILY;
    }

}
