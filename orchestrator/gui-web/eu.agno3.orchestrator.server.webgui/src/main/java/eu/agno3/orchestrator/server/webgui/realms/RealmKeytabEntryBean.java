/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.realms;


import java.util.Comparator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.orchestrator.config.realms.KeytabEntry;
import eu.agno3.orchestrator.config.realms.KeytabEntryImpl;
import eu.agno3.orchestrator.config.realms.i18n.RealmsConfigMessages;
import eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;


/**
 * @author mbechler
 *
 */
@Named ( "realmKeytabEntryBean" )
@ApplicationScoped
public class RealmKeytabEntryBean extends AbstractConfigObjectBean<KeytabEntry, KeytabEntryImpl> {

    public Comparator<KeytabEntry> getComparator () {
        return new KeytabEntryComparator();
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
    protected Class<KeytabEntry> getObjectType () {
        return KeytabEntry.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getInstanceType()
     */
    @Override
    protected Class<KeytabEntryImpl> getInstanceType () {
        return KeytabEntryImpl.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#cloneInternal(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected void cloneInternal ( ConfigContext<?, ?> ctx, KeytabEntryImpl cloned, KeytabEntry old, KeytabEntry def ) {
        cloned.setKeytabId(old.getKeytabId());
        super.cloneDefault(ctx, cloned, old, def);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#labelForInternal(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected String labelForInternal ( KeytabEntry obj ) {
        return obj.getKeytabId();
    }

}
