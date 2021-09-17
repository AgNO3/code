/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.05.2015 by mbechler
 */
package eu.agno3.fileshare.model.query;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import eu.agno3.fileshare.model.VFSEntity;


/**
 * @author mbechler
 *
 */
public class SearchResult implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6582581041885018394L;

    private List<VFSEntity> entities = new ArrayList<>();
    private boolean haveMoreElements;


    /**
     * @return the entities
     */
    public List<VFSEntity> getEntities () {
        return this.entities;
    }


    /**
     * @param entities
     *            the entities to set
     */
    public void setEntities ( List<VFSEntity> entities ) {
        this.entities = entities;
    }


    /**
     * @return the haveMoreElements
     */
    public boolean isHaveMoreElements () {
        return this.haveMoreElements;
    }


    /**
     * @param haveMoreElements
     *            the haveMoreElements to set
     */
    public void setHaveMoreElements ( boolean haveMoreElements ) {
        this.haveMoreElements = haveMoreElements;
    }
}
