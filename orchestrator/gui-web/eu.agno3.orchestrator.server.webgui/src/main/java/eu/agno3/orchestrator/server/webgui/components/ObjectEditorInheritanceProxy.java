/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.components;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;


/**
 * @author mbechler
 * 
 */
public class ObjectEditorInheritanceProxy implements InvocationHandler {

    private static Set<String> LOCAL_METHODS = new HashSet<>();

    static {
        LOCAL_METHODS.add("getId"); //$NON-NLS-1$
        LOCAL_METHODS.add("getDisplayName"); //$NON-NLS-1$
        LOCAL_METHODS.add("getName"); //$NON-NLS-1$
        LOCAL_METHODS.add("getRevision"); //$NON-NLS-1$
        LOCAL_METHODS.add("getVersion"); //$NON-NLS-1$
        LOCAL_METHODS.add("getInherits"); //$NON-NLS-1$
    }

    private ObjectEditor objectEditor;


    /**
     * @param objectEditor
     */
    public ObjectEditorInheritanceProxy ( ObjectEditor objectEditor ) {
        this.objectEditor = objectEditor;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws ModelObjectNotFoundException
     * 
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke ( Object proxy, Method method, Object[] args ) throws ModelObjectNotFoundException, IllegalAccessException,
            InvocationTargetException, ModelServiceException, GuiWebServiceException {

        String methodName = method.getName();

        if ( LOCAL_METHODS.contains(methodName) ) {
            return method.invoke(this.objectEditor.getCurrent());
        }

        Object enforcedValue = method.invoke(this.objectEditor.getEnforced());

        if ( enforcedValue != null ) {
            return enforcedValue;
        }

        Object localValue = method.invoke(this.objectEditor.getCurrent());

        if ( localValue != null ) {
            return localValue;
        }

        return method.invoke(this.objectEditor.getInherited());
    }

}
