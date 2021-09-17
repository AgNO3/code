package eu.agno3.orchestrator.server.webgui.menu;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.TreeNode;


/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.11.2013 by mbechler
 */

/**
 * @author mbechler
 * 
 */
@Named ( "mainTreeMenu" )
@ApplicationScoped
public class MainMenuTreeBean {

    @Inject
    private TreeMenuStateBean state;


    /**
     * @return the tree root
     */
    public TreeNode getRoot () {
        return this.state.getRoot();
    }


    /**
     * @return the tree state holder
     */
    public TreeMenuStateBean getState () {
        return this.state;
    }

}
