/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.05.2015 by mbechler
 */
package eu.agno3.fileshare.model.shortcut;


import java.io.Serializable;
import java.util.UUID;

import eu.agno3.fileshare.model.EntityKey;


/**
 * @author mbechler
 *
 */
public class Shortcut implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5310958714054574615L;

    private ShortcutType type;
    private String label;
    private EntityKey targetId;
    private UUID grantId;
    private boolean fromGroup;
    private UUID subjectId;


    /**
     * @return the type
     */
    public ShortcutType getType () {
        return this.type;
    }


    /**
     * @param type
     *            the type to set
     */
    public void setType ( ShortcutType type ) {
        this.type = type;
    }


    /**
     * @return the label
     */
    public String getLabel () {
        return this.label;
    }


    /**
     * @param label
     *            the label to set
     */
    public void setLabel ( String label ) {
        this.label = label;
    }


    /**
     * @return the targetId
     */
    public EntityKey getTargetId () {
        return this.targetId;
    }


    /**
     * @param targetId
     *            the targetId to set
     */
    public void setTargetId ( EntityKey targetId ) {
        this.targetId = targetId;
    }


    /**
     * 
     * @return the subject id
     */
    public UUID getSubjectId () {
        return this.subjectId;
    }


    /**
     * 
     * @param subjectId
     */
    public void setSubjectId ( UUID subjectId ) {
        this.subjectId = subjectId;
    }


    /**
     * @return the grantId
     */
    public UUID getGrantId () {
        return this.grantId;
    }


    /**
     * @param grantId
     *            the grantId to set
     */
    public void setGrantId ( UUID grantId ) {
        this.grantId = grantId;
    }


    /**
     * @return the fromGroup
     */
    public boolean isFromGroup () {
        return this.fromGroup;
    }


    /**
     * @param b
     */
    public void setFromGroup ( boolean b ) {
        this.fromGroup = b;
    }

}
