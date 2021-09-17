/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.validation;


import java.io.Serializable;
import java.util.List;


/**
 * @author mbechler
 *
 */
public interface ViolationEntry extends Serializable {

    /**
     * 
     * @return the violation level
     */
    ViolationLevel getLevel ();


    /**
     * @return the path to the violating entry/property
     */
    List<String> getPath ();


    /**
     * 
     * @return the base object type of the valdiator finding this violation
     */
    String getObjectType ();


    /**
     * 
     * @return the message template key
     */
    String getMessageTemplate ();


    /**
     * 
     * @return Optional message arguments
     */
    List<String> getMessageArgs ();

}
