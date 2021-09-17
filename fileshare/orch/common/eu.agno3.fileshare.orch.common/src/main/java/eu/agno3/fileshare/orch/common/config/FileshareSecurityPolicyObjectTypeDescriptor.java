/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.Duration;
import org.osgi.service.component.annotations.Component;

import eu.agno3.fileshare.orch.common.i18n.FileshareConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class FileshareSecurityPolicyObjectTypeDescriptor extends AbstractObjectTypeDescriptor<FileshareSecurityPolicy, FileshareSecurityPolicyImpl> {

    /**
     * 
     */
    public FileshareSecurityPolicyObjectTypeDescriptor () {
        super(FileshareSecurityPolicy.class, FileshareSecurityPolicyImpl.class, FileshareConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getParentTypeName()
     */
    @Override
    public String getParentTypeName () {
        return FileshareSecurityPolicyConfigObjectTypeDescriptor.TYPE_NAME;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull FileshareSecurityPolicy newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull FileshareSecurityPolicy getGlobalDefaults () {
        return defaultInstance();
    }


    /**
     * @return a instance with defaults
     */
    public @NonNull static FileshareSecurityPolicyImpl defaultInstance () {
        FileshareSecurityPolicyImpl sp = new FileshareSecurityPolicyImpl();
        sp.setSortPriority(0);
        sp.setTransportRequireEncryption(true);
        sp.setTransportMinKeySize(112);
        sp.setTransportMinHashBlockSize(160);
        sp.setTransportRequirePFS(false);
        sp.setAllowedShareTypes(EnumSet.allOf(GrantType.class));
        sp.setRequireTokenPassword(false);
        sp.setNoUserTokenPasswords(false);
        sp.setDisallowWebDAVAccess(false);
        sp.setMinTokenPasswordEntropy(40);
        sp.setEnableDefaultExpiration(false);
        sp.setEnableShareExpiration(true);
        sp.setRestrictExpirationDuration(false);
        sp.setRestrictShareLifetime(false);
        sp.setDefaultShareLifetime(Duration.standardDays(60));
        sp.setMaximumShareLifetime(Duration.standardDays(180));
        sp.setDefaultExpirationDuration(Duration.standardDays(180));
        sp.setMaximumExpirationDuration(Duration.standardDays(365));
        return sp;
    }


    /**
     * @return empty instance
     */
    public static @NonNull FileshareSecurityPolicyMutable emptyInstance () {
        return new FileshareSecurityPolicyImpl();
    }

}
