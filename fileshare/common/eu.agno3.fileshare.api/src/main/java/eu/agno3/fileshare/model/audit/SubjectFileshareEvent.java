/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2015 by mbechler
 */
package eu.agno3.fileshare.model.audit;


import java.util.UUID;


/**
 * @author mbechler
 *
 */
public class SubjectFileshareEvent extends BaseFileshareEvent {

    /**
     * 
     */
    private static final long serialVersionUID = 9122883479593116360L;

    private UUID targetId;
    private String targetName;
    private String targetType;


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
     * @return the targetId
     */
    public UUID getTargetId () {
        return this.targetId;
    }


    /**
     * @param targetId
     *            the targetId to set
     */
    public void setTargetId ( UUID targetId ) {
        this.targetId = targetId;
    }


    /**
     * @return the targetName
     */
    public String getTargetName () {
        return this.targetName;
    }


    /**
     * @param targetName
     *            the targetName to set
     */
    public void setTargetName ( String targetName ) {
        this.targetName = targetName;
    }


    /**
     * @return the targetType
     */
    public String getTargetType () {
        return this.targetType;
    }


    /**
     * @param targetType
     *            the targetType to set
     */
    public void setTargetType ( String targetType ) {
        this.targetType = targetType;
    }
}
