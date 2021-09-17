/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.06.2015 by mbechler
 */
package eu.agno3.fileshare.model.audit;


/**
 * @author mbechler
 *
 */
public class GeneralFileshareEvent extends BaseFileshareEvent {

    /**
     * 
     */
    private static final long serialVersionUID = 9007836528664552414L;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getType()
     */
    @Override
    public String getType () {
        return "general-event"; //$NON-NLS-1$
    }

}
