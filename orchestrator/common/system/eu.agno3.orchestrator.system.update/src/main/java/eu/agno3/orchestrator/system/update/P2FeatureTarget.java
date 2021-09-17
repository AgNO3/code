/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.update;


import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public class P2FeatureTarget implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6594515882362463844L;

    private String featureId;
    private String featureVersion;


    /**
     * @return the featureId
     */
    public String getFeatureId () {
        return this.featureId;
    }


    /**
     * @param featureId
     *            the featureId to set
     */
    public void setFeatureId ( String featureId ) {
        this.featureId = featureId;
    }


    /**
     * @return the featureVersion
     */
    public String getFeatureVersion () {
        return this.featureVersion;
    }


    /**
     * @param featureVersion
     *            the featureVersion to set
     */
    public void setFeatureVersion ( String featureVersion ) {
        this.featureVersion = featureVersion;
    }

}
