/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.05.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.layout;


import java.io.Serializable;

import javax.inject.Named;

import org.primefaces.component.layout.LayoutUnit;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.Visibility;

import eu.agno3.runtime.jsf.windowscope.WindowScoped;


/**
 * @author mbechler
 * 
 */
@Named ( "layout" )
@WindowScoped
public class LayoutBean implements Serializable {

    /**
     * 
     */
    private static final String CONSOLE_ID = "console"; //$NON-NLS-1$

    /**
     * 
     */
    private static final long serialVersionUID = -3648301641002180627L;

    private boolean consoleOpen;
    private int selectedConsoleTab;


    /**
     * @return whether the console is opened
     * 
     */
    public boolean getConsoleOpen () {
        return this.consoleOpen;
    }


    /**
     * @param consoleOpen
     *            the consoleOpen to set
     */
    public void setConsoleOpen ( boolean consoleOpen ) {
        this.consoleOpen = consoleOpen;
    }


    /**
     * @return the selectedConsoleTab
     */
    public int getSelectedConsoleTab () {
        return this.selectedConsoleTab;
    }


    /**
     * @param selectedConsoleTab
     *            the selectedConsoleTab to set
     */
    public void setSelectedConsoleTab ( int selectedConsoleTab ) {
        this.selectedConsoleTab = selectedConsoleTab;
    }


    public void handleToggle ( ToggleEvent event ) {
        LayoutUnit unit = (LayoutUnit) event.getComponent();
        if ( CONSOLE_ID.equals(unit.getId()) ) {
            this.setConsoleOpen(event.getVisibility() == Visibility.VISIBLE);
        }

    }


    public void handleConsoleTabChange ( TabChangeEvent ev ) {
        TabView tabs = (TabView) ev.getComponent();
        this.setSelectedConsoleTab(tabs.getChildren().indexOf(ev.getTab()));
    }
}
