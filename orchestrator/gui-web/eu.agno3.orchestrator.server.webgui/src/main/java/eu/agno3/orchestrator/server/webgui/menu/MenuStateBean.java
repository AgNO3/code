/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.menu;


import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.runtime.jsf.windowscope.WindowScoped;


/**
 * @author mbechler
 * 
 */
@Named ( "menuStateBean" )
@WindowScoped
public class MenuStateBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4824423996173479546L;

    private Set<String> openTabs = new HashSet<>();


    public String getActiveIndices () {
        return StringUtils.join(this.openTabs, ',');
    }


    public void setActiveIndices ( String active ) {
        String[] split = StringUtils.split(active, ',');

        this.openTabs.clear();
        this.openTabs.addAll(Arrays.asList(split));
    }
}
