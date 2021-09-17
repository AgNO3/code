/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.11.2015 by mbechler
 */
package eu.agno3.runtime.update;


import java.io.Serializable;
import java.util.List;


/**
 * @author mbechler
 *
 */
public class FeatureUpdate implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2455881219195103134L;

    private Feature old;
    private List<Feature> possibleUpdates;


    /**
     * @return the old
     */
    public Feature getOld () {
        return this.old;
    }


    /**
     * @param old
     *            the old to set
     */
    public void setOld ( Feature old ) {
        this.old = old;
    }


    /**
     * @return the updates
     */
    public List<Feature> getPossibleUpdates () {
        return this.possibleUpdates;
    }


    /**
     * @param updates
     *            the updates to set
     */
    public void setPossibleUpdates ( List<Feature> updates ) {
        this.possibleUpdates = updates;
    }
}
