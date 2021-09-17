/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.11.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author mbechler
 *
 */
public class ObjectTypeTreeNode implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8485777448233165583L;
    private String objectType;
    private List<ObjectTypeTreeNode> children = new ArrayList<>();


    /**
     * 
     */
    public ObjectTypeTreeNode () {}


    /**
     * @param objectType
     */
    public ObjectTypeTreeNode ( String objectType ) {
        this.objectType = objectType;
    }


    /**
     * @return the objectType
     */
    public String getObjectType () {
        return this.objectType;
    }


    /**
     * @param objectType
     *            the objectType to set
     */
    public void setObjectType ( String objectType ) {
        this.objectType = objectType;
    }


    /**
     * @return the children
     */
    public List<ObjectTypeTreeNode> getChildren () {
        return this.children;
    }


    /**
     * @param children
     *            the children to set
     */
    public void setChildren ( List<ObjectTypeTreeNode> children ) {
        this.children = children;
    }
}
