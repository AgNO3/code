/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.12.2014 by mbechler
 */
package eu.agno3.runtime.jsf.components;


import javax.faces.component.UIComponent;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;


/**
 * @author mbechler
 *
 */
public class ResetComponentsVisitCallback implements VisitCallback {

    private UIComponent self;


    /**
     * @param self
     */
    public ResetComponentsVisitCallback ( UIComponent self ) {
        this.self = self;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.visit.VisitCallback#visit(javax.faces.component.visit.VisitContext,
     *      javax.faces.component.UIComponent)
     */
    @Override
    public VisitResult visit ( VisitContext context, UIComponent target ) {

        if ( target == this.self ) {
            return VisitResult.ACCEPT;
        }

        if ( target instanceof ResettableComponent ) {
            if ( ! ( (ResettableComponent) target ).resetComponent() ) {
                return VisitResult.REJECT;
            }
        }

        return VisitResult.ACCEPT;
    }

}
