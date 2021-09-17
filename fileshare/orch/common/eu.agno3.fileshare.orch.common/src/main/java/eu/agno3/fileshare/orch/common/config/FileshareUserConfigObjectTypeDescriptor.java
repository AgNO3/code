/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Collections;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.fileshare.orch.common.i18n.FileshareConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.config.terms.TermsConfigurationObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class FileshareUserConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<FileshareUserConfig, FileshareUserConfigImpl> {

    /**
     * 
     */
    public static final String TYPE_NAME = "urn:agno3:objects:1.0:fileshare:user"; //$NON-NLS-1$


    /**
     * 
     */
    public FileshareUserConfigObjectTypeDescriptor () {
        super(FileshareUserConfig.class, FileshareUserConfigImpl.class, FileshareConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getParentTypeName()
     */
    @Override
    public String getParentTypeName () {
        return FileshareConfigurationObjectTypeDescriptor.TYPE_NAME;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull FileshareUserConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull FileshareUserConfig getGlobalDefaults () {
        FileshareUserConfigImpl fuc = new FileshareUserConfigImpl();
        fuc.setDefaultRoles(Collections.singleton("DEFAULT_USER")); //$NON-NLS-1$
        fuc.setNoSubjectRootRoles(Collections.singleton("SELF_REGISTERED_USER")); //$NON-NLS-1$
        return fuc;
    }


    /**
     * @return empty instance
     */
    public static @NonNull FileshareUserConfigMutable emptyInstance () {
        FileshareUserConfigImpl fuc = new FileshareUserConfigImpl();
        fuc.setQuotaConfig(FileshareUserQuotaConfigObjectTypeDescriptor.emptyInstance());
        fuc.setSelfServiceConfig(FileshareUserSelfServiceConfigObjectTypeDescriptor.emptyInstance());
        fuc.setUserTrustLevelConfig(FileshareUserTrustLevelConfigObjectTypeDescriptor.emptyInstance());
        fuc.setTermsConfig(TermsConfigurationObjectTypeDescriptor.emptyInstance());
        return fuc;
    }

}
