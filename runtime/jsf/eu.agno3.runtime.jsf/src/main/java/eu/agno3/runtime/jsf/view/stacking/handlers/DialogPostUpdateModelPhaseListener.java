/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2015 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking.handlers;


import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.faces.FactoryFinder;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitContextFactory;
import javax.faces.component.visit.VisitHint;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.jsf.view.stacking.DialogConstants;
import eu.agno3.runtime.jsf.view.stacking.internal.ReturnValueSettingVisitor;


/**
 * @author mbechler
 *
 */
public class DialogPostUpdateModelPhaseListener implements PhaseListener {

    private static final Logger log = Logger.getLogger(DialogPostUpdateModelPhaseListener.class);

    /**
     * 
     */
    private static final long serialVersionUID = 7140334652758138159L;

    private static final Set<VisitHint> PARTIAL_EXECUTE_HINTS = Collections.unmodifiableSet(EnumSet.of(
        VisitHint.EXECUTE_LIFECYCLE,
        VisitHint.SKIP_UNRENDERED));


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.event.PhaseListener#afterPhase(javax.faces.event.PhaseEvent)
     */
    @Override
    public void afterPhase ( PhaseEvent ev ) {
        ExternalContext ctx = ev.getFacesContext().getExternalContext();
        HttpServletRequest req = (HttpServletRequest) ctx.getRequest();

        String returnComponentId = (String) req.getAttribute(DialogConstants.REQATTR_COMPONENT_ID);
        Serializable returnValue = (Serializable) req.getAttribute(DialogConstants.REQATTR_RETURN_VALUE);

        if ( !StringUtils.isBlank(returnComponentId) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Found return to " + returnComponentId); //$NON-NLS-1$
            }
            setReturnValue(ev.getFacesContext(), ev.getFacesContext().getViewRoot(), returnComponentId, returnValue);
            req.removeAttribute(DialogConstants.REQATTR_COMPONENT_ID);
            req.removeAttribute(DialogConstants.REQATTR_RETURN_VALUE);
        }

    }


    /**
     * @param context
     * @param root
     * @param returnComponentId
     * @param returnValue
     */
    private static void setReturnValue ( final FacesContext context, UIViewRoot root, String returnComponentId, final Serializable returnValue ) {
        VisitContext ctx = createVisitContextFactory().getVisitContext(context, Arrays.asList(returnComponentId), PARTIAL_EXECUTE_HINTS);
        root.visitTree(ctx, new ReturnValueSettingVisitor(returnValue));
    }


    private static VisitContextFactory createVisitContextFactory () {
        return (VisitContextFactory) FactoryFinder.getFactory(FactoryFinder.VISIT_CONTEXT_FACTORY);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.event.PhaseListener#beforePhase(javax.faces.event.PhaseEvent)
     */
    @Override
    public void beforePhase ( PhaseEvent ev ) {}


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.event.PhaseListener#getPhaseId()
     */
    @Override
    public PhaseId getPhaseId () {
        return PhaseId.UPDATE_MODEL_VALUES;
    }

}
