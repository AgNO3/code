/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
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
public class FileshareUserTrustLevelConfigObjectTypeDescriptor extends
        AbstractObjectTypeDescriptor<FileshareUserTrustLevelConfig, FileshareUserTrustLevelConfigImpl> {

    private static final UUID SELF_REG_DEF_ID = UUID.fromString("3630202a-022e-4c18-a71e-860cc75dbdda"); //$NON-NLS-1$
    private static final UUID EXTERNAL_DEF_ID = UUID.fromString("e34af91a-a3c6-4aa9-85d3-89aaca8f9d15"); //$NON-NLS-1$
    private static final UUID LOCAL_DEF_ID = UUID.fromString("8cf61fc6-22b7-4b86-975b-eea3883f0f88"); //$NON-NLS-1$


    /**
     * 
     */
    public FileshareUserTrustLevelConfigObjectTypeDescriptor () {
        super(FileshareUserTrustLevelConfig.class, FileshareUserTrustLevelConfigImpl.class, FileshareConfigurationMessages.BASE_PACKAGE);
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
    public @NonNull FileshareUserTrustLevelConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull FileshareUserTrustLevelConfig getGlobalDefaults () {
        FileshareUserTrustLevelConfigImpl futl = new FileshareUserTrustLevelConfigImpl();

        futl.setLinkTrustLevel("EXTERNAL"); //$NON-NLS-1$
        futl.setMailTrustLevel("EXTERNAL"); //$NON-NLS-1$
        futl.setGroupTrustLevel("LOCAL"); //$NON-NLS-1$

        FileshareUserTrustLevelImpl selfRegistered = FileshareUserTrustLevelObjectTypeDescriptor.emptyInstance();
        selfRegistered.setId(SELF_REG_DEF_ID);
        selfRegistered.setColor("B22222"); //$NON-NLS-1$
        selfRegistered.setTrustLevelId("UNTRUSTED"); //$NON-NLS-1$
        selfRegistered.setMatchRoles(new HashSet<>(Arrays.asList("SELF_REGISTERED_USER"))); //$NON-NLS-1$
        selfRegistered.setMessages(Collections.singletonMap(Locale.ROOT, "Self registered user")); //$NON-NLS-1$

        FileshareUserTrustLevelImpl external = FileshareUserTrustLevelObjectTypeDescriptor.emptyInstance();
        external.setId(EXTERNAL_DEF_ID);
        external.setTrustLevelId("EXTERNAL"); //$NON-NLS-1$
        external.setColor("666666"); //$NON-NLS-1$
        external.setMatchRoles(new HashSet<>(Arrays.asList("INVITED_USER", //$NON-NLS-1$
            "EXTERNAL"))); //$NON-NLS-1$
        external.setMessages(Collections.singletonMap(Locale.ROOT, "External user")); //$NON-NLS-1$

        FileshareUserTrustLevelImpl local = FileshareUserTrustLevelObjectTypeDescriptor.emptyInstance();
        local.setId(LOCAL_DEF_ID);
        local.setTrustLevelId("LOCAL"); //$NON-NLS-1$
        local.setColor("003300"); //$NON-NLS-1$
        local.setMatchRoles(new HashSet<>(Arrays.asList("ADMIN_CREATED_USER", //$NON-NLS-1$
            "SYNCHRONIZED_USER"))); //$NON-NLS-1$
        local.setMessages(Collections.singletonMap(Locale.ROOT, "Local user")); //$NON-NLS-1$

        futl.setTrustLevels(new HashSet<>(Arrays.asList(selfRegistered, external, local)));
        return futl;
    }


    /**
     * @return empty instance
     */
    public static @NonNull FileshareUserTrustLevelConfigMutable emptyInstance () {
        return new FileshareUserTrustLevelConfigImpl();
    }

}
