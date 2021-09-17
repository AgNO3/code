/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.12.2015 by mbechler
 */
package eu.agno3.orchestrator.agent;


import java.io.Serializable;

import eu.agno3.orchestrator.server.component.ComponentInfo;


/**
 * @author mbechler
 *
 */
public class AgentInfo extends ComponentInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2352034241412223529L;

    private String imageType;


    /**
     * @return the imageType
     */
    public String getImageType () {
        return this.imageType;
    }


    /**
     * @param imageType
     *            the imageType to set
     */
    public void setImageType ( String imageType ) {
        this.imageType = imageType;
    }

}
