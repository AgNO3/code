/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.runtime.jsf.components.crypto;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Objects;

import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.pubkey.PublicKeyParser;
import eu.agno3.runtime.crypto.pubkey.PublicKeyParser.PublicKeyEntry;
import eu.agno3.runtime.crypto.tls.FingerprintUtil;
import eu.agno3.runtime.jsf.i18n.BaseMessages;


/**
 * @author mbechler
 *
 */
public class RSAPublicKeyEditor extends UIInput implements NamingContainer {

    private static final Logger log = Logger.getLogger(RSAPublicKeyEditor.class);

    private static final String TEXT_INPUT = "textInput"; //$NON-NLS-1$
    private static final String CHOOSE_OTHER = "chooseOther"; //$NON-NLS-1$
    private static final String MODIFIED = "modified"; //$NON-NLS-1$


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
        return this.getValue() == null || (Boolean) this.getStateHelper().eval(CHOOSE_OTHER, false);
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
     * 
     * @return key bit length
     */
    public int getBitLength () {
        Object value = getValue();
        if ( value instanceof RSAPublicKey ) {
            return ( (RSAPublicKey) value ).getModulus().bitLength();
        }
        return 0;
    }


    /**
     * @return key type
     */
    public String getKeyType () {
        Object value = getValue();
        if ( value instanceof RSAPublicKey ) {
            return BaseMessages.get("keyType.RSA"); //$NON-NLS-1$
        }
        return StringUtils.EMPTY;
    }


    /**
     * 
     * @return formatted key type
     */
    public String getKeyTypeFormatted () {
        if ( this.getValue() instanceof PublicKey ) {
            return BaseMessages.format("crypto.keyTypeFmt", getBitLength(), getKeyType()); //$NON-NLS-1$
        }
        return StringUtils.EMPTY;
    }


    /**
     * @return SHA-256 fingerprint of the key
     */
    public String getFingerprintSHA256 () {
        if ( this.getValue() instanceof PublicKey ) {
            try {
                return Base64.encodeBase64String(FingerprintUtil.sha256((PublicKey) getValue()));
            }
            catch ( CryptoException e1 ) {
                log.warn("Failed to produce public key fingerprint", e1); //$NON-NLS-1$
            }
        }
        return StringUtils.EMPTY;
    }


    /**
     * @return SHA-256 SSH style fingerprint of the key
     */
    public String getFingerprintSHA256SSH () {
        if ( this.getValue() instanceof PublicKey ) {
            try {
                return Base64.encodeBase64String(FingerprintUtil.sha256SSH((PublicKey) getValue()));
            }
            catch ( CryptoException e1 ) {
                log.warn("Failed to produce public key fingerprint", e1); //$NON-NLS-1$
            }
        }
        return StringUtils.EMPTY;
    }


    /**
     * @return SHA-256 SSH style fingerprint of the key
     */
    public String getFingerprintMD5SSH () {
        if ( this.getValue() instanceof PublicKey ) {
            try {
                return FingerprintUtil.printFingerprintHex(FingerprintUtil.md5SSH((PublicKey) getValue()));
            }
            catch ( CryptoException e1 ) {
                log.warn("Failed to produce public key fingerprint", e1); //$NON-NLS-1$
            }
        }
        return StringUtils.EMPTY;
    }


    /**
     * 
     * @param comment
     */
    public void setComment ( String comment ) {
        ValueExpression ve = this.getValueExpression("comment"); //$NON-NLS-1$
        if ( ve != null ) {
            ve.setValue(FacesContext.getCurrentInstance().getELContext(), comment);
        }
        markModified();
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

            try {
                parsePubkey(f.getInputstream());
            }
            catch ( IOException e ) {
                log.warn("Error processing file upload", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * 
     */
    protected void parsePubkey ( InputStream is ) {
        Object oldVal = getValue();
        try {

            PublicKeyEntry pubkey = PublicKeyParser.parsePublicKeys(is);
            this.setValue(pubkey.getKey());
            this.setComment(pubkey.getComment());
            this.getStateHelper().put(CHOOSE_OTHER, false);

            if ( !Objects.equals(oldVal, pubkey.getKey()) ) {
                markModified();
            }
        }
        catch ( Exception e ) {
            log.debug("Failed to parse public key", e); //$NON-NLS-1$
            setValid(false);
            FacesContext.getCurrentInstance().addMessage(
                this.getClientId(FacesContext.getCurrentInstance()),
                new FacesMessage(FacesMessage.SEVERITY_WARN, BaseMessages.get("pubkeyEditor.cannotParse"), StringUtils.EMPTY)); //$NON-NLS-1$
        }
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
     * 
     * @param ev
     */
    public void useTextInput ( ActionEvent ev ) {
        String textInput = this.getTextInput();
        if ( textInput != null ) {
            this.parsePubkey(new ByteArrayInputStream(textInput.getBytes(Charset.forName("UTF-8")))); //$NON-NLS-1$
            setTextInput(StringUtils.EMPTY);
        }
        else {
            this.getStateHelper().put(CHOOSE_OTHER, Boolean.FALSE);
        }
    }

}
