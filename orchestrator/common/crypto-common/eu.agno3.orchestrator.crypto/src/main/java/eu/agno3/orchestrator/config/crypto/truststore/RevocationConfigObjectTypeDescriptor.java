/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.truststore;


import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.Duration;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.crypto.i18n.CryptoConfigMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class RevocationConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<RevocationConfig, RevocationConfigImpl> {

    /**
     * 
     */
    public RevocationConfigObjectTypeDescriptor () {
        super(RevocationConfig.class, RevocationConfigImpl.class, CryptoConfigMessages.BASE, "urn:agno3:objects:1.0:crypto:truststores:revocation"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull RevocationConfig getGlobalDefaults () {
        RevocationConfigMutable defaults = emptyInstance();
        defaults.setNetworkTimeout(new Duration(500));
        defaults.setCheckOnlyEndEntity(false);

        defaults.setCrlUpdateInterval(new Duration(12 * 60 * 60 * 1000));
        defaults.setCrlCheckLevel(CRLCheckLevel.OPPORTUNISTIC);
        defaults.setOnDemandCRLDownload(true);
        defaults.setOnDemandCRLCacheSize(16);

        defaults.setOcspCheckLevel(OCSPCheckLevel.PRIMARY);
        defaults.setOcspCacheSize(128);
        defaults.setUseTrustedResponder(false);
        defaults.setTrustedResponderCheckAll(false);
        return defaults;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull RevocationConfig newInstance () {
        return emptyInstance();
    }


    /**
     * @return an empty instance
     */
    public static @NonNull RevocationConfigMutable emptyInstance () {
        return new RevocationConfigImpl();
    }
}
