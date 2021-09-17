/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( RuntimeConfiguration.class )
@Entity
@Table ( name = "config_runtime" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "rtc" )
public class RuntimeConfigurationImpl extends AbstractConfigurationObject<RuntimeConfiguration>
        implements RuntimeConfiguration, RuntimeConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 1971317391839741587L;

    private Boolean autoMemoryLimit;
    private Long memoryLimit;

    private Set<String> debugPackages = new HashSet<>();
    private Set<String> tracePackages = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<RuntimeConfiguration> getType () {
        return RuntimeConfiguration.class;
    }


    /**
     * @return the autoMemoryLimit
     */
    @Override
    public Boolean getAutoMemoryLimit () {
        return this.autoMemoryLimit;
    }


    /**
     * @param autoMemoryLimit
     *            the autoMemoryLimit to set
     */
    @Override
    public void setAutoMemoryLimit ( Boolean autoMemoryLimit ) {
        this.autoMemoryLimit = autoMemoryLimit;
    }


    /**
     * @return the memoryLimit
     */
    @Override
    public Long getMemoryLimit () {
        return this.memoryLimit;
    }


    /**
     * @param memoryLimit
     *            the memoryLimit to set
     */
    @Override
    public void setMemoryLimit ( Long memoryLimit ) {
        this.memoryLimit = memoryLimit;
    }


    /**
     * @return the debugPackages
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_runtime_debug_pkg" )
    public Set<String> getDebugPackages () {
        return this.debugPackages;
    }


    /**
     * @param debugPackages
     *            the debugPackages to set
     */
    @Override
    public void setDebugPackages ( Set<String> debugPackages ) {
        this.debugPackages = debugPackages;
    }


    /**
     * @return the tracePackages
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_runtime_trace_pkg" )
    public Set<String> getTracePackages () {
        return this.tracePackages;
    }


    /**
     * @param tracePackages
     *            the tracePackages to set
     */
    @Override
    public void setTracePackages ( Set<String> tracePackages ) {
        this.tracePackages = tracePackages;
    }

}
