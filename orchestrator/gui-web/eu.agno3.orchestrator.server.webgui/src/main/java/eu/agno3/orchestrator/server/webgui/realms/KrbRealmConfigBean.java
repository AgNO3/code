/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.realms;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.orchestrator.config.realms.KRBRealmConfig;
import eu.agno3.orchestrator.config.realms.KRBRealmConfigImpl;
import eu.agno3.orchestrator.config.realms.i18n.RealmsConfigMessages;
import eu.agno3.orchestrator.realms.RealmType;
import eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;


/**
 * @author mbechler
 *
 */
@Named ( "krbRealmConfigBean" )
@ApplicationScoped
public class KrbRealmConfigBean extends AbstractConfigObjectBean<KRBRealmConfig, KRBRealmConfigImpl> {

    public Comparator<KRBRealmConfig> getComparator () {
        return new KRBRealmComparator();
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


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getObjectType()
     */
    @Override
    protected Class<KRBRealmConfig> getObjectType () {
        return KRBRealmConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getInstanceType()
     */
    @Override
    protected Class<KRBRealmConfigImpl> getInstanceType () {
        return KRBRealmConfigImpl.class;
    }


    public List<RealmType> getRealmTypes () {
        List<RealmType> types = new ArrayList<>(Arrays.asList(RealmType.values()));
        types.remove(RealmType.AD);
        return types;
    }


    public String translateRealmType ( Object type ) {
        return translateEnumValue(RealmType.class, type);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#cloneInternal(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected void cloneInternal ( ConfigContext<?, ?> ctx, KRBRealmConfigImpl cloned, KRBRealmConfig old, KRBRealmConfig def ) {
        cloned.setRealmName(old.getRealmName());
        super.cloneDefault(ctx, cloned, old, def);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#labelForInternal(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected String labelForInternal ( KRBRealmConfig obj ) {
        return obj.getRealmName();
    }

}
