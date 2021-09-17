/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 12, 2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.graph;


import java.io.Serializable;
import java.util.List;


/**
 * @author mbechler
 *
 */
public class GraphCategory implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -377363431362022298L;

    private boolean defaultCategory;
    private List<String> instances;

    private String msgBundle;
    private String titleMsgId;


    /**
     * @return the msgBundle
     */
    public String getMsgBundle () {
        return this.msgBundle;
    }


    /**
     * @param msgBundle
     *            the msgBundle to set
     */
    public void setMsgBundle ( String msgBundle ) {
        this.msgBundle = msgBundle;
    }


    /**
     * @return the titleMsgId
     */
    public String getTitleMsgId () {
        return this.titleMsgId;
    }


    /**
     * @param titleMsgId
     *            the titleMsgId to set
     */
    public void setTitleMsgId ( String titleMsgId ) {
        this.titleMsgId = titleMsgId;
    }


    /**
     * @return whether this is the default category
     */
    public boolean isDefault () {
        return this.defaultCategory;
    }


    /**
     * @return graph instances in this category
     */
    public List<String> getInstances () {
        return this.instances;
    }


    /**
     * @param instances
     *            the instances to set
     */
    public void setInstances ( List<String> instances ) {
        this.instances = instances;
    }


    /**
     * @param b
     */
    public void setDefault ( boolean b ) {
        this.defaultCategory = b;
    }

}
