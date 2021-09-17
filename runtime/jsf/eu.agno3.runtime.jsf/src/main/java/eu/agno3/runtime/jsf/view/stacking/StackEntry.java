/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.01.2014 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking;


import java.io.Serializable;


/**
 * @author mbechler
 * 
 */
public class StackEntry implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2053775839518253561L;
    private final String returnUrl;
    private final String returnComponentId;
    private final Serializable state;
    private final String parentId;
    private String id;
    private String label;
    private boolean closable;
    private Serializable viewKey;


    /**
     * @param id
     * @param returnUrl
     * @param returnComponentId
     * @param label
     * @param state
     * @param viewKey
     * @param closable
     * 
     */
    public StackEntry ( String id, String returnUrl, String returnComponentId, String label, Serializable state, Serializable viewKey,
            boolean closable ) {
        this(id, returnUrl, returnComponentId, null, label, state, viewKey, closable);
    }


    /**
     * @param id
     * @param returnUrl
     * @param returnComponentId
     * @param parentId
     * @param label
     * @param state
     * @param viewKey
     * @param closable
     */
    public StackEntry ( String id, String returnUrl, String returnComponentId, String parentId, String label, Serializable state,
            Serializable viewKey, boolean closable ) {
        this.id = id;
        this.returnUrl = returnUrl;
        this.returnComponentId = returnComponentId;
        this.label = label;
        this.state = state;
        this.parentId = parentId;
        this.viewKey = viewKey;
        this.closable = closable;
    }


    /**
     * @return the id
     */
    public String getId () {
        return this.id;
    }


    /**
     * @return the returnUrl
     */
    public String getReturnUrl () {
        return this.returnUrl;
    }


    /**
     * @return the state
     */
    public Serializable getState () {
        return this.state;
    }


    /**
     * @return the viewKey
     */
    public Serializable getViewKey () {
        return this.viewKey;
    }


    /**
     * @return the returnComponentId
     */
    public String getReturnComponentId () {
        return this.returnComponentId;
    }


    /**
     * @return the parentId, null if root
     */
    public String getParentId () {
        return this.parentId;
    }


    /**
     * @return the closable
     */
    public boolean isClosable () {
        return this.closable;
    }


    /**
     * @return the label
     */
    public String getLabel () {
        return this.label;
    }

}
