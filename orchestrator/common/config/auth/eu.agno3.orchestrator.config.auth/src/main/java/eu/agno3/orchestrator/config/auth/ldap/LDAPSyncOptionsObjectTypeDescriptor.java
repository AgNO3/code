/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.auth.ldap;


import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.Duration;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.auth.AuthenticatorsConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.auth.i18n.AuthenticationConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class LDAPSyncOptionsObjectTypeDescriptor extends AbstractObjectTypeDescriptor<LDAPSyncOptions, LDAPSyncOptionsImpl> {

    /**
     * 
     */
    public LDAPSyncOptionsObjectTypeDescriptor () {
        super(LDAPSyncOptions.class, LDAPSyncOptionsImpl.class, AuthenticationConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getParentTypeName()
     */
    @Override
    public String getParentTypeName () {
        return AuthenticatorsConfigObjectTypeDescriptor.OBJECT_TYPE;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull LDAPSyncOptions newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull LDAPSyncOptions getGlobalDefaults () {
        LDAPSyncOptionsImpl so = new LDAPSyncOptionsImpl();
        so.setSynchronizeRemovals(false);
        so.setRemoveUsingUUID(true);
        so.setPageSize(128);
        so.setSyncInterval(Duration.standardMinutes(30));
        return so;
    }


    /**
     * @return empty instance
     */
    public static @NonNull LDAPSyncOptionsMutable emptyInstance () {
        return new LDAPSyncOptionsImpl();
    }

}
