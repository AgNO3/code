/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.01.2016 by mbechler
 */
package eu.agno3.runtime.jsf.components.pwstrength;


import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.runtime.jsf.i18n.BaseMessages;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "javadoc" )
public class PasswordInput extends UIInput implements NamingContainer {

    private static final String PASSWORD = "password"; //$NON-NLS-1$
    private static final String OPTIONAL = "optional"; //$NON-NLS-1$
    private static final String PASSWORD_CONFIRM = "passwordConfirm"; //$NON-NLS-1$
    private static final String PASSWORD_ENTROPY = "passwordEntropy"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#getFamily()
     */
    @Override
    public String getFamily () {
        return UINamingContainer.COMPONENT_FAMILY;
    }


    public String getEscapedClientId () {
        return getClientId().replace(':', '_');
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#processValidators(javax.faces.context.FacesContext)
     */
    @Override
    public void processValidators ( FacesContext context ) {
        this.pushComponentToEL(context, this);
        try {
            super.processValidators(context);
        }
        finally {
            this.popComponentFromEL(context);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#processUpdates(javax.faces.context.FacesContext)
     */
    @Override
    public void processUpdates ( FacesContext context ) {
        this.pushComponentToEL(context, this);
        try {
            super.processUpdates(context);
        }
        finally {
            this.popComponentFromEL(context);
        }
    }


    public boolean isOptional () {
        return (Boolean) this.getStateHelper().eval(OPTIONAL, false);
    }


    public void setOptional ( boolean optional ) {
        this.getStateHelper().put(OPTIONAL, optional);
    }


    public String getPassword () {
        return (String) this.getStateHelper().eval(PASSWORD);
    }


    public void setPassword ( String pw ) {
        this.getStateHelper().put(PASSWORD, pw);
    }


    public String getPasswordConfirm () {
        return (String) this.getStateHelper().eval(PASSWORD_CONFIRM);
    }


    public void setPasswordConfirm ( String pwConfirm ) {
        this.getStateHelper().put(PASSWORD_CONFIRM, pwConfirm);
    }


    public void updateEntropyEstimate ( ActionEvent ev ) {
        setPasswordEntropy(getPasswordPolicyChecker().estimateEntropy(this.getPassword()));
    }


    public void setPasswordEntropy ( int estimateEntropy ) {
        this.getStateHelper().put(PASSWORD_ENTROPY, estimateEntropy);
    }


    public int getPasswordEntropy () {
        return (Integer) this.getStateHelper().eval(PASSWORD_ENTROPY, 0);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#resetValue()
     */
    @Override
    public void resetValue () {
        this.getStateHelper().remove(PASSWORD);
        this.getStateHelper().remove(PASSWORD_CONFIRM);
        this.getStateHelper().remove(PASSWORD_ENTROPY);
        super.resetValue();
    }


    /**
     * @return
     */
    private PasswordPolicyChecker getPasswordPolicyChecker () {
        PasswordPolicyChecker checker = (PasswordPolicyChecker) this.getValueExpression("checker") //$NON-NLS-1$
                .getValue(FacesContext.getCurrentInstance().getELContext());
        if ( checker == null ) {
            throw new FacesException("Missing checker"); //$NON-NLS-1$
        }
        return checker;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#updateModel(javax.faces.context.FacesContext)
     */
    @Override
    public void updateModel ( FacesContext context ) {

        String pw = getPassword();
        if ( StringUtils.isBlank(pw) ) {
            if ( !this.isOptional() ) {
                context.addMessage(
                    this.getClientId(context),
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, BaseMessages.get("pwstrength.missingPassword"), StringUtils.EMPTY)); //$NON-NLS-1$
                setValid(false);
            }
            else {
                setValue(null);
            }
        }
        else if ( !pw.equals(getPasswordConfirm()) ) {
            context.addMessage(
                this.getClientId(context),
                new FacesMessage(FacesMessage.SEVERITY_ERROR, BaseMessages.get("pwstrength.mismatchPassword"), StringUtils.EMPTY)); //$NON-NLS-1$
            setValid(false);
        }
        else {
            int entropy = getPasswordPolicyChecker().estimateEntropy(this.getPassword());
            if ( getEntropyLowerLimit() > 0 && entropy < getEntropyLowerLimit() ) {
                context.addMessage(
                    this.getClientId(context),
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, BaseMessages.get("pwstrength.insufficentStrenght"), StringUtils.EMPTY)); //$NON-NLS-1$
                setValid(false);
            }
            else {
                setValue(pw);
            }
        }

        super.updateModel(context);
    }


    public int getEntropyLowerLimit () {
        Integer min = (Integer) getAttributes().get("minimumEntropy"); //$NON-NLS-1$
        if ( min != null && min >= 0 ) {
            return min;
        }
        return getPasswordPolicyChecker().getEntropyLowerLimit();
    }


    public String getPasswordLabelString () {
        String label = (String) getAttributes().get("passwordLabel"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(label) ) {
            return label;
        }
        return BaseMessages.get("pwstrength.password.label"); //$NON-NLS-1$
    }


    public String getPasswordDescriptionString () {
        String desc = (String) getAttributes().get("passwordDescription"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(desc) ) {
            return desc;
        }
        return BaseMessages.get("pwstrength.password.description"); //$NON-NLS-1$
    }


    public String getConfirmLabelString () {
        String label = (String) getAttributes().get("confirmLabel"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(label) ) {
            return label;
        }
        return BaseMessages.get("pwstrength.confirm.label"); //$NON-NLS-1$
    }


    public String getConfirmDescriptionString () {
        String desc = (String) getAttributes().get("confirmDescription"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(desc) ) {
            return desc;
        }
        return BaseMessages.get("pwstrength.confirm.description"); //$NON-NLS-1$
    }

}
