/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 23, 2016 by mbechler
 */
package org.primefaces.fixed.component.wizard;


import org.primefaces.component.wizard.Wizard;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( {
    "javadoc", "nls"
} )
public class WizardFixed extends Wizard {

    public java.lang.String getProcess () {
        return (java.lang.String) getStateHelper().eval("process", null);
    }


    public void setProcess ( java.lang.String _process ) {
        getStateHelper().put("process", _process);
    }


    public java.lang.String getUpdate () {
        return (java.lang.String) getStateHelper().eval("update", null);
    }


    public void setUpdate ( java.lang.String _update ) {
        getStateHelper().put("update", _update);
    }


    public java.lang.String getOnstart () {
        return (java.lang.String) getStateHelper().eval("onstart", null);
    }


    public void setOnstart ( java.lang.String _onstart ) {
        getStateHelper().put("onstart", _onstart);
    }


    public java.lang.String getOncomplete () {
        return (java.lang.String) getStateHelper().eval("oncomplete", null);
    }


    public void setOncomplete ( java.lang.String _oncomplete ) {
        getStateHelper().put("oncomplete", _oncomplete);
    }


    public java.lang.String getOnerror () {
        return (java.lang.String) getStateHelper().eval("onerror", null);
    }


    public void setOnerror ( java.lang.String _onerror ) {
        getStateHelper().put("onerror", _onerror);
    }


    public java.lang.String getOnsuccess () {
        return (java.lang.String) getStateHelper().eval("onsucess", null);
    }


    public void setOnsuccess ( java.lang.String _onsuccess ) {
        getStateHelper().put("onsucess", _onsuccess);
    }
}
