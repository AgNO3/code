/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.04.2014 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.runtime.jsf.i18n.BaseMessages;


/**
 * @author mbechler
 * 
 */
@Named ( "dialog" )
@ApplicationScoped
public class Dialog {

    /**
     * Close the current dialog without return value
     * 
     * @return outcome
     */
    public Object close () {
        return DialogContext.closeDialog(null);
    }


    /**
     * 
     * @return whether we are in a dialog
     */
    public boolean inDialog () {
        return DialogContext.isInDialog();
    }


    /**
     * 
     * @return label for the close button
     */
    public String getCloseLabel () {
        if ( DialogContext.getCurrentStack().size() > 1 ) {
            return BaseMessages.get("dialog.backLabel"); //$NON-NLS-1$
        }

        return BaseMessages.get("dialog.closeLabel"); //$NON-NLS-1$
    }


    /**
     * @return label for a cancel button
     */
    public String getCancelLabel () {
        return BaseMessages.get("dialog.cancelLabel"); //$NON-NLS-1$
    }


    /**
     * 
     * @return description/title for the close button
     */
    public String getCloseDescription () {
        if ( DialogContext.getCurrentStack().size() > 1 ) {
            return BaseMessages.get("dialog.backDescription"); //$NON-NLS-1$
        }

        return BaseMessages.get("dialog.closeDescription"); //$NON-NLS-1$
    }


    /**
     * 
     * @return description/title for a cancel button
     */
    public String getCancelDescription () {
        return BaseMessages.get("dialog.cancelDescription"); //$NON-NLS-1$
    }


    /**
     * 
     * @return icon for the close button
     */
    public String getCloseIcon () {
        if ( DialogContext.getCurrentStack().size() > 1 ) {
            return "ui-icon-back"; //$NON-NLS-1$
        }

        return "ui-icon-closethick"; //$NON-NLS-1$
    }

}
