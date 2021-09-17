/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2013 by mbechler
 */
package eu.agno3.runtime.logging.config.internal;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import eu.agno3.runtime.logging.config.LoggerConfigObserver;
import eu.agno3.runtime.logging.config.LoggerConfigObserverProxy;
import eu.agno3.runtime.logging.config.LoggerConfigurationException;
import eu.agno3.runtime.logging.config.LoggerConfigurationSource;
import eu.agno3.runtime.logging.config.PrioritizedLoggerConfigurationSource;


/**
 * @author mbechler
 */
public class DelegatingLoggerConfigurationSource extends Observable implements LoggerConfigurationSource, LoggerConfigObserver {

    private static final Logger log = Logger.getLogger(DelegatingLoggerConfigurationSource.class);

    private final SortedSet<PrioritizedLoggerConfigurationSource> sources = new TreeSet<>();
    private final LoggerConfigObserverProxy observerProxy = new LoggerConfigObserverProxy(this);


    /**
     * Initialize the delegating configuration source
     * 
     * @param configurationSources
     *            any number of configuration sources backing this one
     */
    public DelegatingLoggerConfigurationSource ( PrioritizedLoggerConfigurationSource... configurationSources ) {
        super();
        for ( int i = 0; i < configurationSources.length; i++ ) {
            this.addSourceInternal(configurationSources[ i ]);
        }
    }


    /**
     * Adds source and registers observer if possible
     * 
     * @param source
     *            Source to add
     */
    private void addSourceInternal ( PrioritizedLoggerConfigurationSource source ) {
        if ( log.isTraceEnabled() ) {
            log.trace("Adding source " + source.getClass().getName()); //$NON-NLS-1$
        }
        this.sources.add(source);

        if ( source instanceof Observable ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Adding observer to source " + source.getClass().getName()); //$NON-NLS-1$
            }
            Observable obs = (Observable) source;
            obs.addObserver(this.observerProxy);
        }
    }


    /**
     * Add another configuration source
     * 
     * @param source
     *            A configuration to add to the backing instances
     */
    public void addSource ( PrioritizedLoggerConfigurationSource source ) {
        synchronized ( this.sources ) {
            this.addSourceInternal(source);
        }
        this.setChanged();
        this.notifyObservers(this);
    }


    /**
     * Remove a configuration source
     * 
     * @param source
     *            Configuration source to remove
     */
    public void removeSource ( PrioritizedLoggerConfigurationSource source ) {
        if ( source instanceof Observable ) {
            ( (Observable) source ).deleteObserver(this.observerProxy);
        }
        synchronized ( this.sources ) {
            this.sources.remove(source);
        }
        this.setChanged();
        this.notifyObservers(this);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws LoggerConfigurationException
     * 
     * @see eu.agno3.runtime.logging.config.LoggerConfigurationSource#getConfig()
     */
    @Override
    public Map<String, ?> getConfig () throws LoggerConfigurationException {

        Map<String, Object> config = new HashMap<>();

        synchronized ( this.sources ) {
            for ( PrioritizedLoggerConfigurationSource source : this.sources ) {
                for ( Entry<String, ?> entry : source.getConfig().entrySet() ) {
                    config.put(entry.getKey(), entry.getValue());
                }
            }
        }

        if ( log.isTraceEnabled() ) {
            for ( Entry<String, ?> entry : config.entrySet() ) {
                log.trace(String.format("%s : %s", entry.getKey(), entry.getValue())); //$NON-NLS-1$
            }
        }

        return config;
    }


    protected Set<PrioritizedLoggerConfigurationSource> getSources () {
        synchronized ( this.sources ) {
            return new HashSet<>(this.sources);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.logging.config.LoggerConfigObserver#configurationUpdated(eu.agno3.runtime.logging.config.LoggerConfigurationSource)
     */
    @Override
    public void configurationUpdated ( LoggerConfigurationSource s ) {
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Configuration source %s changed", s.getClass().getName())); //$NON-NLS-1$
        }
        this.setChanged();
        this.notifyObservers(s);
    }

}
