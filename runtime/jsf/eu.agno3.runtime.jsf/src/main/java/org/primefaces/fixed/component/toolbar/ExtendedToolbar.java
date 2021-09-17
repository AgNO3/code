/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.09.2016 by mbechler
 */
package org.primefaces.fixed.component.toolbar;


import org.primefaces.component.toolbar.Toolbar;


/**
 * @author mbechler
 *
 */
public class ExtendedToolbar extends Toolbar {

    private static final String REVERSE = "reverseBreak"; //$NON-NLS-1$
    private static final String LEFT_STYLE = "left"; //$NON-NLS-1$
    private static final String RIGHT_STYLE = "right"; //$NON-NLS-1$


    /**
     * @return whether the toolbar groups are rendered in reverse order
     */
    public boolean getReverseBreak () {
        return (java.lang.Boolean) getStateHelper().eval(REVERSE, false);
    }


    /**
     * 
     * @param reverse
     */
    public void setReverseBreak ( boolean reverse ) {
        getStateHelper().put(REVERSE, reverse);
    }


    /**
     * @return extra style to apply to left toolbar group
     */
    public String getLeft () {
        return (String) getStateHelper().eval(LEFT_STYLE, null);
    }


    /**
     * 
     * @param style
     */
    public void setLeft ( String style ) {
        getStateHelper().put(LEFT_STYLE, style);
    }


    /**
     * @return extra style to apply to right toolbar group
     */
    public String getRightStyle () {
        return (String) getStateHelper().eval(RIGHT_STYLE, null);
    }


    /**
     * 
     * @param style
     */
    public void setRightStyle ( String style ) {
        getStateHelper().put(RIGHT_STYLE, style);
    }

}
