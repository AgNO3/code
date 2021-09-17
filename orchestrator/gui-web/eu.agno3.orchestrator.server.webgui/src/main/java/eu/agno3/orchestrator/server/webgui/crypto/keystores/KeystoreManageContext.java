/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.04.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto.keystores;


import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.util.Base64;

import eu.agno3.orchestrator.crypto.keystore.CertificateInfo;
import eu.agno3.orchestrator.crypto.keystore.KeyInfo;
import eu.agno3.orchestrator.crypto.keystore.KeyStoreInfo;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.runtime.crypto.keystore.KeyType;
import eu.agno3.runtime.jsf.components.crypto.CertificateUtil;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "keystoreManageContext" )
public class KeystoreManageContext implements Serializable {

    /**
     * 
     */
    private static final String CHARSET = "UTF-8"; //$NON-NLS-1$

    /**
     * 
     */
    private static final long serialVersionUID = 7800178389399164515L;

    private static final Logger log = Logger.getLogger(KeystoreManageContext.class);

    @Inject
    private InstanceKeystoreManager ikm;

    private TreeNode root;
    private List<KeyStoreInfo> keystores;
    private TreeNode[] multiSelection;

    private boolean includeInternal;

    private String selectKeystore;
    private String selectKey;

    private String suggestSubject;


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

    private String suggestSANs;
    private String suggestKeyUsage;
    private String suggestEKUs;

    private TreeNode singleSelection;

    private String selectionMode;

    private boolean allowAnonymous;


    public void init ( ComponentSystemEvent ev ) {
        getRoot();
    }


    public TreeNode getRoot () {
        if ( this.root == null ) {
            this.root = makeModel();
        }
        return this.root;
    }


    public void refresh () {
        this.keystores = null;
        this.root = makeModel();
    }


    /**
     * @return the selection
     */
    public TreeNode[] getMultiSelection () {
        return this.multiSelection;
    }


    /**
     * @param selection
     *            the selection to set
     */
    public void setMultiSelection ( TreeNode[] selection ) {
        this.multiSelection = selection;
    }


    public TreeNode getSingleSelection () {
        return this.singleSelection;
    }


    /**
     * @param singleSelection
     *            the singleSelection to set
     */
    public void setSingleSelection ( TreeNode singleSelection ) {
        this.singleSelection = singleSelection;
    }


    /**
     * @return the includeInternal
     */
    public boolean getIncludeInternal () {
        return this.includeInternal;
    }


    /**
     * @return the selectKey
     */
    public String getSelectKey () {
        return this.selectKey;
    }


    /**
     * @param selectKey
     *            the selectKey to set
     */
    public void setSelectKey ( String selectKey ) {
        this.selectKey = selectKey;
    }


    public String getSelectionMode () {
        return this.selectionMode;
    }


    public void setSelectionMode ( String selectionMode ) {
        if ( "none".equals(selectionMode) || //$NON-NLS-1$
                "single".equals(selectionMode) ) { //$NON-NLS-1$
            this.selectionMode = selectionMode;
        }
    }


    public boolean getAllowAnonymous () {
        return this.allowAnonymous;
    }


    public void setAllowAnonymous ( boolean allowAnon ) {
        this.allowAnonymous = allowAnon;
    }


    /**
     * @return the selectKeystore
     */
    public String getSelectKeystore () {
        return this.selectKeystore;
    }


    /**
     * @param selectKeystore
     *            the selectKeystore to set
     */
    public void setSelectKeystore ( String selectKeystore ) {
        this.selectKeystore = selectKeystore;
    }


    /**
     * @param includeInternal
     *            the includeInternal to set
     */
    public void setIncludeInternal ( boolean includeInternal ) {
        boolean changed = includeInternal != this.includeInternal;
        this.includeInternal = includeInternal;
        if ( changed ) {
            this.keystores = null;
            this.root = makeModel();
        }
    }


    /**
     * @return
     */
    private TreeNode makeModel () {
        DefaultTreeNode rootNode = new DefaultTreeNode();

        for ( KeyStoreInfo ksi : getKeystores() ) {
            DefaultTreeNode ksNode = new DefaultTreeNode(ksi, rootNode);
            ksNode.setExpanded(true);
            ksNode.setSelectable(false);

            List<KeyInfo> keyEntries = new ArrayList<>(ksi.getKeyEntries());
            Collections.sort(keyEntries, new KeyInfoComparator());

            for ( KeyInfo keyInfo : keyEntries ) {
                KeyInfoWrapper w = new KeyInfoWrapper();
                w.setKeystore(ksi);
                w.setKey(keyInfo);
                w.setDecodedCertificateChain(decodeCertChain(keyInfo));
                w.setPublicKey(decodePublicKey(keyInfo));
                if ( w.getPublicKey() != null ) {
                    w.setPublicKeyFingerprint(CertificateUtil.formatPubkeyFingerprintSHA256(w.getPublicKey()));
                }
                DefaultTreeNode tn = new DefaultTreeNode(w, ksNode);
                if ( !StringUtils.isBlank(this.selectKey) && this.selectKey.equals(keyInfo.getKeyAlias()) ) {
                    setSingleSelection(tn);
                    tn.setSelected(true);
                }
            }
        }

        return rootNode;
    }


    /**
     * @param keyInfo
     * @return the decoded public key
     */
    public static PublicKey decodePublicKey ( KeyInfo keyInfo ) {
        byte[] pkData = Base64.decode(keyInfo.getEncodedPublicKey());
        try {

            KeyType kt;
            try {
                kt = KeyType.valueOf(keyInfo.getKeyType());
            }
            catch ( IllegalArgumentException e ) {
                throw new InvalidKeySpecException("Unknown key type " + keyInfo.getKeyType()); //$NON-NLS-1$
            }

            return KeyFactory.getInstance(kt.getAlgo()).generatePublic(new X509EncodedKeySpec(pkData));
        }
        catch (
            InvalidKeySpecException |
            NoSuchAlgorithmException e ) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        GuiMessages.get("keystoremanager.pubkeyDecodeFail"), //$NON-NLS-1$
                        StringUtils.EMPTY));
            return null;
        }
    }


    /**
     * @param keyInfo
     * @return the unwrapped chain
     */
    public static List<Certificate> decodeCertChain ( KeyInfo keyInfo ) {
        List<Certificate> certs = new LinkedList<>();

        if ( keyInfo != null && keyInfo.getCertificateChain() != null ) {
            for ( CertificateInfo ci : keyInfo.getCertificateChain() ) {
                byte[] certData = Base64.decode(ci.getCertificateData());

                try {
                    certs.add(CertificateFactory.getInstance("X509").generateCertificate(new ByteArrayInputStream(certData))); //$NON-NLS-1$
                }
                catch ( CertificateException e ) {
                    FacesContext.getCurrentInstance()
                            .addMessage(null, new FacesMessage(
                                FacesMessage.SEVERITY_ERROR,
                                GuiMessages.get("keystoremanager.certDecodeFail"), //$NON-NLS-1$
                                StringUtils.EMPTY));
                }
            }
        }

        return certs;
    }


    public static List<Certificate> getDecodedCertChain ( Object o ) {
        if ( ! ( o instanceof KeyInfoWrapper ) ) {
            return Collections.EMPTY_LIST;
        }

        KeyInfoWrapper kiw = (KeyInfoWrapper) o;
        return kiw.getDecodedCertificateChain();
    }


    public String dialogClose () {
        TreeNode selection = this.getSingleSelection();
        if ( selection == null ) {
            return DialogContext.closeDialog(null);
        }
        Object data = selection.getData();

        if ( ! ( data instanceof KeyInfoWrapper ) ) {
            return DialogContext.closeDialog(null);
        }

        KeyInfoWrapper ki = (KeyInfoWrapper) data;
        String keyAlias = ki.getKey().getKeyAlias();
        String keyStoreAlias = ki.getKeystore().getAlias();
        String select = keyStoreAlias + ':' + keyAlias;
        return DialogContext.closeDialog(select);
    }


    public void newKeyReturn ( SelectEvent ev ) {
        if ( ev.getObject() instanceof String ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Selecting new key"); //$NON-NLS-1$
            }
            setSelectKey((String) ev.getObject());
        }
        refresh();
    }


    public void deleteKeyReturn ( SelectEvent ev ) {
        TreeNode sel = getSingleSelection();
        if ( sel != null && sel.getData() instanceof KeyInfoWrapper ) {

            String deleted = (String) ev.getObject();
            KeyInfoWrapper ki = (KeyInfoWrapper) sel.getData();
            String selected = ki.getKeystore().getAlias() + ':' + ki.getKey().getKeyAlias();

            if ( selected.equals(deleted) ) {
                log.debug("Deselecting deleted key"); //$NON-NLS-1$
                setSingleSelection(null);
            }
        }
        else {
            log.debug("No selection"); //$NON-NLS-1$
        }
        refresh();
    }


    /**
     * @return
     * 
     */
    private List<KeyStoreInfo> getKeystores () {
        try {
            if ( this.keystores == null ) {
                if ( !StringUtils.isBlank(this.selectKeystore) ) {
                    KeyStoreInfo keystoreInfo = this.ikm.getKeystoreInfo(this.selectKeystore);
                    if ( keystoreInfo == null ) {
                        this.keystores = Collections.EMPTY_LIST;
                    }
                    else {
                        this.keystores = Arrays.asList(keystoreInfo);
                    }
                }
                else {
                    this.keystores = this.ikm.getKeystores(this.includeInternal);
                }
            }
            return this.keystores;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return Collections.EMPTY_LIST;
        }
    }


    public String makeURLParams ( String keystore, String key ) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        String cs = CHARSET; // $NON-NLS-1$
        if ( keystore != null || !StringUtils.isBlank(this.selectKeystore) ) {
            sb.append("keystore="); //$NON-NLS-1$

            sb.append(URLEncoder.encode(keystore != null ? keystore : this.selectKeystore, cs));
            sb.append('&');
        }

        if ( key != null ) {
            sb.append("key="); //$NON-NLS-1$
            sb.append(URLEncoder.encode(key, cs));
            sb.append('&');
        }

        if ( !StringUtils.isBlank(this.suggestSubject) ) {
            sb.append("suggestSubject="); //$NON-NLS-1$
            sb.append(URLEncoder.encode(this.suggestSubject, cs));
            sb.append('&');
        }

        if ( !StringUtils.isBlank(this.suggestSANs) ) {
            sb.append("suggestSANs="); //$NON-NLS-1$
            sb.append(URLEncoder.encode(this.suggestSANs, cs));
            sb.append('&');
        }

        if ( !StringUtils.isBlank(this.suggestKeyUsage) ) {
            sb.append("suggestKeyUsage="); //$NON-NLS-1$
            sb.append(URLEncoder.encode(this.suggestKeyUsage, cs));
            sb.append('&');
        }

        if ( !StringUtils.isBlank(this.suggestEKUs) ) {
            sb.append("suggestEKUs="); //$NON-NLS-1$
            sb.append(URLEncoder.encode(this.suggestEKUs, cs));
            sb.append('&');
        }

        if ( sb.length() > 1 && sb.charAt(sb.length() - 1) == '&' ) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }


    public static boolean isKeystore ( Object o ) {
        return o instanceof KeyStoreInfo;
    }


    public static boolean isKey ( Object o ) {
        return o instanceof KeyInfoWrapper;
    }


    public static boolean isEmptyKeystore ( Object o ) {
        return o == null;
    }
}
