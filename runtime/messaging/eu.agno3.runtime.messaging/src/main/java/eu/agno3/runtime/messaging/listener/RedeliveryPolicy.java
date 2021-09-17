/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.02.2016 by mbechler
 */
package eu.agno3.runtime.messaging.listener;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * @author mbechler
 *
 */
@Retention ( RetentionPolicy.RUNTIME )
@Documented
public @interface RedeliveryPolicy {

    /**
     * 
     * @return whether to use exponential backoff
     */
    boolean useExponentialBackoff() default false;


    /**
     * @return time before first redelivery, in ms
     */
    long initialRedeliveryDelay() default 500;


    /**
     * @return number of redeliveries
     */
    int maximumRedeliveries() default 3;


    /**
     * @return back off multiplier
     */
    double backOffMultiplier() default 4.0;

}
