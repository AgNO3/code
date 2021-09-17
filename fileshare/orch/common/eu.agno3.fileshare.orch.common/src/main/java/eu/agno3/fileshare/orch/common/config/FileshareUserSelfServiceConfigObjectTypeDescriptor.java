/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Arrays;
import java.util.HashSet;

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
public class FileshareUserSelfServiceConfigObjectTypeDescriptor extends
        AbstractObjectTypeDescriptor<FileshareUserSelfServiceConfig, FileshareUserSelfServiceConfigImpl> {

    /**
     * 
     */
    public FileshareUserSelfServiceConfigObjectTypeDescriptor () {
        super(FileshareUserSelfServiceConfig.class, FileshareUserSelfServiceConfigImpl.class, FileshareConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getParentTypeName()
     */
    @Override
    public String getParentTypeName () {
        return FileshareUserConfigObjectTypeDescriptor.TYPE_NAME;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull FileshareUserSelfServiceConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull FileshareUserSelfServiceConfig getGlobalDefaults () {
        FileshareUserSelfServiceConfigImpl fuss = new FileshareUserSelfServiceConfigImpl();
        fuss.setRegistrationEnabled(false);
        fuss.setRegistrationTokenLifetime(Duration.standardDays(7));
        fuss.setRegistrationUserRoles(new HashSet<>(Arrays.asList("SELF_REGISTERED_USER"))); //$NON-NLS-1$
        fuss.setRegistrationUserExpires(false);

        fuss.setInvitationEnabled(false);
        fuss.setInvitationTokenLifetime(Duration.standardDays(14));
        fuss.setInvitationUserRoles(new HashSet<>(Arrays.asList("EXTERNAL_USER", //$NON-NLS-1$
            "INVITED_USER"))); //$NON-NLS-1$
        fuss.setAllowInvitingUserExtension(true);
        fuss.setTrustInvitedUserNames(true);
        fuss.setInvitationUserExpires(false);

        fuss.setLocalPasswordRecoveryEnabled(true);
        fuss.setPasswordRecoveryTokenLifetime(Duration.standardHours(4));
        return fuss;
    }


    /**
     * @return empty instance
     */
    public static @NonNull FileshareUserSelfServiceConfigMutable emptyInstance () {
        return new FileshareUserSelfServiceConfigImpl();
    }

}
