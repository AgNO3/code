/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 27, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.config;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * @author mbechler
 *
 */
@Documented
@Retention ( RetentionPolicy.RUNTIME )
public @interface ObjectName {

    /**
     * 
     * @return whether to allow duplicate identifier values
     */
    boolean allowDuplicates() default false;
}
