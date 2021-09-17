/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto.truststores;


import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.orchestrator.config.crypto.i18n.CryptoConfigMessages;
import eu.agno3.orchestrator.config.crypto.truststore.CRLCheckLevel;
import eu.agno3.orchestrator.config.crypto.truststore.OCSPCheckLevel;
import eu.agno3.orchestrator.config.crypto.truststore.RevocationConfig;
import eu.agno3.orchestrator.config.crypto.truststore.RevocationConfigImpl;
import eu.agno3.orchestrator.config.crypto.truststore.RevocationConfigMutable;
import eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;


/**
 * @author mbechler
 *
 */
@Named ( "revocationConfigBean" )
@ApplicationScoped
public class RevocationConfigBean extends AbstractConfigObjectBean<RevocationConfig, RevocationConfigImpl> {

    public List<CRLCheckLevel> getCrlCheckLevels () {
        return Arrays.asList(CRLCheckLevel.values());
    }


    public List<OCSPCheckLevel> getOcspCheckLevels () {
        return Arrays.asList(OCSPCheckLevel.values());
    }


    public String translateCRLCheckLevel ( Object type ) {
        return translateEnumValue(CRLCheckLevel.class, type);
    }


    public String translateOCSPCheckLevel ( Object type ) {
        return translateEnumValue(OCSPCheckLevel.class, type);
    }


    public TrustedResponderCertificateWrapper certEntryWrapper ( RevocationConfigMutable cfg ) {
        return new TrustedResponderCertificateWrapper(cfg);
    }


    public boolean shouldShowOCSPSettings ( RevocationConfig current, RevocationConfig defaults, RevocationConfig enforced ) {
        if ( enforced != null && enforced.getOcspCheckLevel() != null ) {
            return enforced.getOcspCheckLevel() != OCSPCheckLevel.DISABLE;
        }

        if ( current != null && current.getOcspCheckLevel() != null ) {
            return current.getOcspCheckLevel() != OCSPCheckLevel.DISABLE;
        }

        if ( defaults != null && defaults.getOcspCheckLevel() != null ) {
            return defaults.getOcspCheckLevel() != OCSPCheckLevel.DISABLE;
        }

        return false;
    }


    public boolean shouldShowCRLSettings ( RevocationConfig current, RevocationConfig defaults, RevocationConfig enforced ) {
        if ( enforced != null && enforced.getCrlCheckLevel() != null ) {
            return enforced.getCrlCheckLevel() != CRLCheckLevel.DISABLE;
        }

        if ( current != null && current.getCrlCheckLevel() != null ) {
            return current.getCrlCheckLevel() != CRLCheckLevel.DISABLE;
        }

        if ( defaults != null && defaults.getCrlCheckLevel() != null ) {
            return defaults.getCrlCheckLevel() != CRLCheckLevel.DISABLE;
        }

        return false;
    }


    public boolean shouldShowTrustedResponder ( RevocationConfig current, RevocationConfig defaults, RevocationConfig enforced ) {

        if ( !this.shouldShowOCSPSettings(current, defaults, enforced) ) {
            return false;
        }

        if ( trustedResponderAvail(enforced) ) {
            return enforced.getUseTrustedResponder();
        }

        if ( trustedResponderAvail(current) ) {
            return current.getUseTrustedResponder();
        }

        if ( trustedResponderAvail(defaults) ) {
            return defaults.getUseTrustedResponder();
        }

        return false;
    }


    public boolean shouldShowOnDemandCRLSettings ( RevocationConfig current, RevocationConfig defaults, RevocationConfig enforced ) {

        if ( !this.shouldShowCRLSettings(current, defaults, enforced) ) {
            return false;
        }

        return hasOnDemandCRLSettings(current, defaults, enforced);
    }


    /**
     * @param current
     * @param defaults
     * @param enforced
     * @return
     */
    private static boolean hasOnDemandCRLSettings ( RevocationConfig current, RevocationConfig defaults, RevocationConfig enforced ) {
        if ( enforced != null && enforced.getOnDemandCRLDownload() != null ) {
            return enforced.getOnDemandCRLDownload();
        }

        if ( current != null && current.getOnDemandCRLDownload() != null ) {
            return current.getOnDemandCRLDownload();
        }

        if ( defaults != null && defaults.getOnDemandCRLDownload() != null ) {
            return defaults.getOnDemandCRLDownload();
        }

        return false;
    }


    private static boolean trustedResponderAvail ( RevocationConfig cfg ) {
        return cfg != null && cfg.getUseTrustedResponder() != null;
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
    protected Class<RevocationConfig> getObjectType () {
        return RevocationConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getInstanceType()
     */
    @Override
    protected Class<RevocationConfigImpl> getInstanceType () {
        return RevocationConfigImpl.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#cloneInternal(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected void cloneInternal ( ConfigContext<?, ?> ctx, RevocationConfigImpl cloned, RevocationConfig obj, RevocationConfig def ) {
        super.cloneDefault(ctx, cloned, obj, def);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#labelForInternal(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected String labelForInternal ( RevocationConfig obj ) {
        return null;
    }

}
