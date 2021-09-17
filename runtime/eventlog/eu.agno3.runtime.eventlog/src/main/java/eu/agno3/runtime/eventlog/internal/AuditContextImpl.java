/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.internal;


import eu.agno3.runtime.eventlog.AuditContext;
import eu.agno3.runtime.eventlog.AuditEventBuilder;


/**
 * @author mbechler
 * @param <T>
 *
 */
public class AuditContextImpl <T extends AuditEventBuilder<T>> implements AuditContext<T> {

    private T builder;

    private boolean suppress;


    /**
     * @param builder
     */
    public AuditContextImpl ( T builder ) {
        this.builder = builder;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.AuditContext#builder()
     */
    @Override
    public T builder () {
        return this.builder;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.AuditContext#suppress()
     */
    @Override
    public void suppress () {
        this.suppress = true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.AuditContext#close()
     */
    @Override
    public void close () {
        if ( !this.suppress ) {
            this.builder.log();
        }
    }

}
