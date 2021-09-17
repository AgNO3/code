/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.04.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.realms;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.realms.ADRealmConfig;
import eu.agno3.orchestrator.config.realms.KRBRealmConfig;
import eu.agno3.orchestrator.config.realms.i18n.RealmsConfigMessages;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.realms.KeyInfo;
import eu.agno3.orchestrator.realms.KeytabInfo;
import eu.agno3.orchestrator.realms.RealmInfo;
import eu.agno3.orchestrator.realms.RealmManagementException;
import eu.agno3.orchestrator.realms.RealmType;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.orchestrator.server.webgui.components.MultiObjectEditor;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;


/**
 * @author mbechler
 *
 */
@Named ( "realmManageContext" )
@ViewScoped
public class RealmManageContext implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5479829387857136291L;

    private TreeNode root;

    @Inject
    private InstanceRealmManager irm;

    @Inject
    private CoreServiceProvider csp;

    private List<RealmInfo> realmInfos;

    private TreeNode[] selection;

    private String selectRealm;


    public void init ( ComponentSystemEvent ev ) {
        getRoot();
    }


    public TreeNode getRoot () {

        if ( this.root == null ) {
            this.root = makeModel();
        }

        return this.root;
    }


    /**
     * @return the selection
     */
    public TreeNode[] getSelection () {
        return this.selection;
    }


    /**
     * @param selection
     *            the selection to set
     */
    public void setSelection ( TreeNode[] selection ) {
        this.selection = selection;
    }


    /**
     * @return the selectRealm
     */
    public String getSelectRealm () {
        return this.selectRealm;
    }


    /**
     * @param selectRealm
     *            the selectRealm to set
     */
    public void setSelectRealm ( String selectRealm ) {
        this.selectRealm = selectRealm;
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws RealmManagementException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private List<RealmInfo> getRealmInfos () {
        if ( this.realmInfos == null ) {
            try {

                List<RealmInfo> realms;
                if ( !StringUtils.isBlank(this.selectRealm) ) {
                    RealmInfo realm = this.irm.getRealm(this.selectRealm);
                    if ( realm != null ) {
                        realms = new ArrayList<>(Collections.singleton(realm));
                    }
                    else {
                        realms = new ArrayList<>();
                    }
                }
                else {
                    realms = this.irm.getRealms();
                }
                Collections.sort(realms, new RealmInfoComparator());
                this.realmInfos = realms;
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
                this.realmInfos = Collections.EMPTY_LIST;
            }
        }
        return this.realmInfos;
    }


    public void refresh () {
        this.realmInfos = null;
        this.root = makeModel();
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws RealmManagementException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private TreeNode makeModel () {
        DefaultTreeNode rn = new DefaultTreeNode();
        rn.setExpanded(true);
        for ( RealmInfo ri : this.getRealmInfos() ) {
            DefaultTreeNode realmNode = new DefaultTreeNode(ri, rn);
            realmNode.setSelectable(false);
            realmNode.setType(ri.getType().name());
            realmNode.setExpanded(true);

            for ( KeytabInfo kti : ri.getKeytabs() ) {
                KeytabInfoWrapper keytabInfoWrapper = new KeytabInfoWrapper();
                keytabInfoWrapper.setRealm(ri);
                keytabInfoWrapper.setKeytab(kti);
                DefaultTreeNode keytabNode = new DefaultTreeNode(keytabInfoWrapper, realmNode);
                keytabNode.setType("keytab"); //$NON-NLS-1$
                keytabNode.setSelectable(false);
                keytabNode.setExpanded(!StringUtils.isEmpty(this.selectRealm));

                for ( KeyInfo ki : kti.getKeys() ) {
                    KeyInfoWrapper keyInfoWrapper = new KeyInfoWrapper();
                    keyInfoWrapper.setKeytab(keytabInfoWrapper);
                    keyInfoWrapper.setKey(ki);
                    DefaultTreeNode keyNode = new DefaultTreeNode(keyInfoWrapper, keytabNode);
                    keyNode.setType("key"); //$NON-NLS-1$
                }
            }
        }

        return rn;
    }


    public String translateAlgorithm ( String algo ) {
        if ( algo == null ) {
            return null;
        }
        int sepPos = algo.indexOf('-');
        return algo.substring(0, sepPos);
    }


    public String translateRealmType ( RealmType type ) {
        if ( type == null ) {
            return null;
        }
        return translateEnumValue(RealmType.class, type);
    }


    public ResourceBundle getLocalizationBundle () {
        return this.csp.getLocalizationService().getBundle(RealmsConfigMessages.BASE, FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }


    public <TEnum extends Enum<TEnum>> String translateEnumValue ( Class<TEnum> en, Object val ) {
        if ( val == null || !en.isAssignableFrom(val.getClass()) ) {
            return null;
        }
        @SuppressWarnings ( "unchecked" )
        TEnum enumVal = (TEnum) val;
        StringBuilder key = new StringBuilder();
        key.append(en.getSimpleName());
        key.append('.');
        key.append(enumVal.name());
        return this.getLocalizationBundle().getString(key.toString());
    }


    public String deleteSelection () {
        if ( this.selection == null ) {
            return null;
        }

        Set<KeyInfoWrapper> keys = new HashSet<>();
        for ( TreeNode n : this.selection ) {
            if ( n.getData() instanceof KeyInfoWrapper ) {
                keys.add((KeyInfoWrapper) n.getData());
            }
        }

        try {
            this.irm.deleteKeys(keys);
            this.refresh();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }

        return null;
    }


    public String getRealmName ( OuterWrapper<?> outer ) {

        try {
            OuterWrapper<?> outerWrapper = outer.get("urn:agno3:objects:1.0:realms:ad"); //$NON-NLS-1$
            if ( outerWrapper != null ) {
                ADRealmConfig cfg;
                if ( outerWrapper.getEditor() instanceof MultiObjectEditor ) {
                    cfg = (ADRealmConfig) ( (MultiObjectEditor) outerWrapper.getEditor() ).getSelectedObject();
                }
                else {
                    cfg = (ADRealmConfig) outerWrapper.getEditor().getCurrent();
                }
                if ( cfg != null ) {
                    return cfg.getRealmName();
                }
            }

            outerWrapper = outer.get("urn:agno3:objects:1.0:realms:krb"); //$NON-NLS-1$
            if ( outerWrapper != null ) {
                KRBRealmConfig cfg;
                if ( outerWrapper.getEditor() instanceof MultiObjectEditor ) {
                    cfg = (KRBRealmConfig) ( (MultiObjectEditor) outerWrapper.getEditor() ).getSelectedObject();
                }
                else {
                    cfg = (KRBRealmConfig) outerWrapper.getEditor().getCurrent();
                }
                if ( cfg != null ) {
                    return cfg.getRealmName();
                }
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }

        return null;

    }


    public RealmType getRealmType ( OuterWrapper<?> outer ) {
        try {
            OuterWrapper<?> outerWrapper = outer.get("urn:agno3:objects:1.0:realms:ad"); //$NON-NLS-1$
            if ( outerWrapper != null ) {
                ADRealmConfig cfg;
                if ( outerWrapper.getEditor() instanceof MultiObjectEditor ) {
                    cfg = (ADRealmConfig) ( (MultiObjectEditor) outerWrapper.getEditor() ).getSelectedObject();
                }
                else {
                    cfg = (ADRealmConfig) outerWrapper.getEditor().getCurrent();
                }
                if ( cfg != null ) {
                    return RealmType.AD;
                }
            }

            outerWrapper = outer.get("urn:agno3:objects:1.0:realms:krb"); //$NON-NLS-1$
            if ( outerWrapper != null ) {
                KRBRealmConfig cfg;
                if ( outerWrapper.getEditor() instanceof MultiObjectEditor ) {
                    cfg = (KRBRealmConfig) ( (MultiObjectEditor) outerWrapper.getEditor() ).getSelectedObject();
                }
                else {
                    cfg = (KRBRealmConfig) outerWrapper.getEditor().getCurrent();
                }
                if ( cfg != null ) {
                    return cfg.getRealmType();
                }
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }

        return null;

    }


    public static boolean isRealm ( Object o ) {
        return o instanceof RealmInfo;
    }


    public static boolean isADRealm ( Object o ) {
        return o instanceof RealmInfo && ( (RealmInfo) o ).getType() == RealmType.AD;
    }


    public static boolean isKeytab ( Object o ) {
        return o instanceof KeytabInfoWrapper;
    }


    public static boolean isKey ( Object o ) {
        return o instanceof KeyInfoWrapper;
    }
}
