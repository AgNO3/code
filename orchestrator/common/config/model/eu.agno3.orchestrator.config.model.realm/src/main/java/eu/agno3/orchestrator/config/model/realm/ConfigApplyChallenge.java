/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 12, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import java.io.Serializable;
import java.util.List;


/**
 * @author mbechler
 *
 */
public interface ConfigApplyChallenge extends Serializable {

    /**
     * 
     * @return challenge type identifier
     */
    String getType ();


    /**
     * 
     * @return key to identify the challenge
     */
    String getKey ();


    /**
     * 
     * @return whether answering is required
     */
    boolean isRequired ();


    /**
     * 
     * @return whether the response is valid
     */
    boolean verify ();


    /**
     * @return message bundle to use
     */
    String getMessageBase ();


    /**
     * @return label template
     */
    String getLabelTemplate ();


    /**
     * @return label arguments
     */
    List<String> getLabelArgs ();
}
