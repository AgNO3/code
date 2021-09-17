/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.09.2016 by mbechler
 */
package eu.agno3.runtime.security.web.gui.terms;


import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.runtime.jsf.components.ResettableComponent;
import eu.agno3.runtime.security.terms.TermsDefinition;
import eu.agno3.runtime.security.web.gui.LoginMessages;


/**
 * @author mbechler
 *
 */
public class TermsAcceptance extends UIInput implements NamingContainer, ResettableComponent {

    private static final Serializable ACCEPTANCE_MAP = "acceptanceMap"; //$NON-NLS-1$


    /**
     * 
     * @return acceptance map
     */
    @SuppressWarnings ( "unchecked" )
    public Map<String, Boolean> getAcceptanceMap () {
        Object object = getStateHelper().get(ACCEPTANCE_MAP);
        if ( ! ( object instanceof Map ) ) {
            object = new HashMap<>();
            getStateHelper().put(ACCEPTANCE_MAP, object);
        }
        return (Map<String, Boolean>) object;
    }


    /**
     * @param def
     * @return a wrapper to get/set acceptance of specific terms
     */
    public AcceptanceWrapper getAcceptance ( TermsDefinition def ) {
        return new AcceptanceWrapper(def.getId(), this);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.ResettableComponent#resetComponent()
     */
    @Override
    public boolean resetComponent () {
        getStateHelper().remove(ACCEPTANCE_MAP);
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#getFamily()
     */
    @Override
    public String getFamily () {
        return UINamingContainer.COMPONENT_FAMILY;
    }


    @Override
    public void updateModel ( FacesContext context ) {
        Object object = this.getAttributes().get("toAccept"); //$NON-NLS-1$
        if ( ! ( object instanceof Collection ) ) {
            setValue(Collections.EMPTY_LIST);
            super.updateModel(context);
            return;
        }
        @SuppressWarnings ( "unchecked" )
        Collection<TermsDefinition> toAcceptDefs = (Collection<TermsDefinition>) object;
        Set<String> toAcceptIds = toAcceptDefs.stream().map(x -> x.getId()).collect(Collectors.toSet());
        Map<String, Boolean> accepted = getAcceptanceMap();
        if ( accepted.keySet().containsAll(toAcceptIds) ) {
            boolean allAccepted = true;
            for ( String toAccept : toAcceptIds ) {
                if ( !accepted.getOrDefault(toAccept, false) ) {
                    allAccepted = false;
                    break;
                }
            }

            if ( allAccepted ) {

                setValue(Collections.unmodifiableCollection(toAcceptIds));
                setValid(true);
                super.updateModel(context);
                return;
            }
        }

        setValue(Collections.EMPTY_LIST);
        context.addMessage(this.getClientId(context), new FacesMessage(
            FacesMessage.SEVERITY_ERROR,
            LoginMessages.format("terms.needAcceptance"), //$NON-NLS-1$
            StringUtils.EMPTY));
        setValid(false);
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
}
