/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.realms;


import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.realms.ADJoinType;
import eu.agno3.orchestrator.config.realms.AbstractRealmConfigImpl;
import eu.agno3.orchestrator.config.realms.KerberosSecurityLevel;
import eu.agno3.orchestrator.config.realms.RealmConfig;
import eu.agno3.orchestrator.config.realms.i18n.RealmsConfigMessages;
import eu.agno3.orchestrator.realms.RealmInfo;
import eu.agno3.orchestrator.realms.RealmType;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.config.AbstractBaseConfigObjectBean;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;
import eu.agno3.orchestrator.server.webgui.config.EffectiveConfigCacheBean;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureCacheBean;
import eu.agno3.orchestrator.server.webgui.util.completer.Completer;
import eu.agno3.orchestrator.server.webgui.util.completer.ListCompleter;


/**
 * @author mbechler
 *
 */
@Named ( "realmConfigBean" )
@ApplicationScoped
public class RealmConfigBean extends AbstractBaseConfigObjectBean<RealmConfig, AbstractRealmConfigImpl<RealmConfig>> {

    @Inject
    private EffectiveConfigCacheBean hcCache;

    @Inject
    private RealmStatusBean realmStatus;

    @Inject
    private StructureCacheBean structureCache;

    /**
     * 
     */
    private static final String HC_TYPE = "urn:agno3:objects:1.0:hostconfig"; //$NON-NLS-1$


    public Comparator<RealmConfig> getComparator () {
        return new RealmComparator();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getMessageBase()
     */
    @Override
    protected String getMessageBase () {
        return RealmsConfigMessages.BASE;
    }


    public String getRealmName ( OuterWrapper<?> outerWrapper, String objType ) {
        if ( outerWrapper == null ) {
            return null;
        }

        OuterWrapper<?> ow = outerWrapper.get(objType);
        if ( ow == null ) {
            return null;
        }

        AbstractObjectEditor<?> editor = ow.getEditor();

        return getRealmName(editor);
    }


    /**
     * @param editor
     * @return
     */
    private static String getRealmName ( AbstractObjectEditor<?> editor ) {
        if ( editor == null ) {
            return null;
        }
        try {
            Object current = editor.getCurrent();
            if ( current instanceof RealmConfig ) {
                String rn = ( (RealmConfig) current ).getRealmName();
                if ( !StringUtils.isBlank(rn) ) {
                    return rn;
                }
            }

            Object def = editor.getDefaults();
            if ( def instanceof RealmConfig ) {
                String rn = ( (RealmConfig) def ).getRealmName();
                if ( !StringUtils.isBlank(rn) ) {
                    return rn;
                }
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    public String getRealmStatus ( OuterWrapper<?> outerWrapper ) {
        String realmName = getRealmName(outerWrapper.getEditor());
        if ( realmName == null ) {
            return GuiMessages.get("realms.state.unconfigured"); //$NON-NLS-1$
        }

        StructuralObject anchor;
        try {
            anchor = outerWrapper.getContext().getAnchor();
            if ( anchor instanceof ServiceStructuralObject ) {
                anchor = this.structureCache.getParentFor(anchor);
            }

            if ( ! ( anchor instanceof InstanceStructuralObject ) ) {
                return null;
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return null;
        }

        RealmInfo ri = this.realmStatus.getStatus((InstanceStructuralObject) anchor, realmName);

        if ( ri == null ) {
            return GuiMessages.get("realms.state.unconfigured"); //$NON-NLS-1$
        }
        else if ( ri.getJoined() ) {
            return GuiMessages.get("realms.state.joined"); //$NON-NLS-1$
        }
        else {
            return GuiMessages.get("realms.state.unjoined"); //$NON-NLS-1$
        }
    }


    public boolean isRealmConfigured ( OuterWrapper<?> outerWrapper ) {
        String realmName = getRealmName(outerWrapper.getEditor());
        if ( realmName == null ) {
            return false;
        }

        StructuralObject anchor;
        try {
            anchor = outerWrapper.getContext().getAnchor();
            if ( anchor instanceof ServiceStructuralObject ) {
                anchor = this.structureCache.getParentFor(anchor);
            }

            if ( ! ( anchor instanceof InstanceStructuralObject ) ) {
                return false;
            }
        }
        catch ( Exception e ) {
            return false;
        }

        RealmInfo ri = this.realmStatus.getStatus((InstanceStructuralObject) anchor, realmName);

        if ( ri == null ) {
            return false;
        }
        return true;
    }


    public RealmType getAdType () {
        return RealmType.AD;
    }


    public Completer<String> getRealmCompleter ( ConfigContext<?, ?> ctx, RealmType rt ) {
        try {
            if ( ctx == null || ctx.getAbstract() ) {
                return fallbackCompleter();
            }
            StructuralObject anchor = ctx.getAnchor();
            if ( anchor == null ) {
                return fallbackCompleter();
            }
            HostConfiguration hc = (HostConfiguration) this.hcCache
                    .getEffectiveSingletonConfig(anchor, HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE, HC_TYPE);
            if ( hc == null ) {
                return fallbackCompleter();
            }
            return makeCompleter(hc.getRealmsConfiguration().getRealms(), rt);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return fallbackCompleter();
        }
    }


    /**
     * @return
     */
    ListCompleter fallbackCompleter () {
        return new ListCompleter();
    }


    /**
     * @param realms
     * @return
     */
    Completer<String> makeCompleter ( final Collection<RealmConfig> realms, RealmType rt ) {
        return new Completer<String>() {

            @Override
            public List<String> complete ( String query ) {
                List<String> res = new LinkedList<>();
                for ( RealmConfig e : realms ) {
                    String name = e.getRealmName();
                    if ( rt != null && rt != e.getRealmType() ) {
                        continue;
                    }
                    if ( name != null && !name.isEmpty() && name.startsWith(query) ) {
                        res.add(name);
                    }
                }
                return res;
            }
        };
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getObjectType()
     */
    @Override
    protected Class<RealmConfig> getObjectType () {
        return RealmConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractBaseConfigObjectBean#cloneInternal(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected void cloneInternal ( AbstractRealmConfigImpl<RealmConfig> cloned, RealmConfig obj ) {
        cloned.clone(obj);
    }


    public String translateSecurityLevel ( Object val ) {
        return this.translateEnumValue(KerberosSecurityLevel.class, val);
    }


    public String translateSecurityLevelDescription ( Object val ) {
        return this.translateEnumDescription(KerberosSecurityLevel.class, val);
    }


    public KerberosSecurityLevel[] getSecurityLevels () {
        return KerberosSecurityLevel.values();
    }


    public ADJoinType[] getAdJoinTypes () {
        return ADJoinType.values();
    }


    public String translateADJoinType ( Object val ) {
        return this.translateEnumValue(ADJoinType.class, val);
    }


    public String translateADJoinTypeDescription ( Object val ) {
        return this.translateEnumDescription(ADJoinType.class, val);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractBaseConfigObjectBean#labelForInternal(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected String labelForInternal ( RealmConfig obj ) {
        return ( obj.getRealmType() != null ? obj.getRealmType() : "KRB5" ) + //$NON-NLS-1$
                ": " + //$NON-NLS-1$
                ( obj.getRealmName() != null ? obj.getRealmName() : GuiMessages.get(GuiMessages.UNNAMED_CONFIG_OBJECT) );
    }
}
