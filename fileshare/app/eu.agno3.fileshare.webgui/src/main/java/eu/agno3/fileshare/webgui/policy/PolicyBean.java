/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.policy;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletRequest;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.tree.EntityTreeNode;
import eu.agno3.runtime.i18n.I18NUtil;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "policyBean" )
public class PolicyBean {

    // caching per session, the parameters should not change
    @Inject
    private PolicyCache cache;

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * @param label
     * @return whether this policy for the given label is fulfilled
     */
    public boolean isPolicyFulfilled ( SecurityLabel label ) {

        return getViolation(label) == null;
    }


    /**
     * @param o
     * @return whether the nodes policy is fulfilled
     */
    public boolean isNodePolicyFulfilled ( Object o ) {
        if ( ! ( o instanceof EntityTreeNode ) ) {
            return false;
        }

        EntityTreeNode en = (EntityTreeNode) o;
        if ( en.getAttachedObject() != null ) {
            return isPolicyFulfilled(en.getAttachedObject().getSecurityLabel());
        }
        return false;
    }


    /**
     * 
     * @param o
     * @return whether the entity policy is fulfilled
     */
    public boolean isEntityPolicyFulfilled ( Object o ) {
        if ( ! ( o instanceof VFSEntity ) ) {
            return false;
        }

        return isPolicyFulfilled( ( (VFSEntity) o ).getSecurityLabel());
    }


    /**
     * @param label
     * @return the policy violation if the policy is not fulfilled
     */
    public PolicyViolation getViolation ( SecurityLabel label ) {

        if ( label == null ) {
            return new PolicyViolation("noLabel"); //$NON-NLS-1$
        }
        String strLabel = label.getLabel();
        return getViolationForString(strLabel);
    }


    /**
     * @param label
     * @return the violation for the given label string
     */
    public PolicyViolation getViolationForString ( String label ) {
        if ( StringUtils.isBlank(label) ) {
            return new PolicyViolation("noLabel"); //$NON-NLS-1$
        }

        if ( this.cache.getCache().containsKey(label) ) {
            return this.cache.getCache().get(label);
        }

        try {
            PolicyViolation violation = this.fsp.getEntityService()
                    .getPolicyViolation(label, (ServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
            this.cache.getCache().put(label, violation);
            return violation;
        }
        catch ( UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
            return new PolicyViolation("error"); //$NON-NLS-1$
        }
    }


    /**
     * @param label
     * @return a descriptive text for the policy violation
     */
    public String getViolationMessage ( SecurityLabel label ) {
        PolicyViolation violation = getViolation(label);

        if ( violation == null ) {
            return StringUtils.EMPTY;
        }

        return getPolicyViolationMessage(violation);
    }


    /**
     * @param violation
     * @return a violation message for the given policy violation
     */
    public String getPolicyViolationMessage ( PolicyViolation violation ) {
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        ResourceBundle bundle = this.fsp.getResouceBundleService()
                .getBundle("eu.agno3.fileshare.service.policy.rules", locale, this.getClass().getClassLoader()); //$NON-NLS-1$

        return I18NUtil.format(bundle, locale, violation.getKey(), violation.getArguments());
    }


    /**
     * 
     * @param label
     * @param fallbackMessage
     * @return the violation message or an alternative text
     */
    public String getViolationMessageOr ( SecurityLabel label, String fallbackMessage ) {
        if ( this.isPolicyFulfilled(label) ) {
            return fallbackMessage;
        }
        return getViolationMessage(label);
    }


    /**
     * 
     */
    public void flushCache () {
        this.cache.getCache().clear();
    }

}
