/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto.keystores;


import java.util.Comparator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.orchestrator.config.crypto.i18n.CryptoConfigMessages;
import eu.agno3.orchestrator.config.crypto.keystore.ImportKeyPairEntry;
import eu.agno3.orchestrator.config.crypto.keystore.ImportKeyPairEntryConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.crypto.keystore.ImportKeyPairEntryImpl;
import eu.agno3.orchestrator.config.crypto.keystore.ImportKeyPairEntryMutable;
import eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;


/**
 * @author mbechler
 *
 */
@Named ( "importKeyPairEntryBean" )
@ApplicationScoped
public class ImportKeyPairEntryBean extends AbstractConfigObjectBean<ImportKeyPairEntry, ImportKeyPairEntryImpl> {

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
    protected Class<ImportKeyPairEntry> getObjectType () {
        return ImportKeyPairEntry.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getInstanceType()
     */
    @Override
    protected Class<ImportKeyPairEntryImpl> getInstanceType () {
        return ImportKeyPairEntryImpl.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#makeInstance()
     */
    @Override
    public ImportKeyPairEntry makeInstance () throws InstantiationException, IllegalAccessException {
        return ImportKeyPairEntryConfigObjectTypeDescriptor.emptyInstance();
    }


    /**
     * 
     * @param ke
     * @return a certificate chain wrapper
     */
    public CertEntryChainWrapper getChainWrapper ( Object ke ) {
        if ( ! ( ke instanceof ImportKeyPairEntryMutable ) ) {
            return null;
        }
        return new CertEntryChainWrapper((ImportKeyPairEntryMutable) ke);

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#labelForInternal(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected String labelForInternal ( ImportKeyPairEntry obj ) {
        return obj.getAlias();
    }


    public Comparator<ImportKeyPairEntry> getComparator () {
        return new ImportKeyEntryComparator();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#cloneInternal(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected void cloneInternal ( ConfigContext<?, ?> ctx, ImportKeyPairEntryImpl cloned, ImportKeyPairEntry old, ImportKeyPairEntry def ) {
        cloned.setAlias(old.getAlias());
        super.cloneDefault(ctx, cloned, old, def);
    }

}
