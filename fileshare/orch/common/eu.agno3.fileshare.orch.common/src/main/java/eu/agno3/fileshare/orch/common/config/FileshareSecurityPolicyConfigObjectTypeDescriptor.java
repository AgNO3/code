/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.fileshare.orch.common.i18n.FileshareConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class FileshareSecurityPolicyConfigObjectTypeDescriptor
        extends AbstractObjectTypeDescriptor<FileshareSecurityPolicyConfig, FileshareSecurityPolicyConfigImpl> {

    /**
     * 
     */
    private static final String UNCLASSIFIED_LABEL = "UNCLASSIFIED"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String TYPE_NAME = "urn:agno3:objects:1.0:fileshare:securityPolicies"; //$NON-NLS-1$

    private static final UUID UNCLASSIFIED_ID = UUID.fromString("768f13f6-c723-4c77-b5c1-c0290bc560ab"); //$NON-NLS-1$
    private static final UUID CLASSIFIED_ID = UUID.fromString("45b3d679-53b5-4f5e-9ebc-ee2c8cb36cb4"); //$NON-NLS-1$
    private static final UUID SECRET_ID = UUID.fromString("c1f3dcaf-c0f4-46fa-88ed-3b9671840b05"); //$NON-NLS-1$


    /**
     * 
     */
    public FileshareSecurityPolicyConfigObjectTypeDescriptor () {
        super(FileshareSecurityPolicyConfig.class, FileshareSecurityPolicyConfigImpl.class, FileshareConfigurationMessages.BASE_PACKAGE);
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
    public @NonNull FileshareSecurityPolicyConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull FileshareSecurityPolicyConfig getGlobalDefaults () {
        FileshareSecurityPolicyConfigImpl spc = new FileshareSecurityPolicyConfigImpl();
        spc.setDefaultEntityLabel(UNCLASSIFIED_LABEL);
        spc.setDefaultSharePasswordBits(64);

        FileshareSecurityPolicy unclassified = defaultUnclassifiedPolicy();
        FileshareSecurityPolicy classified = defaultClassifiedPolicy();
        FileshareSecurityPolicy secret = defaultSecretPolicy();

        spc.getPolicies().add(unclassified);
        spc.getPolicies().add(classified);
        spc.getPolicies().add(secret);
        return spc;
    }


    /**
     * @return
     */
    private static FileshareSecurityPolicy defaultUnclassifiedPolicy () {
        FileshareSecurityPolicyImpl unclassified = FileshareSecurityPolicyObjectTypeDescriptor.defaultInstance();
        unclassified.setId(UNCLASSIFIED_ID);
        unclassified.setLabel(UNCLASSIFIED_LABEL);
        unclassified.setSortPriority(0);
        return unclassified;
    }


    /**
     * @return
     */
    private static FileshareSecurityPolicy defaultClassifiedPolicy () {
        FileshareSecurityPolicyImpl classified = FileshareSecurityPolicyObjectTypeDescriptor.defaultInstance();
        classified.setId(CLASSIFIED_ID);
        classified.setLabel("CLASSIFIED"); //$NON-NLS-1$
        classified.setSortPriority(50);
        classified.setTransportRequireEncryption(true);
        classified.setTransportRequirePFS(true);
        classified.setTransportMinHashBlockSize(256);
        classified.setTransportMinKeySize(128);
        classified.setAllowedShareTypes(EnumSet.of(GrantType.SUBJECT, GrantType.MAIL));
        classified.setNoUserTokenPasswords(true);
        classified.setRequireTokenPassword(true);
        classified.setMinTokenPasswordEntropy(60);
        return classified;
    }


    /**
     * @return
     */
    private static FileshareSecurityPolicy defaultSecretPolicy () {
        FileshareSecurityPolicyImpl secret = FileshareSecurityPolicyObjectTypeDescriptor.defaultInstance();
        secret.setId(SECRET_ID);
        secret.setLabel("SECRET"); //$NON-NLS-1$
        secret.setSortPriority(100);
        secret.setTransportRequireEncryption(true);
        secret.setTransportRequirePFS(true);
        secret.setTransportMinHashBlockSize(256);
        secret.setTransportMinKeySize(128);
        secret.setMinTokenPasswordEntropy(60);
        secret.setNoUserTokenPasswords(true);
        secret.setRequireTokenPassword(true);
        secret.setAllowedShareTypes(EnumSet.of(GrantType.SUBJECT));
        secret.setRequireAnyRole(new HashSet<>(Arrays.asList(
            "ADMIN_CREATED_USER", //$NON-NLS-1$
            "SYNCHRONIZED_USER"))); //$NON-NLS-1$
        return secret;
    }


    /**
     * @return empty instance
     */
    public static @NonNull FileshareSecurityPolicyConfigMutable emptyInstance () {
        return new FileshareSecurityPolicyConfigImpl();
    }

}
