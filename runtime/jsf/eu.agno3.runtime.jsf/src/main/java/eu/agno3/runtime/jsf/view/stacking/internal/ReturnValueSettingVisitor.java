/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.01.2014 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking.internal;


import java.io.Serializable;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;

import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;

import eu.agno3.runtime.jsf.view.stacking.components.DialogOpenComponent;


/**
 * @author mbechler
 * 
 */
public class ReturnValueSettingVisitor implements VisitCallback {

    private static final Logger log = Logger.getLogger(ReturnValueSettingVisitor.class);

    private final Serializable returnValue;


    /**
     * @param returnValue
     */
    public ReturnValueSettingVisitor ( Serializable returnValue ) {
        this.returnValue = returnValue;
    }


    @Override
    public VisitResult visit ( VisitContext ctx, UIComponent target ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Visiting component " + target.getClientId()); //$NON-NLS-1$
        }

        if ( target instanceof UIComponentBase ) {
            UIComponentBase comp = (UIComponentBase) target;
            List<ClientBehavior> list = comp.getClientBehaviors().get("return"); //$NON-NLS-1$

            if ( comp instanceof DialogOpenComponent ) {
                ( (DialogOpenComponent) comp ).resetOpened();
            }

            if ( list != null && !list.isEmpty() ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Found return behaviour " + list.get(0)); //$NON-NLS-1$
                }

                for ( ClientBehavior behavior : list ) {
                    comp.queueEvent(new SelectEvent(comp, behavior, this.returnValue));
                }
            }
        }

        return VisitResult.REJECT;
    }
}