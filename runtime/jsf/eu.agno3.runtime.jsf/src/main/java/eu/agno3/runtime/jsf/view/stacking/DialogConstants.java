/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.01.2014 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking;


/**
 * @author mbechler
 * 
 */
public final class DialogConstants {

    private DialogConstants () {}

    /**
     * Dialog stack return indicator attr
     */
    public static final String RETURN_ATTR = "return"; //$NON-NLS-1$

    /**
     * Dialog stack return pointer attribute
     */
    public static final String RETURN_TO_ATTR = "returnTo"; //$NON-NLS-1$

    /**
     * Navigation outcome which triggers a dialog close
     */
    public static final String DIALOG_CLOSE_OUTCOME = "dialog:close"; //$NON-NLS-1$

    /**
     * Component family
     */
    public static final String COMPONENT_FAMILY = "eu.agno3.jsf.dialog"; //$NON-NLS-1$
    /**
     * Dialog return label component attribute
     */
    public static final String RETURN_LABEL = "returnLabel"; //$NON-NLS-1$

    /**
     * Dialog target component attribute
     */
    public static final String DIALOG_ATTR = "dialog"; //$NON-NLS-1$

    /**
     * Method called before opening a dialog
     */
    public static final String PRE_OPEN_HANDLER = "preOpen"; //$NON-NLS-1$

    /**
     * Closable attr
     */
    public static final String CLOSABLE = "closable"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String REQATTR_COMPONENT_ID = "returnComponentId"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String REQATTR_RETURN_VALUE = "returnValue"; //$NON-NLS-1$

}
