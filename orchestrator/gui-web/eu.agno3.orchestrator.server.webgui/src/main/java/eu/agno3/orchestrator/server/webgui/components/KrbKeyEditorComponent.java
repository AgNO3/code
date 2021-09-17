/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.04.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.components;


import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.application.FacesMessage;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import eu.agno3.orchestrator.realms.KeyData;
import eu.agno3.orchestrator.realms.RealmType;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.runtime.net.krb5.ETypesUtil;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.KeyTab;
import eu.agno3.runtime.net.krb5.KeyTabEntry;


/**
 * @author mbechler
 *
 */
public class KrbKeyEditorComponent extends UIInput implements NamingContainer {

    private static final Serializable KEYADD_TYPE = "keyAddType"; //$NON-NLS-1$
    /**
     * 
     */
    private static final String KEYTAB_ADD = "keytab"; //$NON-NLS-1$
    private static final String PASSWORD_ADD = "password"; //$NON-NLS-1$

    private static final String[] DEFAULT_ETYPES = new String[] {
        "aes256-cts-hmac-sha1-96", //$NON-NLS-1$
        "aes128-cts-hmac-sha1-96", //$NON-NLS-1$
        "des3-cbc-sha1", //$NON-NLS-1$
        "arcfour-hmac-md5" //$NON-NLS-1$
    };


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
     * @return
     */
    private String getDefaultKeyAddType () {
        if ( getRealmType() == RealmType.AD ) {
            return PASSWORD_ADD;
        }
        return KEYTAB_ADD;
    }


    private String getRealmName () {
        return (String) getAttributes().get("realm"); //$NON-NLS-1$
    }


    /**
     * @return
     */
    private RealmType getRealmType () {
        RealmType realmType = (RealmType) getAttributes().get("realmType"); //$NON-NLS-1$
        if ( realmType == null ) {
            return RealmType.UNSPECIFIED;
        }
        return realmType;
    }


    public String getKeyAddType () {
        return (String) this.getStateHelper().eval(
            KEYADD_TYPE,
            Boolean.TRUE.toString().equals(this.getAttributes().get("initialAdd")) ? getDefaultKeyAddType() : null); //$NON-NLS-1$
    }


    public void setKeyAddType ( String keyAddType ) {
        this.getStateHelper().put(KEYADD_TYPE, keyAddType);
    }


    public void doAdd ( ActionEvent ev ) {
        this.setKeyAddType(getDefaultKeyAddType());
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
    }


    /**
     * @param keys
     */
    @SuppressWarnings ( "unchecked" )
    private void addKeys ( Collection<KeyData> keys ) {
        Object val = this.getValue();

        if ( ! ( val instanceof Set ) ) {
            this.setValue(new TreeSet<>());
        }

        if ( ! ( val instanceof SortedSet<?> ) ) {
            this.setValue(new TreeSet<>((Set<KeyData>) val));
        }

        ( (Set<KeyData>) this.getValue() ).addAll(keys);
    }


    public void handleKeytabUpload ( FileUploadEvent ev ) {
        UploadedFile file = ev.getFile();
        try {
            KeyTab parsed = KeyTab.parse(file.getInputstream());
            List<KeyData> keys = new LinkedList<>();
            for ( KeyTabEntry e : parsed.getEntries() ) {
                KeyData k = new KeyData();
                k.setPrincipal(e.getPrincipal().toString());
                k.setKvno(e.getKvno());
                k.setAlgorithm(ETypesUtil.mapEType(e.getKeyblockType()));
                k.setData(Base64.encodeBase64String(e.getKeyblock()));
                keys.add(k);
            }

            addKeys(keys);

            this.setKeyAddType(null);
        }
        catch (
            IOException |
            KerberosException e ) {
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, GuiMessages.get("component.krbkeyeditor.keytabParseFail"), //$NON-NLS-1$
                    StringUtils.EMPTY));
        }
    }


    /**
     * 
     * @param ev
     */
    public void addPasswordKeys ( ActionEvent ev ) {
        List<KeyData> keysFromPassword = getKeysFromPassword();
        if ( !keysFromPassword.isEmpty() ) {
            addKeys(keysFromPassword);
            this.setPasswordKey(null);
            this.setPasswordKeyConfirm(null);
            this.setPasswordKeyPrincipal(null);
            this.setKeyAddType(null);
        }
    }


    /**
     * @return
     */
    private List<KeyData> getKeysFromPassword () {

        if ( !this.getPasswordKey().equals(this.getPasswordKeyConfirm()) ) {
            FacesContext.getCurrentInstance().addMessage(this.findComponent("passwordField:keyPassword").getClientId(), //$NON-NLS-1$
                new FacesMessage(FacesMessage.SEVERITY_ERROR, StringUtils.EMPTY, GuiMessages.get("component.krbkeyeditor.passwordNoMatch"))); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }

        String princ = this.getPasswordKeyPrincipal();
        int realmSepPos = princ.lastIndexOf('@');
        if ( getRealmName() != null && realmSepPos < 0 ) {
            princ = princ + "@" + getRealmName(); //$NON-NLS-1$
        }
        else if ( realmSepPos < 0 ) {
            FacesContext.getCurrentInstance().addMessage(this.findComponent("principalField:principal").getClientId(), //$NON-NLS-1$
                new FacesMessage(FacesMessage.SEVERITY_ERROR, StringUtils.EMPTY, GuiMessages.get("component.krbkeyeditor.realmUnknown"))); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }
        KerberosPrincipal kp = new KerberosPrincipal(princ);
        List<KeyData> keys = new LinkedList<>();
        for ( String etype : this.getPasswordKeyETypes() ) {
            KerberosKey key = new KerberosKey(kp, this.getPasswordKey().toCharArray(), ETypesUtil.getAlgoFromEtype(ETypesUtil
                    .eTypeFromMITString(etype)));
            KeyData data = new KeyData();
            data.setAlgorithm(etype);
            data.setKvno(this.getPasswordKeyKVNO());
            data.setPrincipal(kp.toString());
            data.setData(Base64.encodeBase64String(key.getEncoded()));
            keys.add(data);
        }
        return keys;
    }


    public String removeKey ( Object o ) {
        if ( ! ( o instanceof KeyData ) ) {
            return null;
        }

        @SuppressWarnings ( "unchecked" )
        Set<KeyData> data = (Set<KeyData>) this.getValue();
        if ( data == null ) {
            return null;
        }
        data.remove(o);
        return null;
    }


    /**
     * @return the password key knvo
     */
    public long getPasswordKeyKVNO () {
        return (long) getStateHelper().eval("passwordKeyKVNO", 0L); //$NON-NLS-1$
    }


    public void setPasswordKeyKVNO ( long kvno ) {
        getStateHelper().put("passwordKeyKVNO", kvno); //$NON-NLS-1$
    }


    /**
     * @return the password key
     */
    public String getPasswordKey () {
        return (String) getStateHelper().eval("passwordKey"); //$NON-NLS-1$
    }


    public void setPasswordKey ( String passwordKey ) {
        getStateHelper().put("passwordKey", passwordKey); //$NON-NLS-1$
    }


    /**
     * @return the password key
     */
    public String getPasswordKeyConfirm () {
        return (String) getStateHelper().eval("passwordKeyConfirm"); //$NON-NLS-1$
    }


    public void setPasswordKeyConfirm ( String passwordKey ) {
        getStateHelper().put("passwordKeyConfirm", passwordKey); //$NON-NLS-1$
    }


    /**
     * @return the password key etypes
     */
    @SuppressWarnings ( "unchecked" )
    public List<String> getPasswordKeyETypes () {
        List<String> res = (List<String>) getStateHelper().eval("passwordKeyETypes", Arrays.asList(DEFAULT_ETYPES)); //$NON-NLS-1$
        if ( res == null || res.isEmpty() ) {
            res = Arrays.asList(DEFAULT_ETYPES);
        }
        return res;
    }


    public void setPasswordKeyETypes ( List<String> etypes ) {
        getStateHelper().put("passwordKeyETypes", etypes); //$NON-NLS-1$
    }


    /**
     * @return the password key principal
     */
    public String getPasswordKeyPrincipal () {
        return (String) getStateHelper().eval("passwordKeyPrincipal"); //$NON-NLS-1$
    }


    public void setPasswordKeyPrincipal ( String principal ) {
        getStateHelper().put("passwordKeyPrincipal", principal); //$NON-NLS-1$
    }


    /**
     * 
     * @return the known enctypes
     */
    public String[] getAvailableEnctypeAlgos () {
        return ETypesUtil.ETYPES;
    }


    public static String translateAlgorithm ( String algo ) {
        if ( algo == null ) {
            return null;
        }
        int sepPos = algo.indexOf('-');
        return algo.substring(0, sepPos);
    }

}
