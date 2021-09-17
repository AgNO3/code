/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.components;


import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import javax.el.MethodExpression;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;

import org.primefaces.component.selectoneradio.SelectOneRadio;

import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantPermission;


/**
 * @author mbechler
 *
 */
public class GrantPermissionEditor extends UIInput implements NamingContainer {

    private static final Serializable PERMISSIONS = "perms"; //$NON-NLS-1$

    private static final int READ_MASK = 1 + 2;
    private static final int WRITE_MASK = 4 + 8 + 16;


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
     * @see javax.faces.component.UIInput#resetValue()
     */
    @Override
    public void resetValue () {
        this.getStateHelper().put(PERMISSIONS, GrantPermission.toInt(getPermissionValue()));
        UIComponent comp = this.getFacet(COMPOSITE_FACET_NAME);
        for ( UIComponent child : comp.getChildren() ) {
            if ( child instanceof SelectOneRadio ) {
                SelectOneRadio radio = (SelectOneRadio) child;
                radio.resetValue();
            }
        }

        super.resetValue();
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


    /**
     * @param ev
     */
    public void reset ( ActionEvent ev ) {
        this.getStateHelper().put(PERMISSIONS, GrantPermission.toInt(getPermissionValue()));
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#updateModel(javax.faces.context.FacesContext)
     */
    @Override
    public void updateModel ( FacesContext context ) {
        Set<GrantPermission> permissions = toPermissions();
        if ( this.getSaveListener() != null && getGrantValue() != null ) {
            this.getSaveListener().invoke(context.getELContext(), new Object[] {
                getGrantValue(), permissions
            });
        }
        else if ( getGrantValue() != null ) {
            getGrantValue().setPermissions(permissions);
        }
        else {
            setValue(permissions);
        }
        super.updateModel(context);
    }


    /**
     * @return
     */
    private Set<GrantPermission> toPermissions () {
        return GrantPermission.fromInt(this.getPermissions());
    }


    /**
     * @return the write permission integer
     */
    public String getWritePermissions () {
        String res = String.valueOf(getPermissions() & WRITE_MASK);
        return res;
    }


    /**
     * @return the read permission integer
     */
    public String getReadPermissions () {
        String res = String.valueOf(getPermissions() & READ_MASK);
        return res;
    }


    /**
     * @return
     */
    private int getPermissions () {
        int res = (int) this.getStateHelper().eval(PERMISSIONS, GrantPermission.toInt(getPermissionValue()));

        return res;
    }


    /**
     * 
     * @param perms
     */
    public void setReadPermissions ( String perms ) {
        if ( perms == null ) {
            return;
        }

        setPermissions( ( getPermissions() & ~READ_MASK ) | ( Integer.valueOf(perms) & READ_MASK ));
    }


    /**
     * 
     * @param perms
     */
    public void setWritePermissions ( String perms ) {
        if ( perms == null ) {
            return;
        }
        int ordinal = ( getPermissions() & ~WRITE_MASK ) | ( Integer.valueOf(perms) & WRITE_MASK );

        if ( ( ordinal & WRITE_MASK ) == 0 || ( ordinal & WRITE_MASK & 16 ) > 0 ) {
            ordinal |= 3;
        }

        setPermissions(ordinal);
    }


    /**
     * @param perms
     */
    public void setPermissions ( int perms ) {
        this.getStateHelper().put(PERMISSIONS, perms);
    }


    /**
     * 
     * @return whether any write permission is present
     */
    public boolean disableReadPerms () {
        return ( ( this.getPermissions() & WRITE_MASK ) == 0 || ( this.getPermissions() & WRITE_MASK & 16 ) > 0 )
                && ( this.getPermissions() & READ_MASK ) == 3;
    }


    /**
     * 
     * @param ev
     */
    public void writeSelected ( AjaxBehaviorEvent ev ) {
        // ignore
    }


    /**
     * 
     * @param ev
     */
    public void readSelected ( AjaxBehaviorEvent ev ) {
        // ignore
    }


    /**
     * @return
     */
    @SuppressWarnings ( "unchecked" )
    private Set<GrantPermission> getPermissionValue () {
        if ( this.getGrantValue() != null ) {
            return this.getGrantValue().getPermissions();
        }
        if ( ! ( this.getValue() instanceof Set ) ) {
            return Collections.EMPTY_SET;
        }
        return (Set<GrantPermission>) this.getValue();
    }


    private Grant getGrantValue () {
        return (Grant) this.getAttributes().get("grant"); //$NON-NLS-1$
    }


    private MethodExpression getSaveListener () {
        return (MethodExpression) this.getAttributes().get("saveListener"); //$NON-NLS-1$
    }

}
