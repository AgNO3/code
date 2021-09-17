/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.10.2014 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking.components;


/**
 * @author mbechler
 *
 */
public interface DialogOpenComponent {

    /**
     * 
     * @return the widget var
     */
    String getWidgetVar ();


    /**
     * 
     * @return the client id
     */
    String getClientId ();


    /**
     * @return dialog view id
     */
    String getDialog ();


    /**
     * 
     * @return whether the dialog should be closable
     */
    boolean isClosable ();


    /**
     * @return the return label
     */
    String getReturnLabel ();


    /**
     * @return whether a client return behaviour is attached
     * 
     */
    boolean hasReturnBehaviour ();


    /**
     * 
     * @return whether the dialog was already opened
     */
    boolean getAndSetOpened ();


    /**
     * Reset the dialog open state
     * 
     */
    void resetOpened ();

}