/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.ui;


import javax.faces.view.ViewScoped;
import javax.inject.Named;

import eu.agno3.runtime.jsf.prefs.AbstractTabsBean;


/**
 * @author mbechler
 *
 */
@Named ( "infoTabsBean" )
@ViewScoped
public class InfoTabsBean extends AbstractTabsBean {

    /**
     * 
     */
    private static final long serialVersionUID = 5393754848577832775L;


    /**
     * 
     */
    public InfoTabsBean () {
        addTab("properties"); //$NON-NLS-1$
    }
}
