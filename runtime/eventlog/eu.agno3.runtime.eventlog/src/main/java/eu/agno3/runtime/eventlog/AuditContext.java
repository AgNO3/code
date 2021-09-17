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
public interface AuditContext <T extends AuditEventBuilder<T>> extends AutoCloseable {

    /**
     * 
     * @return the builder
     */
    T builder ();


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close ();


    /**
     * Supress the generated message
     */
    void suppress ();
}
