/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * @author mbechler
 * 
 */
@Retention ( RetentionPolicy.RUNTIME )
@Documented
public @interface ObjectTypeName {

    /**
     * 
     * @return the object type name
     */
    String value();
}
