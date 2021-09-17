/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.auth.ldap;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.auth.i18n.AuthenticationConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.config.web.LDAPObjectConfigObjectTypeDescriptor;
import eu.agno3.orchestrator.config.web.LDAPSearchScope;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class LDAPAuthSchemaConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<LDAPAuthSchemaConfig, LDAPAuthSchemaConfigImpl> {

    /**
     * 
     */
    public LDAPAuthSchemaConfigObjectTypeDescriptor () {
        super(LDAPAuthSchemaConfig.class, LDAPAuthSchemaConfigImpl.class, AuthenticationConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getParentTypeName()
     */
    @Override
    public String getParentTypeName () {
        return LDAPAuthenticatorConfigObjectTypeDescriptor.TYPE_NAME;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull LDAPAuthSchemaConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull LDAPAuthSchemaConfig getGlobalDefaults () {
        LDAPAuthSchemaConfigImpl las = new LDAPAuthSchemaConfigImpl();
        las.setRecursiveResolveGroups(false);
        las.setUseForwardGroups(false);
        las.setReferencesAreDNs(true);
        las.setGroupSchema(LDAPObjectConfigObjectTypeDescriptor.emptyInstance());
        las.getGroupSchema().setScope(LDAPSearchScope.SUB);
        las.getGroupSchema().setAttributeStyle("LDAP"); //$NON-NLS-1$
        las.setUserSchema(LDAPObjectConfigObjectTypeDescriptor.emptyInstance());
        las.getUserSchema().setAttributeStyle("LDAP"); //$NON-NLS-1$
        las.getUserSchema().setScope(LDAPSearchScope.SUB);
        return las;
    }


    /**
     * @return empty instance
     */
    public static @NonNull LDAPAuthSchemaConfigMutable emptyInstance () {
        LDAPAuthSchemaConfigImpl las = new LDAPAuthSchemaConfigImpl();
        las.setGroupSchema(LDAPObjectConfigObjectTypeDescriptor.emptyInstance());
        las.setUserSchema(LDAPObjectConfigObjectTypeDescriptor.emptyInstance());
        return las;
    }

}
