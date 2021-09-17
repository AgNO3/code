/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.06.2015 by mbechler
 */
package eu.agno3.fileshare.service.audit;


import eu.agno3.fileshare.model.audit.GeneralFileshareEvent;
import eu.agno3.runtime.eventlog.EventLogger;


/**
 * @author mbechler
 *
 */
public class GeneralFileshareAuditBuilder extends AbstractFileshareAuditBuilder<GeneralFileshareEvent, GeneralFileshareAuditBuilder> {

    /**
     * @param logger
     */
    public GeneralFileshareAuditBuilder ( EventLogger logger ) {
        super(logger);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.audit.AbstractFileshareAuditBuilder#makeEvent()
     */
    @Override
    protected GeneralFileshareEvent makeEvent () {
        return new GeneralFileshareEvent();
    }

}
