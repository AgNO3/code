/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 19, 2017 by mbechler
 */
package eu.agno3.orchestrator.realms;


import java.io.Serializable;

import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@SafeSerialization
public class RealmLookupResult implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1964221691918110219L;

    private String displayName;
    private String idName;
    private RealmEntityType type;
    private String id;


    /**
     * 
     */
    public RealmLookupResult () {}


    /**
     * @param t
     * @param id
     * @param idName
     * @param displayName
     */
    public RealmLookupResult ( RealmEntityType t, String id, String idName, String displayName ) {
        this.type = t;
        this.id = id;
        this.idName = idName;
        this.displayName = displayName;
    }


    /**
     * @return the displayName
     */
    public String getDisplayName () {
        return this.displayName;
    }


    /**
     * @param displayName
     *            the displayName to set
     */
    public void setDisplayName ( String displayName ) {
        this.displayName = displayName;
    }


    /**
     * @return the idName
     */
    public String getIdName () {
        return this.idName;
    }


    /**
     * @param idName
     *            the idName to set
     */
    public void setIdName ( String idName ) {
        this.idName = idName;
    }


    /**
     * @return the type
     */
    public RealmEntityType getType () {
        return this.type;
    }


    /**
     * @param type
     *            the type to set
     */
    public void setType ( RealmEntityType type ) {
        this.type = type;
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
