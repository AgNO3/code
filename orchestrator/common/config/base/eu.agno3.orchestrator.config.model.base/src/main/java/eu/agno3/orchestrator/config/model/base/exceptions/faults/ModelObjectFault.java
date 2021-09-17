/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.exceptions.faults;


import java.util.UUID;


/**
 * @author mbechler
 * 
 */
public class ModelObjectFault {

    private String objectType;
    private String id;


    /**
     * 
     */
    public ModelObjectFault () {}


    /**
     * @param objectType
     * @param id
     */
    public ModelObjectFault ( String objectType, String id ) {
        this.objectType = objectType;
        this.id = id;
    }


    /**
     * @param type
     * @param id
     */
    public ModelObjectFault ( Class<?> type, UUID id ) {
        this(type.getName() != null ? type.getName() : null, id != null ? id.toString() : null);
    }


    /**
     * @return the objectType
     */
    public String getObjectType () {
        return this.objectType;
    }


    /**
     * @param objectType
     *            the objectType to set
     */
    public void setObjectType ( String objectType ) {
        this.objectType = objectType;
    }


    /**
     * @return the id
     */
    public String getId () {
        return this.id;
    }


    /**
     * @param id
     *            the id to set
     */
    public void setId ( String id ) {
        this.id = id;
    }

}
