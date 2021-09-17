/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.01.2014 by mbechler
 */
package eu.agno3.runtime.jsf.types.uri;


import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
public class URLPostRestoreViewPhaseListener implements PhaseListener {

    /**
     * 
     */
    public static final String AGNO3_ORIGINAL_REQUEST_URI = "agno3.originalRequestURI"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(URLPostRestoreViewPhaseListener.class);

    /**
     * 
     */
    private static final long serialVersionUID = 5544105130585327375L;


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.event.PhaseListener#afterPhase(javax.faces.event.PhaseEvent)
     */
    @Override
    public void afterPhase ( PhaseEvent event ) {
        log.trace("After RESTORE_VIEW"); //$NON-NLS-1$

        FacesContext context = event.getFacesContext();

        if ( !context.isPostback() ) {
            Map<String, Object> viewMap = context.getViewRoot().getViewMap();

            if ( viewMap == null ) {
                log.debug("View map is NULL"); //$NON-NLS-1$
                return;
            }
            String fullRequestUri = URIUtil.getFullRequestUri();
            if ( log.isDebugEnabled() ) {
                log.debug("Saving request URI " + fullRequestUri); //$NON-NLS-1$
            }
            viewMap.put(AGNO3_ORIGINAL_REQUEST_URI, fullRequestUri);
            context.getExternalContext().getRequestMap().put(AGNO3_ORIGINAL_REQUEST_URI, fullRequestUri);
        }
        else if ( context.getViewRoot() != null ) {
            Map<String, Object> viewMap = context.getViewRoot().getViewMap();
            if ( viewMap == null ) {
                log.debug("View map is NULL"); //$NON-NLS-1$
                return;
            }
            String fullRequestUri = (String) viewMap.get(AGNO3_ORIGINAL_REQUEST_URI);
            if ( log.isDebugEnabled() ) {
                log.debug("Saving request URI " + fullRequestUri); //$NON-NLS-1$
            }
            context.getExternalContext().getRequestMap().put(AGNO3_ORIGINAL_REQUEST_URI, fullRequestUri);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.event.PhaseListener#beforePhase(javax.faces.event.PhaseEvent)
     */
    @Override
    public void beforePhase ( PhaseEvent event ) {

    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.event.PhaseListener#getPhaseId()
     */
    @Override
    public PhaseId getPhaseId () {
        return PhaseId.RESTORE_VIEW;
    }

}
