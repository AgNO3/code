/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.12.2013 by mbechler
 */
package eu.agno3.runtime.jsf.components.selectoradd;


import javax.el.ValueExpression;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;


/**
 * @author mbechler
 * 
 */
public class SelectOrAdd extends UIInput implements NamingContainer {

    private static final Logger log = Logger.getLogger(SelectOrAdd.class);

    private UIComponent selectComponent;


    /**
     * Set select value
     * 
     * @param ev
     */
    public void setSelectedValue ( SelectEvent ev ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Set value " + ev); //$NON-NLS-1$
        }
        this.setValue(ev.getObject());
        UISelectOne inputField = (UISelectOne) this.getSelectComponent();
        inputField.resetValue();
    }


    /**
     * Evaluate itemLabel from outer component
     * 
     * @return the itemLabel
     */
    public String proxySelectLabel () {
        ValueExpression e = this.getValueExpression("itemLabel"); //$NON-NLS-1$

        if ( e == null ) {
            return null;
        }
        return (String) e.getValue(FacesContext.getCurrentInstance().getELContext());
    }


    /**
     * Evaluate itemValue from outer component
     * 
     * @return the itemValue
     */
    public Object proxySelectValue () {
        ValueExpression e = this.getValueExpression("itemValue"); //$NON-NLS-1$

        if ( e == null ) {
            return null;
        }
        return e.getValue(FacesContext.getCurrentInstance().getELContext());
    }


    /**
     * @return the autoComplete component
     */
    public UIComponent getSelectComponent () {
        return this.selectComponent;
    }


    /**
     * @param selectComponent
     *            the select one menu component to set
     */
    public void setSelectComponent ( UIComponent selectComponent ) {
        this.selectComponent = selectComponent;
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
