/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.terms;


import java.util.Comparator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.orchestrator.config.terms.TermsApplyType;
import eu.agno3.orchestrator.config.terms.TermsDefinition;
import eu.agno3.orchestrator.config.terms.TermsDefinitionImpl;
import eu.agno3.orchestrator.config.web.i18n.WebConfigurationMessages;
import eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;


/**
 * @author mbechler
 *
 */
@Named ( "termsConfigBean" )
@ApplicationScoped
public class TermsConfigBean extends AbstractConfigObjectBean<TermsDefinition, TermsDefinitionImpl> {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractBaseConfigObjectBean#getMessageBase()
     */
    @Override
    protected String getMessageBase () {
        return WebConfigurationMessages.BASE_PACKAGE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractBaseConfigObjectBean#getObjectType()
     */
    @Override
    protected Class<TermsDefinition> getObjectType () {
        return TermsDefinition.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getInstanceType()
     */
    @Override
    protected Class<TermsDefinitionImpl> getInstanceType () {
        return TermsDefinitionImpl.class;
    }


    public TermsApplyType[] getApplyTypes () {
        return TermsApplyType.values();
    }


    public String translateApplyType ( Object val ) {
        return translateEnumValue(TermsApplyType.class, val);
    }


    public String translateApplyDescription ( Object val ) {
        return translateEnumDescription(TermsApplyType.class, val);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractBaseConfigObjectBean#labelForInternal(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected String labelForInternal ( TermsDefinition obj ) {
        if ( obj == null ) {
            return null;
        }
        return obj.getTermsId();
    }


    /**
     * 
     * @return a comparator
     */
    public Comparator<TermsDefinition> getComparator () {
        return new TermsConfigComparator();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractBaseConfigObjectBean#cloneInternal(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected void cloneInternal ( ConfigContext<?, ?> ctx, TermsDefinitionImpl cloned, TermsDefinition obj, TermsDefinition def ) {
        cloned.setTermsId(obj.getTermsId());
        super.cloneDefault(ctx, cloned, obj, def);
    }

}
