/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2013 by mbechler
 */
package eu.agno3.runtime.logging.config;


import java.util.Map;
import java.util.Observable;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractLoggerConfigurationSource extends Observable implements PrioritizedLoggerConfigurationSource {

    private int priority = 0;


    protected AbstractLoggerConfigurationSource ( int prio ) {
        this.priority = prio;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws LoggerConfigurationException
     * 
     * @see eu.agno3.runtime.logging.config.LoggerConfigurationSource#getConfig()
     */
    @Override
    public abstract Map<String, ?> getConfig () throws LoggerConfigurationException;


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        return super.equals(obj);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * @note this class has a natural ordering that is inconsistent with equals.
     */
    @Override
    public int compareTo ( PrioritizedLoggerConfigurationSource o ) {

        int res = Integer.compare(this.getPriority(), o.getPriority());

        if ( res == 0 ) {
            return Integer.compare(System.identityHashCode(this), System.identityHashCode(o));
        }

        return res;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.logging.config.PrioritizedLoggerConfigurationSource#getPriority()
     */
    @Override
    public int getPriority () {
        return this.priority;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return super.hashCode();
    }

}
