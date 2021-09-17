/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.components;


import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;


/**
 * @author mbechler
 * 
 */
public class InheritanceChangedEvent extends FacesEvent {

    /**
     * 
     */
    private static final long serialVersionUID = 3971204015827213362L;


    /**
     * @param component
     */
    public InheritanceChangedEvent ( UIComponent component ) {
        super(component);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.event.FacesEvent#isAppropriateListener(javax.faces.event.FacesListener)
     */
    @Override
    public boolean isAppropriateListener ( FacesListener l ) {
        return l instanceof ObjectEditor;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.event.FacesEvent#processListener(javax.faces.event.FacesListener)
     */
    @Override
    public void processListener ( FacesListener l ) {

        ObjectEditor editor = (ObjectEditor) l;

        try {
            editor.setLocalInherits(null);
        }
        catch ( Exception e ) {
            throw new FacesException(e);
        }

    }

}
