/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.11.2013 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.menu;


import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;


/**
 * @author mbechler
 * 
 */
public class SyntheticMenuTreeNode extends DefaultTreeNode implements NavigationMenuItem {

    /**
     * 
     */
    private static final long serialVersionUID = 70011680982230218L;


    /**
     * @param parent
     * @param type
     * 
     */
    public SyntheticMenuTreeNode ( TreeNode parent, String type ) {
        super(parent);
        this.setType(type);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.primefaces.model.DefaultTreeNode#getData()
     */
    @Override
    public Object getData () {
        return this;
    }
}
