/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 12, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.config;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * This is a one-off property
 * 
 * One off properties are Booleans which are reset to a specifed value after applying the config.
 * 
 * @author mbechler
 *
 */
@Documented
@Retention ( RetentionPolicy.RUNTIME )
public @interface OneOff {

    /**
     * 
     * @return the boolean value to reset to after application
     */
    boolean resetTo() default false;


    /**
     * 
     * @return reset the value to null after application
     */
    boolean resetToNull() default false;
}
