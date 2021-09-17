/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2015 by mbechler
 */
package eu.agno3.runtime.eventlog;


/**
 * @author mbechler
 * @param <T>
 *
 */
public interface AuditEventBuilder <T extends AuditEventBuilder<T>> extends EventBuilder<T> {

    /**
     * 
     * @param reason
     * @return this builder
     */
    T fail ( AuditStatus reason );


    /**
     * @param action
     * @return this builder
     */
    T action ( String action );

}
