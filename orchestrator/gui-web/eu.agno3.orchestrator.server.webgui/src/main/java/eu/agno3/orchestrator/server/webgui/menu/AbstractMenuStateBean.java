/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.04.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.menu;


import java.io.Serializable;
import java.util.List;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuItem;
import org.primefaces.model.menu.MenuModel;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractMenuStateBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5556116492808274584L;

    private DefaultMenuModel model;


    protected abstract String makeDefaultParameters () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    protected abstract DefaultMenuModel createMenuModel () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException;


    protected AbstractMenuStateBean () {
        super();
    }


    public MenuModel getModel () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( this.model == null ) {
            this.model = createMenuModel();
        }
        return this.model;
    }


    /**
     * @param menu
     * @param entries
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    protected void addEntries ( DefaultSubMenu menu, List<MenuEntry> entries, String msgPrefix ) throws ModelObjectNotFoundException,
            ModelServiceException, GuiWebServiceException {
        for ( MenuEntry e : entries ) {
            menu.addElement(makeDefaultItem(getLocalized(e.getLabel()), e.getView()));

        }
    }


    protected void addEntry ( DefaultSubMenu menu, MenuEntry e, String args, String msgPrefix ) {
        menu.addElement(makeItemWithArgs(getLocalized(e.getLabel()), e.getView(), args));
    }


    /**
     * @param localized
     * @param view
     * @param string
     * @return
     */
    protected MenuElement makeItemWithArgs ( String label, String view, String args ) {
        DefaultMenuItem itm = new DefaultMenuItem(label);
        itm.setOutcome(view + args);
        return itm;
    }


    /**
     * @param label
     * @return
     */
    protected abstract String getLocalized ( String label );


    protected MenuItem makeDefaultItem ( String label, String view ) throws ModelObjectNotFoundException, ModelServiceException,
            GuiWebServiceException {
        DefaultMenuItem itm = new DefaultMenuItem(label);
        itm.setOutcome(view + this.makeDefaultParameters());
        return itm;
    }


    /**
     * @return the menu model
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public MenuModel createModel () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return createMenuModel();
    }

}