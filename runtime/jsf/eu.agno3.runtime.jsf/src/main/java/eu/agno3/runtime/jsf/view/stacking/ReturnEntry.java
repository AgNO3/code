/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jul 12, 2017 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking;


import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public class ReturnEntry implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4168346713681961445L;
    private Serializable returnValue;
    private String viewId;
    private Object viewKey;


    /**
     * @param returnValue
     * @param viewId
     * @param viewKey
     * 
     */
    public ReturnEntry ( Serializable returnValue, String viewId, Serializable viewKey ) {
        this.returnValue = returnValue;
        this.viewId = viewId;
        this.viewKey = viewKey;
    }


    /**
     * @return the returnValue
     */
    public Serializable getReturnValue () {
        return this.returnValue;
    }


    /**
     * @return the viewId
     */
    public String getViewId () {
        return this.viewId;
    }


    /**
     * @return the viewKey
     */
    public Object getViewKey () {
        return this.viewKey;
    }
}
