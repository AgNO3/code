/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.08.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.server.webgui.components.ObjectEditor;


/**
 * @author mbechler
 *
 */
public final class EditorUtil {

    private static final Logger log = Logger.getLogger(EditorUtil.class);


    /**
     * 
     */
    private EditorUtil () {}


    /**
     * 
     * @return the root editor or null if not found
     */
    public static ObjectEditor findRootEditor () {
        UIViewRoot vr = FacesContext.getCurrentInstance().getViewRoot();
        VisitCallbackImplementation findRootEditorCallback = new VisitCallbackImplementation();
        vr.visitTree(VisitContext.createVisitContext(FacesContext.getCurrentInstance()), findRootEditorCallback);
        return findRootEditorCallback.getEditor();
    }


    public static void resetRootEditor () {
        ObjectEditor root = findRootEditor();
        if ( root != null ) {
            log.debug("Resetting root editor"); //$NON-NLS-1$
            root.resetComponent();
        }
        else {
            log.debug("Root editor not found"); //$NON-NLS-1$
        }
    }

    /**
     * @author mbechler
     *
     */
    public static final class VisitCallbackImplementation implements VisitCallback {

        private ObjectEditor editor;


        /**
         * 
         */
        public VisitCallbackImplementation () {}


        @Override
        public VisitResult visit ( VisitContext ctx, UIComponent comp ) {
            if ( comp instanceof ObjectEditor ) {
                this.editor = (ObjectEditor) comp;
                return VisitResult.COMPLETE;
            }
            return VisitResult.ACCEPT;
        }


        /**
         * @return the editor
         */
        public ObjectEditor getEditor () {
            return this.editor;
        }
    }
}
