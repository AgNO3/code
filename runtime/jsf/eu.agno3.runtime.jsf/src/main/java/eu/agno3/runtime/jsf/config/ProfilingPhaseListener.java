/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.07.2014 by mbechler
 */
package eu.agno3.runtime.jsf.config;


import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
public class ProfilingPhaseListener implements PhaseListener {

    /**
     * 
     */
    private static final String EU_AGNO3_PROFILING = "eu.agno3.profiling."; //$NON-NLS-1$
    /**
     * 
     */
    private static final long serialVersionUID = 5848077340439543583L;
    private static final Logger log = Logger.getLogger(ProfilingPhaseListener.class);


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.event.PhaseListener#afterPhase(javax.faces.event.PhaseEvent)
     */
    @Override
    public void afterPhase ( PhaseEvent event ) {
        if ( log.isDebugEnabled() ) {
            Long phaseStart = (Long) event.getFacesContext().getAttributes().get(EU_AGNO3_PROFILING + event.getPhaseId());
            Long requestStart = (Long) event.getFacesContext().getAttributes().get(EU_AGNO3_PROFILING + PhaseId.RESTORE_VIEW);

            if ( phaseStart != null ) {
                log.debug(String.format(
                    "Phase %s took %d ms (%d ms from start of request)", //$NON-NLS-1$
                    event.getPhaseId(),
                    System.currentTimeMillis() - phaseStart,
                    System.currentTimeMillis() - requestStart));
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.event.PhaseListener#beforePhase(javax.faces.event.PhaseEvent)
     */
    @Override
    public void beforePhase ( PhaseEvent event ) {
        if ( log.isDebugEnabled() ) {
            event.getFacesContext().getAttributes().put(EU_AGNO3_PROFILING + event.getPhaseId(), System.currentTimeMillis());
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.event.PhaseListener#getPhaseId()
     */
    @Override
    public PhaseId getPhaseId () {
        return PhaseId.ANY_PHASE;
    }

}
