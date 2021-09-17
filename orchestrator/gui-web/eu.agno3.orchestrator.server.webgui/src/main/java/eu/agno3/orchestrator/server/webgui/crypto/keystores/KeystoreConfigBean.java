/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto.keystores;


import java.util.Comparator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.orchestrator.config.crypto.i18n.CryptoConfigMessages;
import eu.agno3.orchestrator.config.crypto.keystore.KeystoreConfig;
import eu.agno3.orchestrator.config.crypto.keystore.KeystoreConfigImpl;
import eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;


/**
 * @author mbechler
 *
 */
@Named ( "keystoreConfigBean" )
@ApplicationScoped
public class KeystoreConfigBean extends AbstractConfigObjectBean<KeystoreConfig, KeystoreConfigImpl> {

    public Comparator<KeystoreConfig> getComparator () {
        return new KeystoreComparator();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getMessageBase()
     */
    @Override
    protected String getMessageBase () {
        return CryptoConfigMessages.BASE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getObjectType()
     */
    @Override
    protected Class<KeystoreConfig> getObjectType () {
        return KeystoreConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getInstanceType()
     */
    @Override
    protected Class<KeystoreConfigImpl> getInstanceType () {
        return KeystoreConfigImpl.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#cloneInternal(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected void cloneInternal ( ConfigContext<?, ?> ctx, KeystoreConfigImpl cloned, KeystoreConfig old, KeystoreConfig def ) {
        cloned.setAlias(old.getAlias());
        super.cloneDefault(ctx, cloned, old, def);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#labelForInternal(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected String labelForInternal ( KeystoreConfig obj ) {
        return obj.getAlias();
    }

}
