/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2015 by mbechler
 */
package eu.agno3.fileshare.model.audit;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * @author mbechler
 *
 */
public class MultiSubjectFileshareEvent extends BaseFileshareEvent {

    /**
     * 
     */
    private static final long serialVersionUID = 9122883479593116360L;

    private List<UUID> targetIds = new ArrayList<>();
    private List<String> targetNames = new ArrayList<>();
    private List<String> targetTypes = new ArrayList<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getType()
     */
    @Override
    public String getType () {
        return "subject-event"; //$NON-NLS-1$
    }


    /**
     * @return the targetIds
     */
    public List<UUID> getTargetIds () {
        return this.targetIds;
    }


    /**
     * @param targetIds
     *            the targetIds to set
     */
    public void setTargetIds ( List<UUID> targetIds ) {
        this.targetIds = targetIds;
    }


    /**
     * @return the targetNames
     */
    public List<String> getTargetNames () {
        return this.targetNames;
    }


    /**
     * @param targetNames
     *            the targetNames to set
     */
    public void setTargetNames ( List<String> targetNames ) {
        this.targetNames = targetNames;
    }


    /**
     * @return the targetTypes
     */
    public List<String> getTargetTypes () {
        return this.targetTypes;
    }


    /**
     * @param targetTypes
     *            the targetTypes to set
     */
    public void setTargetTypes ( List<String> targetTypes ) {
        this.targetTypes = targetTypes;
    }
}
