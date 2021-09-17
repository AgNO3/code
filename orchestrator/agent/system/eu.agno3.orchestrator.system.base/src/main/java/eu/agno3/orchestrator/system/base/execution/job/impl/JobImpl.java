/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.job.impl;


import java.util.Arrays;

import eu.agno3.orchestrator.system.base.execution.Job;
import eu.agno3.orchestrator.system.base.execution.JobIterator;


/**
 * @author mbechler
 * 
 */
public class JobImpl implements Job {

    /**
     * 
     */
    private static final long serialVersionUID = -7784927100321359644L;

    private String[] flags;
    private String name;
    private JobIterator units;


    /**
     * @param units
     * @param name
     * @param flags
     */
    public JobImpl ( JobIterator units, String name, String[] flags ) {
        this.units = units;
        this.name = name;
        if ( flags != null ) {
            this.flags = Arrays.copyOf(flags, flags.length);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Job#getExecutionUnits()
     */
    @Override
    public JobIterator getExecutionUnits () {
        return this.units;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.Named#getName()
     */
    @Override
    public String getName () {
        return this.name;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.Job#getFlags()
     */
    @Override
    public String[] getFlags () {
        if ( this.flags == null ) {
            return new String[] {};
        }
        return Arrays.copyOf(this.flags, this.flags.length);
    }

}
