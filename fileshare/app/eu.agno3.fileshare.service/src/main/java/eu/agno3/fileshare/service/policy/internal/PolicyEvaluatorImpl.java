/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.policy.internal;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.ServletRequest;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.GrantAuthenticationRequiredException;
import eu.agno3.fileshare.exceptions.PolicyNotFoundException;
import eu.agno3.fileshare.exceptions.PolicyNotFulfilledException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.tokens.AccessToken;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.PolicyEvaluator;
import eu.agno3.fileshare.service.config.PolicyConfiguration;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.i18n.I18NUtil;
import eu.agno3.runtime.i18n.ResourceBundleService;


/**
 * @author mbechler
 *
 */
@Component ( service = PolicyEvaluator.class )
public class PolicyEvaluatorImpl implements PolicyEvaluator {

    private static final Logger log = Logger.getLogger(PolicyEvaluatorImpl.class);

    private DefaultServiceContext sctx;
    private AccessControlService accessControl;
    private ResourceBundleService i18n;
    private Set<PolicyRule> boundRuleSet = new HashSet<>();
    private List<PolicyRule> rules = Collections.EMPTY_LIST;


    @Reference
    protected synchronized void setContext ( DefaultServiceContext ctx ) {
        this.sctx = ctx;
    }


    protected synchronized void unsetContext ( DefaultServiceContext ctx ) {
        if ( this.sctx == ctx ) {
            this.sctx = null;
        }
    }


    @Reference
    protected synchronized void setAccessControl ( AccessControlService acs ) {
        this.accessControl = acs;
    }


    protected synchronized void unsetAccessControl ( AccessControlService acs ) {
        if ( this.accessControl == acs ) {
            this.accessControl = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindPolicyRule ( PolicyRule pr ) {
        this.boundRuleSet.add(pr);
        this.rules = createRules(this.boundRuleSet);
    }


    protected synchronized void unbindPolicyRule ( PolicyRule pr ) {
        this.boundRuleSet.remove(pr);
        this.rules = createRules(this.boundRuleSet);
    }


    @Reference
    protected synchronized void setResourceBundleService ( ResourceBundleService rbs ) {
        this.i18n = rbs;
    }


    protected synchronized void unsetResourceBundleService ( ResourceBundleService rbs ) {
        if ( this.i18n == rbs ) {
            this.i18n = null;
        }
    }


    private static List<PolicyRule> createRules ( Set<PolicyRule> ruleSet ) {
        List<PolicyRule> r = new ArrayList<>(ruleSet);
        Collections.sort(r, new PolicyRuleComparator());
        return r;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.PolicyEvaluator#checkPolicy(eu.agno3.fileshare.vfs.VFSContext,
     *      eu.agno3.fileshare.model.VFSEntity, javax.servlet.ServletRequest)
     */
    @Override
    public void checkPolicy ( VFSContext v, VFSEntity e, ServletRequest req ) throws PolicyNotFulfilledException {
        PolicyViolation violation = this.isPolicyFulfilled(v, e, req);
        if ( violation != null ) {
            throw new PolicyNotFulfilledException(violation);
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.PolicyEvaluator#isPolicyFulfilled(eu.agno3.fileshare.vfs.VFSContext,
     *      eu.agno3.fileshare.model.VFSEntity, javax.servlet.ServletRequest)
     */
    @Override
    public PolicyViolation isPolicyFulfilled ( VFSContext v, VFSEntity entity, ServletRequest req ) {

        if ( entity == null ) {
            return new PolicyViolation("entity.null"); //$NON-NLS-1$
        }

        SecurityLabel label = entity.getSecurityLabel();

        if ( label == null ) {
            log.debug("Entity is not labeled " + entity); //$NON-NLS-1$
            return new PolicyViolation("entity.notLabeled"); //$NON-NLS-1$
        }

        return isPolicyFulfilledInternal(v, req, label.getLabel(), entity);
    }


    /**
     * @param req
     * @param policyConfig
     * @param label
     * @param entity
     * @return
     */
    private PolicyViolation isPolicyFulfilledInternal ( VFSContext v, ServletRequest req, String l, VFSEntity entity ) {
        PolicyConfiguration policyConfig;
        try {
            policyConfig = this.sctx.getConfigurationProvider().getSecurityPolicyConfiguration().getPolicy(l);
        }
        catch ( PolicyNotFoundException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug("No policy found for " + l); //$NON-NLS-1$
            }
            return new PolicyViolation("entity.notLabeled", l); //$NON-NLS-1$
        }

        PolicyEvaluationContext ctx = makeEvaluationContext(v, entity, req);
        for ( PolicyRule rule : getPolicyRules(l) ) {
            PolicyViolation violation = rule.isFulfilledForAccess(policyConfig, ctx);
            if ( violation != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Rejected for label %s by policy %s with %s", l, rule.getClass().getName(), violation.getKey())); //$NON-NLS-1$
                }
                return violation;
            }
        }

        return null;
    }


    @Override
    public PolicyViolation isPolicyFulfilled ( SecurityLabel label, ServletRequest req ) {
        return isPolicyFulfilledInternal(null, req, label.getLabel(), null);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.PolicyEvaluator#isPolicyFulfilled(java.lang.String,
     *      javax.servlet.ServletRequest)
     */
    @Override
    public PolicyViolation isPolicyFulfilled ( String label, ServletRequest req ) {
        return isPolicyFulfilledInternal(null, req, label, null);
    }


    /**
     * @param label
     * @param req
     * @return
     */
    private PolicyEvaluationContext makeEvaluationContext ( VFSContext v, VFSEntity e, ServletRequest req ) {
        AccessToken tokenAuthValue = this.accessControl.getTokenAuthValue();

        Grant grant = null;
        try {
            grant = this.accessControl.getTokenAuthGrant(v, e);
        }
        catch ( GrantAuthenticationRequiredException ex ) {
            log.warn("Failed to get token auth grant, as password is required but not available", ex); //$NON-NLS-1$
        }
        User currentUser = getCurrentUser();
        boolean owner = this.accessControl.isOwner(v, e);

        if ( e != null && !owner && grant == null ) {
            // this should be a subject grant, get one (even if it is not the right one)
            try {
                grant = this.accessControl.getAnySubjectGrant(v, e);
            }
            catch ( AuthenticationException ex ) {
                log.warn("Failed to get subject grant", ex); //$NON-NLS-1$
            }
        }

        return new PolicyEvaluationContextImpl(e, currentUser, tokenAuthValue, grant, SecurityUtils.getSubject(), req, owner);
    }


    /**
     * @param currentUser
     * @return
     */
    private User getCurrentUser () {
        try {
            if ( this.accessControl.isUserAuthenticated() ) {
                return this.accessControl.getCurrentUserCachable();
            }
        }
        catch (
            UserNotFoundException |
            AuthenticationException e ) {
            log.debug("Failed to get current user", e); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * @param label
     * @return
     */
    private List<PolicyRule> getPolicyRules ( String label ) {
        return this.rules;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.PolicyEvaluator#getViolationMessage(eu.agno3.fileshare.model.PolicyViolation,
     *      java.util.Locale)
     */
    @Override
    public String getViolationMessage ( PolicyViolation violation, Locale l ) {
        ResourceBundle bundle = this.i18n.getBundle("eu.agno3.fileshare.service.policy.rules", l, this.getClass().getClassLoader()); //$NON-NLS-1$
        return I18NUtil.format(bundle, l, violation.getKey(), violation.getArguments());
    }
}
