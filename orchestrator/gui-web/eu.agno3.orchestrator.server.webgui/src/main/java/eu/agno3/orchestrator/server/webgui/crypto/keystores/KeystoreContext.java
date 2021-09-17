/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.04.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto.keystores;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.crypto.keystore.KeyInfo;
import eu.agno3.orchestrator.crypto.keystore.KeyStoreInfo;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.jobs.JobDetailContextBean;
import eu.agno3.runtime.crypto.keystore.KeyType;
import eu.agno3.runtime.jsf.components.crypto.CertificateUtil;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 *
 */
@Named ( "keystoreContext" )
@ViewScoped
public class KeystoreContext implements Serializable {

    private static final long serialVersionUID = 8769210760488602996L;
    private static final Logger log = Logger.getLogger(KeystoreContext.class);

    private static final int TYPE_CSR = 1;
    private static final int TYPE_SELFSIGNED = 2;

    private static final String VALIDATION_ERROR_PREFIX = "keystoremanager.chainValidateFail."; //$NON-NLS-1$
    private static final String VALIDATION_ERROR_SELFSIGNED = "keystoremanager.chainValidateFail.selfSigned"; //$NON-NLS-1$
    private static final String CHARSET = "UTF-8"; //$NON-NLS-1$

    private String keystore;
    private String keyAlias;
    private String keyAliasInitial;

    private KeyType keyType = KeyType.RSA2048;

    private KeyPair importKeyPair;

    @Inject
    private InstanceKeystoreManager ikm;

    @Inject
    private CertRequestDataContext certReq;

    @Inject
    private JobDetailContextBean keyGenerationJob;

    private int certificateType = 1;

    private List<Certificate> importChain = new ArrayList<>();

    private String renewalCSR;

    private boolean certValidationFailed;
    private boolean disableValidation;

    private String csr;

    private KeyInfo keyInfo;

    private PublicKey publicKey;

    private KeyStoreInfo keystoreInfo;
    private boolean keyGenComplete;
    private boolean keystoreInfoLoaded;

    private String suggestSubject;

    private String suggestSANs;
    private String suggestKeyUsage;
    private String suggestEKUs;
    private KeyInfo templateKeyInfo;
    private String templateKeyAlias;
    private boolean allowAnonymous;
    private X509Certificate cachedCertificate;


    /**
     * @return the suggestSubject
     */
    public String getSuggestSubject () {
        return this.suggestSubject;
    }


    /**
     * @param suggestSubject
     *            the suggestSubject to set
     */
    public void setSuggestSubject ( String suggestSubject ) {
        try {
            this.suggestSubject = URLDecoder.decode(suggestSubject, CHARSET);
        }
        catch ( UnsupportedEncodingException e ) {
            ExceptionHandler.handle(e);
        }
    }


    /**
     * @return the suggestSANs
     */
    public String getSuggestSANs () {
        return this.suggestSANs;
    }


    /**
     * @param suggestSANs
     *            the suggestSANs to set
     */
    public void setSuggestSANs ( String suggestSANs ) {
        try {
            this.suggestSANs = URLDecoder.decode(suggestSANs, CHARSET);
        }
        catch ( UnsupportedEncodingException e ) {
            ExceptionHandler.handle(e);
        }
    }


    /**
     * @return the suggestKeyUsage
     */
    public String getSuggestKeyUsage () {
        return this.suggestKeyUsage;
    }


    /**
     * @param suggestKeyUsage
     *            the suggestKeyUsage to set
     */
    public void setSuggestKeyUsage ( String suggestKeyUsage ) {
        try {
            this.suggestKeyUsage = URLDecoder.decode(suggestKeyUsage, CHARSET);
        }
        catch ( UnsupportedEncodingException e ) {
            ExceptionHandler.handle(e);
        }
    }


    /**
     * @return the suggestEKUs
     */
    public String getSuggestEKUs () {
        return this.suggestEKUs;
    }


    /**
     * @param suggestEKUs
     *            the suggestEKUs to set
     */
    public void setSuggestEKUs ( String suggestEKUs ) {
        try {
            this.suggestEKUs = URLDecoder.decode(suggestEKUs, CHARSET);
        }
        catch ( UnsupportedEncodingException e ) {
            ExceptionHandler.handle(e);
        }
    }


    /**
     * @return the keystore
     */
    public String getKeystore () {
        return this.keystore;
    }


    /**
     * @param keystore
     *            the keystore to set
     */
    public void setKeystore ( String keystore ) {
        this.keystore = keystore;
    }


    /**
     * @return the keyAlias
     */
    public String getKeyAlias () {
        if ( this.keyAlias == null ) {
            return this.getKeyAliasInitial();
        }
        return this.keyAlias;
    }


    /**
     * @param keyAlias
     *            the keyAlias to set
     */
    public void setKeyAlias ( String keyAlias ) {
        this.keyAlias = keyAlias;
    }


    public String getDisplayKeyAlias () {
        KeyInfo ki = getKeyInfo();
        if ( ki == null ) {
            return getKeyAlias();
        }
        return KeyInfoUtil.getDisplayKeyAlias(ki);
    }


    public boolean getGeneratedAlias () {
        String ka = getKeyAlias();
        if ( StringUtils.isBlank(ka) ) {
            return false;
        }
        return ka.charAt(0) == '_';
    }


    /**
     * @return template key alias
     */
    public String getTemplateKeyAlias () {
        if ( StringUtils.isBlank(this.templateKeyAlias) ) {
            return getKeyAlias();
        }
        return this.templateKeyAlias;
    }


    /**
     * @param templateKeyAlias
     *            the templateKeyAlias to set
     */
    public void setTemplateKeyAlias ( String templateKeyAlias ) {
        this.templateKeyAlias = templateKeyAlias;
    }


    /**
     * @return the keyAliasInitial
     */
    public String getKeyAliasInitial () {
        return this.keyAliasInitial;
    }


    /**
     * @param keyAliasInitial
     *            the keyAliasInitial to set
     */
    public void setKeyAliasInitial ( String keyAliasInitial ) {
        this.keyAliasInitial = keyAliasInitial;
    }


    public boolean getAllowAnonymous () {
        return this.allowAnonymous;
    }


    /**
     * @param allowAnonymous
     *            the allowAnonymous to set
     */
    public void setAllowAnonymous ( boolean allowAnonymous ) {
        this.allowAnonymous = allowAnonymous;
    }


    /**
     * @return the certReq
     */
    public CertRequestDataContext getCertReq () {
        return this.certReq;
    }


    /**
     * @return the keyGenCertificateType
     */
    public int getCertificateType () {
        return this.certificateType;
    }


    /**
     * @param keyGenCertificateType
     *            the keyGenCertificateType to set
     */
    public void setCertificateType ( int keyGenCertificateType ) {
        this.certificateType = keyGenCertificateType;
    }


    /**
     * @return the keyInfo
     */
    public KeyInfo getKeyInfo () {
        if ( this.keyInfo == null ) {
            try {
                this.keyInfo = this.ikm.getKeyInfo(this.getKeystore(), this.getKeyAlias());
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
            }
        }

        return this.keyInfo;
    }


    public KeyInfo getTemplateKeyInfo () {
        if ( this.templateKeyInfo == null ) {
            try {
                String tpl = getTemplateKeyAlias();
                if ( StringUtils.isBlank(tpl) ) {
                    return null;
                }
                this.templateKeyInfo = this.ikm.getKeyInfo(this.getKeystore(), tpl);
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
            }
        }

        return this.templateKeyInfo;
    }


    public KeyStoreInfo getKeystoreInfo () {
        if ( !this.keystoreInfoLoaded ) {
            try {
                this.keystoreInfoLoaded = true;
                this.keystoreInfo = this.ikm.getKeystoreInfo(this.getKeystore());
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
            }
        }

        return this.keystoreInfo;
    }


    public PublicKey getPublicKey () {
        if ( this.publicKey == null ) {
            KeyInfo ki = getKeyInfo();
            if ( ki != null ) {
                this.publicKey = KeystoreManageContext.decodePublicKey(ki);
            }
        }

        return this.publicKey;
    }


    public String getPublicKeyFingerprint () {
        PublicKey pubkey = getPublicKey();
        if ( pubkey == null ) {
            return StringUtils.EMPTY;
        }
        return CertificateUtil.formatPubkeyFingerprintSHA256(pubkey);
    }


    public X509Certificate getCertificate () {
        if ( this.cachedCertificate != null ) {
            return this.cachedCertificate;
        }
        List<Certificate> certs = KeystoreManageContext.decodeCertChain(this.getKeyInfo());
        if ( certs.isEmpty() ) {
            return null;
        }
        this.cachedCertificate = (X509Certificate) certs.get(0);
        return this.cachedCertificate;
    }


    public void initCertificateRequest () {
        KeyInfo ki = getTemplateKeyInfo();
        if ( ki == null || ki.getCertificateChain() == null || ki.getCertificateChain().isEmpty() ) {
            return;
        }

        X509Certificate cert = (X509Certificate) KeystoreManageContext.decodeCertChain(ki).get(0);

        if ( StringUtils.isBlank(this.certReq.getSubject()) ) {
            this.certReq.setSubject(cert.getSubjectX500Principal().getName());
        }

        if ( this.certReq.getSans() == null || this.certReq.getSans().isEmpty() ) {
            try {
                this.certReq.setSans(CertRequestDataContext.sansToString(cert));
            }
            catch ( CertificateParsingException e ) {
                ExceptionHandler.handle(e);
            }
        }

        if ( this.certReq.getKeyUsages() == null || this.certReq.getKeyUsages().isEmpty() ) {
            this.certReq.setKeyUsages(CertRequestDataContext.keyUsageToString(cert));
        }

        if ( this.certReq.getEkus() == null || this.certReq.getEkus().isEmpty() ) {
            try {
                this.certReq.setEkus(CertRequestDataContext.extKeyUsageToString(cert));
            }
            catch ( CertificateParsingException e ) {
                ExceptionHandler.handle(e);
            }
        }
    }


    /**
     * @return the keyType
     */
    public KeyType getKeyType () {
        return this.keyType;
    }


    /**
     * @param keyType
     *            the keyType to set
     */
    public void setKeyType ( KeyType keyType ) {
        this.keyType = keyType;
    }


    /**
     * @return the importKeyPair
     */
    public KeyPair getImportKeyPair () {
        return this.importKeyPair;
    }


    /**
     * @param importKeyPair
     *            the importKeyPair to set
     */
    public void setImportKeyPair ( KeyPair importKeyPair ) {
        this.importKeyPair = importKeyPair;
    }


    /**
     * @return the importChain
     */
    public List<Certificate> getImportChain () {
        return this.importChain;
    }


    public List<Certificate> getImportOrExistingChain () {
        if ( this.importChain == null ) {
            this.importChain = KeystoreManageContext.decodeCertChain(this.getKeyInfo());
        }
        return this.importChain;
    }


    public void setImportOrExistingChain ( List<Certificate> importChain ) {
        setImportChain(importChain);
    }


    /**
     * @param importChain
     *            the importChain to set
     */
    public void setImportChain ( List<Certificate> importChain ) {
        this.importChain = importChain;
    }


    /**
     * @return the certValidationFailed
     */
    public boolean getCertValidationFailed () {
        return this.certValidationFailed;
    }


    /**
     * @param certValidationFailed
     *            the certValidationFailed to set
     */
    public void setCertValidationFailed ( boolean certValidationFailed ) {
        this.certValidationFailed = certValidationFailed;
    }


    /**
     * @return the disableValidation
     */
    public boolean getDisableValidation () {
        return this.disableValidation;
    }


    /**
     * @param disableValidation
     *            the disableValidation to set
     */
    public void setDisableValidation ( boolean disableValidation ) {
        this.disableValidation = disableValidation;
    }


    public void handleChain ( Certificate[] chain ) {
        if ( chain == null ) {
            this.importChain = new ArrayList<>();
            return;
        }
        this.importChain = new ArrayList<>(Arrays.asList(chain));
    }


    public String getRenewalCSR () {
        if ( this.renewalCSR == null ) {
            try {
                this.renewalCSR = this.ikm.getRenewalCSR(this.getKeystore(), this.getKeyAlias(), this.certReq.getRequestPassword());
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
            }
        }
        return this.renewalCSR;
    }


    public StreamedContent getRenewalCSRDownload () throws UnsupportedEncodingException {
        return makeCSRDownload(getRenewalCSR());
    }


    /**
     * @param renewalCsr
     * @return
     * @throws UnsupportedEncodingException
     */
    private StreamedContent makeCSRDownload ( String renewalCsr ) throws UnsupportedEncodingException {
        if ( !StringUtils.isEmpty(renewalCsr) ) {
            return new DefaultStreamedContent(
                new ByteArrayInputStream(renewalCsr.getBytes("US-ASCII")), //$NON-NLS-1$
                "application/pkcs10", //$NON-NLS-1$
                String.format("%s-%s.req", this.getKeystore(), this.getKeyAlias())); //$NON-NLS-1$
        }

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
            FacesMessage.SEVERITY_ERROR,
            GuiMessages.get("keystoremanager.noCSR"), //$NON-NLS-1$
            StringUtils.EMPTY));
        return null;
    }


    /**
     * @return the csr
     */
    public String getCsr () {
        return this.csr;
    }


    public String clearCSR () {
        this.csr = null;
        return null;
    }


    public StreamedContent getCsrDownload () throws UnsupportedEncodingException {
        return makeCSRDownload(getCsr());
    }


    public String generateKey () {
        try {
            if ( StringUtils.isBlank(getKeyAlias()) && getAllowAnonymous() ) {
                setKeyAliasInitial('_' + UUID.randomUUID().toString());
            }
            else {
                checkNotExists();
            }
            JobInfo info = this.ikm.generateKey(this.getKeystore(), this.getKeyAlias(), this.getKeyType().name()); // $NON-NLS-1$
            this.keyGenerationJob.setJobId(info.getJobId());
            return null;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    /**
     * @throws ModelObjectConflictException
     */
    private void checkNotExists () throws ModelObjectConflictException {
        KeyStoreInfo ksInfo = this.getKeystoreInfo();
        if ( ksInfo == null ) {
            return;
        }
        for ( KeyInfo ki : ksInfo.getKeyEntries() ) {
            if ( this.getKeyAlias().equalsIgnoreCase(ki.getKeyAlias()) ) {
                throw new ModelObjectConflictException("key", this.getKeyAlias()); //$NON-NLS-1$
            }
        }
    }


    public synchronized void generatedKey ( ActionEvent ev ) {
        if ( this.keyGenComplete ) {
            return;
        }
        if ( log.isDebugEnabled() ) {
            log.debug("Generated key - mode " + this.certificateType); //$NON-NLS-1$
        }
        if ( this.certificateType == TYPE_CSR ) {
            this.generateCSR();
        }
        else if ( this.certificateType == TYPE_SELFSIGNED ) {
            this.generateSelfSigned();
        }
        this.keyGenComplete = true;
    }


    /**
     * @return the keyGenComplete
     */
    public boolean getKeyGenComplete () {
        return this.keyGenComplete;
    }


    /**
     * @return the keyGenerationJob
     */
    public JobDetailContextBean getKeyGenerationJob () {
        return this.keyGenerationJob;
    }


    public String generateCSR () {
        try {
            this.csr = this.ikm.getCSR(
                this.getKeystore(),
                this.getKeyAlias(),
                this.certReq.getSubject(),
                this.certReq.getExtensions(),
                this.certReq.getRequestPassword(),
                this.certReq.getLifetime());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    public String dialogClose () {
        if ( getKeyGenComplete() && getCertificateType() == TYPE_SELFSIGNED ) {
            return DialogContext.closeDialog(getKeyAlias());
        }
        return DialogContext.closeDialog(null);
    }


    public String generateSelfSigned () {
        try {
            generateSelfSignedInternal();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    public String generateSelfSignedClose () {
        try {
            if ( generateSelfSignedInternal() ) {
                DialogContext.closeDialog(getKeyAlias());
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    /**
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     * @throws ModelServiceException
     * @throws KeystoreManagerException
     * @throws GuiWebServiceException
     * @throws IOException
     * @throws ModelObjectValidationException
     */
    private boolean generateSelfSignedInternal () throws ModelObjectNotFoundException, AgentDetachedException, AgentCommunicationErrorException,
            ModelServiceException, KeystoreManagerException, GuiWebServiceException, IOException, ModelObjectValidationException {
        KeyStoreInfo info = getKeystoreInfo();
        if ( info == null ) {
            return false;
        }

        if ( !this.disableValidation && !StringUtils.isBlank(info.getValidationTrustStore()) ) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, GuiMessages.get(VALIDATION_ERROR_SELFSIGNED), StringUtils.EMPTY));
            this.certValidationFailed = true;
            return false;
        }

        this.ikm.makeSelfSigned(
            this.getKeystore(),
            this.getKeyAlias(),
            this.certReq.getSubject(),
            this.certReq.getRequestPassword(),
            this.certReq.getExtensions(),
            this.certReq.getLifetime());

        return true;
    }


    public String updateChain () {
        try {
            if ( !checkChain(this.importChain) ) {
                return null;
            }
            this.ikm.updateChain(this.getKeystore(), this.getKeyAlias(), this.importChain);
            return DialogContext.closeDialog(getKeyAlias());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    /**
     * @param chain
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     * @throws CertificateEncodingException
     * @throws ModelServiceException
     * @throws KeystoreManagerException
     * @throws GuiWebServiceException
     */
    private boolean checkChain ( List<Certificate> chain ) throws ModelObjectNotFoundException, AgentDetachedException,
            AgentCommunicationErrorException, CertificateEncodingException, ModelServiceException, KeystoreManagerException, GuiWebServiceException {
        if ( chain.isEmpty() ) {
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, GuiMessages.get(VALIDATION_ERROR_PREFIX + "EMPTY"), StringUtils.EMPTY)); //$NON-NLS-1$
            return false;
        }
        KeyStoreInfo info = getKeystoreInfo();
        if ( !this.disableValidation && info != null && !StringUtils.isBlank(info.getValidationTrustStore()) ) {
            String valError = this.ikm.validateChain(this.getKeystore(), chain);
            if ( !StringUtils.isBlank(valError) ) {
                FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, GuiMessages.get(VALIDATION_ERROR_PREFIX + valError), StringUtils.EMPTY));

                this.certValidationFailed = true;
                return false;
            }
        }

        return true;
    }


    public String importKey () {
        try {
            if ( StringUtils.isBlank(getKeyAlias()) && getAllowAnonymous() ) {
                setKeyAliasInitial('_' + UUID.randomUUID().toString());
            }
            else {
                checkNotExists();
            }
            List<Certificate> chain = this.importChain;

            if ( !chain.isEmpty() && !checkChain(chain) ) {
                return null;
            }

            if ( this.importKeyPair == null ) {
                return null;
            }

            this.ikm.importKey(
                this.getKeystore(),
                this.getKeyAlias(),
                KeyType.getKeyType(this.importKeyPair.getPublic()).name(),
                this.importKeyPair,
                chain);

            return DialogContext.closeDialog(getKeyAlias());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    public String deleteKey () {
        try {
            this.ikm.deleteKey(this.getKeystore(), this.getKeyAlias());
            return DialogContext.closeDialog(getKeystore() + ':' + getKeyAlias());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    public Set<String> getKeyUsages () throws UnsupportedEncodingException {
        if ( this.certReq.getKeyUsages() != null ) {
            return this.certReq.getKeyUsages();
        }
        else if ( !StringUtils.isBlank(this.suggestKeyUsage) ) {
            return new HashSet<>(Arrays.asList(StringUtils.split(URLDecoder.decode(this.suggestKeyUsage, CHARSET), ',')));
        }
        return new HashSet<>(
            Arrays.asList(
                String.valueOf(KeyUsage.dataEncipherment),
                String.valueOf(KeyUsage.digitalSignature),
                String.valueOf(KeyUsage.keyAgreement),
                String.valueOf(KeyUsage.keyEncipherment)));
    }


    public void setKeyUsages ( Set<String> kus ) {
        this.certReq.setKeyUsages(kus);
    }


    public Set<String> getEkus () throws UnsupportedEncodingException {
        if ( this.certReq.getEkus() != null ) {
            return this.certReq.getEkus();
        }
        else if ( !StringUtils.isBlank(this.suggestEKUs) ) {
            return new HashSet<>(Arrays.asList(StringUtils.split(URLDecoder.decode(this.suggestEKUs, CHARSET), ',')));
        }
        return new HashSet<>(Arrays.asList(KeyPurposeId.id_kp_clientAuth.getId(), KeyPurposeId.id_kp_serverAuth.getId()));
    }


    public void setEkus ( Set<String> ekus ) {
        this.certReq.setEkus(ekus);
    }


    public List<String> getSans () {
        if ( this.certReq.getSans() == null ) {
            if ( !StringUtils.isBlank(this.suggestSANs) ) {
                this.certReq.setSans(new ArrayList<>(Arrays.asList(StringUtils.split(this.suggestSANs, ','))));
            }
            else {
                this.certReq.setSans(new ArrayList<>());
            }
        }
        return this.certReq.getSans();
    }


    public void setSans ( List<String> sans ) {
        this.certReq.setSans(sans);
    }


    public String getSubject () throws UnsupportedEncodingException {
        if ( this.certReq.getSubject() != null ) {
            return this.certReq.getSubject();
        }
        if ( !StringUtils.isBlank(this.suggestSubject) ) {
            return URLDecoder.decode(this.suggestSubject, CHARSET);
        }

        return null;
    }


    public void setSubject ( String subject ) {
        this.certReq.setSubject(subject);
    }
}
