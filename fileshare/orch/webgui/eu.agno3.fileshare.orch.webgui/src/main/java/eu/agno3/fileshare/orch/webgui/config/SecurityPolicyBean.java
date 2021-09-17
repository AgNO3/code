/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.config;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.fileshare.orch.common.config.FileshareSecurityPolicy;
import eu.agno3.fileshare.orch.common.config.FileshareSecurityPolicyConfig;
import eu.agno3.fileshare.orch.common.config.FileshareSecurityPolicyImpl;
import eu.agno3.fileshare.orch.common.config.FileshareUserLabelRule;
import eu.agno3.fileshare.orch.common.config.FileshareUserLabelRuleImpl;
import eu.agno3.fileshare.orch.common.config.GrantType;
import eu.agno3.fileshare.orch.common.i18n.FileshareConfigurationMessages;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;
import eu.agno3.orchestrator.server.webgui.util.completer.Completer;
import eu.agno3.orchestrator.server.webgui.util.completer.EmptyCompleter;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "fs_securityPolicyBean" )
public class SecurityPolicyBean extends AbstractConfigObjectBean<FileshareSecurityPolicy, FileshareSecurityPolicyImpl> {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getMessageBase()
     */
    @Override
    protected String getMessageBase () {
        return FileshareConfigurationMessages.BASE_PACKAGE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getObjectType()
     */
    @Override
    protected Class<FileshareSecurityPolicy> getObjectType () {
        return FileshareSecurityPolicy.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getInstanceType()
     */
    @Override
    protected Class<FileshareSecurityPolicyImpl> getInstanceType () {
        return FileshareSecurityPolicyImpl.class;
    }


    /**
     * @return comparator
     */
    public Comparator<FileshareSecurityPolicy> getComparator () {
        return new SecurityPolicyComparator();
    }


    /**
     * 
     * @return available grant types
     */
    public GrantType[] getGrantTypes () {
        return GrantType.values();
    }


    /**
     * 
     * @param o
     * @return translated enum value
     */
    public String translateGrantType ( Object o ) {
        return translateEnumValue(GrantType.class, o);
    }


    /**
     * @param wr
     * @return a trust level completer
     */
    @SuppressWarnings ( "unchecked" )
    public Completer<String> getCompleter ( OuterWrapper<?> wr ) {
        if ( wr == null ) {
            return new EmptyCompleter();
        }

        OuterWrapper<?> outerWrapper = wr.get("urn:agno3:objects:1.0:fileshare:securityPolicies"); //$NON-NLS-1$
        if ( outerWrapper == null ) {
            return new EmptyCompleter();
        }

        return new Completer<String>() {

            @Override
            public List<String> complete ( String query ) {

                List<String> res = new ArrayList<>();

                List<FileshareSecurityPolicy> pols;
                try {
                    FileshareSecurityPolicyConfig current = (FileshareSecurityPolicyConfig) outerWrapper.getEditor().getCurrent();

                    if ( current == null || current.getPolicies().isEmpty() ) {
                        FileshareSecurityPolicyConfig defaults = (FileshareSecurityPolicyConfig) outerWrapper.getEditor().getDefaults();

                        if ( defaults == null || defaults.getPolicies().isEmpty() ) {
                            return res;
                        }

                        pols = new ArrayList<>(defaults.getPolicies());
                    }
                    else {
                        pols = new ArrayList<>(current.getPolicies());
                    }

                }
                catch (
                    ModelObjectNotFoundException |
                    ModelServiceException |
                    GuiWebServiceException |
                    UndeclaredThrowableException e ) {
                    return res;
                }

                String uc = query != null ? query.toUpperCase() : null;

                Collections.sort(pols, new SecurityPolicyComparator());

                for ( FileshareSecurityPolicy tl : pols ) {
                    if ( query != null && ( tl.getLabel() != null && tl.getLabel().toUpperCase().startsWith(uc) ) ) {
                        res.add(tl.getLabel());
                    }
                }
                return res;
            }

        };
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#cloneInternal(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected void cloneInternal ( ConfigContext<?, ?> ctx, FileshareSecurityPolicyImpl cloned, FileshareSecurityPolicy local,
            FileshareSecurityPolicy def ) {
        cloned.setLabel(local.getLabel());
        super.cloneDefault(ctx, cloned, local, def);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#labelForInternal(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected String labelForInternal ( FileshareSecurityPolicy obj ) {
        return obj.getLabel();
    }


    /**
     * 
     * @return new instance
     */
    public FileshareUserLabelRule makeUserLabelRule () {
        return new FileshareUserLabelRuleImpl();
    }
}
