/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.04.2015 by mbechler
 */
package eu.agno3.runtime.jsf.components.crypto;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import eu.agno3.runtime.jsf.i18n.BaseMessages;


/**
 * @author mbechler
 *
 */
public class CertificateChainEditor extends UIInput implements NamingContainer {

    private static final Logger log = Logger.getLogger(CertificateChainEditor.class);
    private static final String TEXT_INPUT = "textInput"; //$NON-NLS-1$
    private static final String CHOOSE_OTHER = "chooseOther"; //$NON-NLS-1$
    private static final String OPEN_INITIAL = "openInitial"; //$NON-NLS-1$
    private static final String MODIFIED = "modified"; //$NON-NLS-1$


    /**
     * 
     * @return whether the certificate input should be open initially
     */
    public boolean getOpenInitial () {
        return (Boolean) this.getStateHelper().eval(OPEN_INITIAL, false);
    }


    /**
     * 
     * @param initial
     *            whether the certificate input should be open initially
     */
    public void setOpenInitial ( boolean initial ) {
        this.getStateHelper().put(OPEN_INITIAL, initial);
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
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#getFamily()
     */
    @Override
    public String getFamily () {
        return UINamingContainer.COMPONENT_FAMILY;
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
        return this.getOpenInitial() || this.getValue() == null || ( (Collection<?>) getValue() ).isEmpty()
                || (Boolean) this.getStateHelper().eval(CHOOSE_OTHER, false);
    }


    /**
     * 
     * @param ev
     */
    public void chooseOther ( ActionEvent ev ) {
        this.getStateHelper().put(CHOOSE_OTHER, Boolean.TRUE);
    }


    private PublicKey getPublicKeyInternal () {
        return (PublicKey) this.getAttributes().get("publicKey"); //$NON-NLS-1$
    }


    @SuppressWarnings ( "unchecked" )
    private List<X509Certificate> getCertificateChainInternal () {
        List<X509Certificate> val = (List<X509Certificate>) getValue();
        if ( val == null ) {
            val = new ArrayList<>();
            setValue(val);
        }
        return val;
    }


    /**
     * 
     * @return whether a chain is set
     */
    public boolean haveChain () {
        return getValue() != null;
    }


    /**
     * 
     * @param certIndex
     * @return null
     */
    public String truncate ( int certIndex ) {
        log.debug("Truncating chain"); //$NON-NLS-1$
        List<X509Certificate> certificateChainInternal = getCertificateChainInternal();
        if ( certIndex == 0 ) {
            certificateChainInternal.clear();
        }
        else {
            for ( int i = certIndex; i < certificateChainInternal.size(); i++ ) {
                certificateChainInternal.remove(i);
            }
        }
        setValue(certificateChainInternal);
        markModified();
        return null;
    }


    /**
     * 
     * @param ev
     * @throws IOException
     * @throws CertificateEncodingException
     */
    public void handleFileUpload ( FileUploadEvent ev ) throws IOException, CertificateEncodingException {
        if ( log.isDebugEnabled() ) {
            log.debug("Got file upload " + ev); //$NON-NLS-1$
        }
        UploadedFile f = ev.getFile();

        if ( f != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Content type: " + f.getContentType()); //$NON-NLS-1$
            }

            Set<X509Certificate> certs = new HashSet<>(parseCertificates(f.getInputstream()));

            if ( certs.isEmpty() ) {
                return;
            }

            updateChain(certs);
        }
    }


    /**
     * 
     * @param ev
     */
    public void useTextInput ( ActionEvent ev ) {
        String textInput = this.getTextInput();
        if ( textInput != null ) {
            Collection<X509Certificate> parsed = parseCertificates(new ByteArrayInputStream(textInput.getBytes(Charset.forName("UTF-8")))); //$NON-NLS-1$
            Set<X509Certificate> certs = new HashSet<>(parsed);

            if ( certs.isEmpty() ) {
                return;
            }

            updateChain(certs);

            setTextInput(StringUtils.EMPTY);
        }
        else {
            this.getStateHelper().put(CHOOSE_OTHER, Boolean.FALSE);
        }
    }


    /**
     * @param certs
     * @throws CertificateEncodingException
     */
    private void updateChain ( Set<X509Certificate> certs ) {
        List<X509Certificate> currentChain = new ArrayList<>(this.getCertificateChainInternal());
        log.debug("Certs are"); //$NON-NLS-1$
        for ( X509Certificate cert : certs ) {
            log.debug(CertificateUtil.formatPrincipalName(cert.getSubjectX500Principal()));
        }
        boolean singleCert = certs.size() == 1;
        PublicKey publicKey = this.getPublicKeyInternal();
        X509Certificate foundEE = tryFindEE(certs, currentChain, publicKey);
        certs.addAll(currentChain);
        boolean addedAny = false;
        try {
            addedAny |= updateEE(certs, currentChain, foundEE);
            addedAny |= buildChain(certs, currentChain, singleCert);
        }
        catch ( CertificateEncodingException e ) {
            log.debug("Certificate parsing failed", e); //$NON-NLS-1$
        }
        log.debug("Chain is"); //$NON-NLS-1$
        for ( X509Certificate cert : currentChain ) {
            log.debug(CertificateUtil.formatPrincipalName(cert.getSubjectX500Principal()));
        }

        if ( !addedAny && singleCert ) {
            FacesContext.getCurrentInstance().addMessage(
                this.getClientId(),
                new FacesMessage(FacesMessage.SEVERITY_WARN, BaseMessages.get("chainEditor.noCertUsableSingle"), StringUtils.EMPTY)); //$NON-NLS-1$
        }
        else if ( !addedAny ) {
            FacesContext.getCurrentInstance().addMessage(
                this.getClientId(),
                new FacesMessage(FacesMessage.SEVERITY_WARN, BaseMessages.get("chainEditor.noCertUsableMulti"), StringUtils.EMPTY)); //$NON-NLS-1$
        }

        this.setValue(currentChain);
        markModified();
    }


    /**
     * @param certs
     * @param currentChain
     * @param includeSelfSigned
     * @throws CertificateEncodingException
     */
    private static boolean buildChain ( Set<X509Certificate> certs, List<X509Certificate> currentChain, boolean includeSelfSigned )
            throws CertificateEncodingException {
        if ( currentChain.isEmpty() ) {
            log.debug("Chain is empty"); //$NON-NLS-1$
            return false;
        }
        X509Certificate at = currentChain.get(currentChain.size() - 1);
        boolean addedAny = false;
        while ( true ) {
            X509Certificate foundCert = null;
            for ( X509Certificate cert : certs ) {
                if ( chains(at, cert) && ( includeSelfSigned || !isSelfSigned(cert) ) ) {
                    foundCert = cert;
                }
            }

            if ( foundCert == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Did not find a chaining certificate for issuer " + CertificateUtil.formatPrincipalName(at.getIssuerX500Principal())); //$NON-NLS-1$
                }
                break;
            }
            addedAny = true;
            certs.remove(foundCert);
            currentChain.add(foundCert);
            at = foundCert;
        }

        return addedAny;
    }


    /**
     * @param certs
     * @param currentChain
     * @param foundEE
     * @throws CertificateEncodingException
     */
    private static boolean updateEE ( Set<X509Certificate> certs, List<X509Certificate> currentChain, X509Certificate foundEE )
            throws CertificateEncodingException {
        if ( foundEE != null ) {
            certs.remove(foundEE);
            if ( currentChain.size() > 1 ) {
                log.debug("Current chain contains intermediates"); //$NON-NLS-1$
                if ( chains(foundEE, currentChain.get(1)) ) {
                    currentChain.set(0, foundEE);
                }
                else {
                    log.debug("Found a EE cert but it does not chain with the current intermediates"); //$NON-NLS-1$
                    currentChain.clear();
                    currentChain.add(foundEE);
                }
            }
            else {
                currentChain.clear();
                currentChain.add(foundEE);
            }
            return true;
        }

        return false;
    }


    /**
     * @param certs
     * @param currentChain
     * @param publicKey
     * @return
     */
    private static X509Certificate tryFindEE ( Set<X509Certificate> certs, List<X509Certificate> currentChain, PublicKey publicKey ) {
        X509Certificate foundEE = null;
        for ( X509Certificate cert : certs ) {
            if ( publicKey != null && pubkeysEqual(publicKey, cert.getPublicKey()) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Found public key match for " + CertificateUtil.formatPrincipalName(cert.getSubjectX500Principal())); //$NON-NLS-1$
                }
                foundEE = cert;
                break;
            }
            else if ( publicKey == null && cert.getBasicConstraints() < 0 ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Found non CA certificate " + CertificateUtil.formatPrincipalName(cert.getSubjectX500Principal())); //$NON-NLS-1$
                }
                foundEE = cert;
            }
            else if ( publicKey == null && currentChain.isEmpty() && certs.size() == 1 ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Found a single CA certificate " + CertificateUtil.formatPrincipalName(cert.getSubjectX500Principal())); //$NON-NLS-1$
                }
                foundEE = cert;
            }
        }
        return foundEE;
    }


    /**
     * @param cert
     * @return whether the certificate is self signed
     */
    public static boolean isSelfSigned ( Object cert ) {
        if ( ! ( cert instanceof X509Certificate ) ) {
            return false;
        }
        X509Certificate c = (X509Certificate) cert;
        return c.getIssuerX500Principal().equals(c.getSubjectX500Principal());
    }


    /**
     * @param cert
     * @return whether this is a CA root certificate
     */
    public static boolean isRoot ( Object cert ) {
        if ( ! ( cert instanceof X509Certificate ) ) {
            return false;
        }
        X509Certificate c = (X509Certificate) cert;
        return c.getIssuerX500Principal().equals(c.getSubjectX500Principal()) && c.getBasicConstraints() > 0;
    }


    /**
     * @param foundEE
     * @param certificate
     * @return
     * @throws CertificateEncodingException
     */
    private static boolean chains ( X509Certificate cur, X509Certificate next ) throws CertificateEncodingException {
        if ( !checkChainsBasic(cur, next) ) {
            return false;
        }

        JcaX509CertificateHolder curHolder = new JcaX509CertificateHolder(cur);
        JcaX509CertificateHolder nextHolder = new JcaX509CertificateHolder(next);
        return checkChainsAuthorityInformation(next, curHolder, nextHolder);
    }


    /**
     * @param next
     * @param curHolder
     * @param nextHolder
     * @return
     */
    protected static boolean checkChainsAuthorityInformation ( X509Certificate next, JcaX509CertificateHolder curHolder,
            JcaX509CertificateHolder nextHolder ) {
        AuthorityKeyIdentifier curAki = AuthorityKeyIdentifier.fromExtensions(curHolder.getExtensions());
        SubjectKeyIdentifier nextSki = SubjectKeyIdentifier.fromExtensions(nextHolder.getExtensions());

        if ( curAki == null || nextSki == null ) {
            log.debug("AKI/SKI chaining not available, ignore"); //$NON-NLS-1$
            return true;
        }

        if ( curAki.getAuthorityCertSerialNumber() != null && next.getSerialNumber().compareTo(curAki.getAuthorityCertSerialNumber()) == 0 ) {
            log.debug("AuthorityCertSerialNumber does not match"); //$NON-NLS-1$
            return false;
        }

        byte[] nextKeyId = nextSki.getKeyIdentifier();
        if ( curAki.getKeyIdentifier() != null && !Arrays.equals(curAki.getKeyIdentifier(), nextKeyId) ) {
            log.debug("AuthorityKeyIdentifier does not match"); //$NON-NLS-1$
            return false;
        }

        return true;
    }


    /**
     * @param cur
     * @param next
     */
    protected static boolean checkChainsBasic ( X509Certificate cur, X509Certificate next ) {
        if ( next.getBasicConstraints() < 0 ) {
            log.debug("Not a CA"); //$NON-NLS-1$
            return false;
        }

        if ( cur.getSubjectX500Principal().equals(next.getSubjectX500Principal()) ) {
            log.debug("Duplicate"); //$NON-NLS-1$
            return false;
        }

        if ( !cur.getIssuerX500Principal().equals(next.getSubjectX500Principal()) ) {
            log.debug("Issuer subject mismatch"); //$NON-NLS-1$
            return false;
        }

        return true;
    }


    /**
     * @param publicKeyInternal
     * @param publicKey
     * @return
     */
    private static boolean pubkeysEqual ( PublicKey a, PublicKey b ) {
        if ( a instanceof RSAPublicKey && b instanceof RSAPublicKey ) {
            RSAPublicKey rsaa = (RSAPublicKey) a;
            RSAPublicKey rsab = (RSAPublicKey) b;
            return rsaa.getModulus().compareTo(rsab.getModulus()) == 0 && rsaa.getPublicExponent().compareTo(rsab.getPublicExponent()) == 0;
        }
        else if ( a != null && b != null ) {
            return Arrays.equals(a.getEncoded(), b.getEncoded());
        }
        return false;
    }


    /**
     * @return
     * 
     */
    @SuppressWarnings ( "unchecked" )
    protected Collection<X509Certificate> parseCertificates ( InputStream is ) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X509"); //$NON-NLS-1$
            return (Collection<X509Certificate>) cf.generateCertificates(is);
        }
        catch ( CertificateException e ) {
            log.debug("Failed to parse certificate", e); //$NON-NLS-1$
            FacesContext.getCurrentInstance().addMessage(
                this.getClientId(FacesContext.getCurrentInstance()),
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Not a valid certificate", StringUtils.EMPTY)); //$NON-NLS-1$
        }
        return Collections.EMPTY_LIST;

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

}
