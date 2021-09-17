/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.01.2014 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking.components;


import javax.faces.component.UINamingContainer;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.MenuActionEvent;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuItem;
import org.primefaces.model.menu.MenuModel;

import eu.agno3.runtime.jsf.view.stacking.DialogConstants;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;
import eu.agno3.runtime.jsf.view.stacking.StackEntry;


/**
 * @author mbechler
 * 
 */
public class DialogBreadcrumps extends UINamingContainer {

    private static final Logger log = Logger.getLogger(DialogBreadcrumps.class);


    /**
     * 
     * @return whether the breadcrumps should be shown (more than one stack entry present)
     */
    public boolean shouldShowBreadcrumps () {
        return DialogContext.getCurrentStack().size() > 1;
    }


    /**
     * @return the view stack as menu model
     */
    public MenuModel getMenuModel () {
        DefaultMenuModel m = new DefaultMenuModel();

        boolean first = true;

        for ( StackEntry e : DialogContext.getCurrentStack() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Adding " + e.getLabel()); //$NON-NLS-1$
            }
            DefaultMenuItem item = new DialogReturnMenuItem(e.getId(), e.getLabel());
            item.setCommand("#{cc.menuListener}"); //$NON-NLS-1$
            if ( first ) {
                item.setDisabled(true);
                item.setValue(StringUtils.EMPTY);
                first = false;
            }
            m.addElement(item);
        }
        m.generateUniqueIds();

        return m;
    }


    /**
     * @param ev
     * @return dialog close outcome if this is a DialogReturnMenuItem
     */
    public String menuListener ( ActionEvent ev ) {
        if ( ev instanceof MenuActionEvent ) {
            MenuItem m = ( (MenuActionEvent) ev ).getMenuItem();

            if ( m instanceof DialogReturnMenuItem ) {
                return DialogConstants.DIALOG_CLOSE_OUTCOME + ":" + ( (DialogReturnMenuItem) m ).getReturnId(); //$NON-NLS-1$
            }
        }

        return null;
    }

    /**
     * @author mbechler
     * 
     */
    public static class DialogReturnMenuItem extends DefaultMenuItem {

        /**
         * 
         */
        private static final long serialVersionUID = 7891336622657997313L;
        private String returnId;


        /**
         * @param id
         * @param label
         * 
         */
        public DialogReturnMenuItem ( String id, String label ) {
            super(label);
            this.setTitle(label);
            this.setUrl(null);
            this.setOutcome(null);
            this.setAjax(true);
            this.setImmediate(true);
            this.returnId = id;
        }


        /**
         * @return the returnId
         */
        public String getReturnId () {
            return this.returnId;
        }

    }
}
