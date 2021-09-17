/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.internal.exec;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.coord.Executor;
import eu.agno3.orchestrator.jobs.coord.ExecutorFactory;
import eu.agno3.orchestrator.jobs.coord.JobForExecutionProvider;
import eu.agno3.orchestrator.jobs.coord.JobRunnableFactoryInternal;
import eu.agno3.orchestrator.jobs.coord.JobStateTracker;
import eu.agno3.orchestrator.jobs.coord.OutputHandlerFactory;


/**
 * @author mbechler
 * 
 */
@Component ( service = ExecutorFactory.class )
public class ExecutorFactoryImpl implements ExecutorFactory {

    private static final int DEFAULT_NUM_THREADS = 2;
    private static final long DEFAULT_SHUTDOWN_TIMEOUT = 30;
    private int numThreads;
    private long shutdownTimeout;
    private JobRunnableFactoryInternal runnableFactory;
    private OutputHandlerFactory outputHandlerFactory = new DefaultOutputHandlerFactory();


    @Reference
    protected synchronized void setJobRunnableFactory ( JobRunnableFactoryInternal jrf ) {
        this.runnableFactory = jrf;
    }


    protected synchronized void unsetJobRunnableFactory ( JobRunnableFactoryInternal jrf ) {
        if ( this.runnableFactory == jrf ) {
            this.runnableFactory = null;
        }
    }


    @Reference
    protected synchronized void setOutputHandlerFactory ( OutputHandlerFactory ohf ) {
        this.outputHandlerFactory = ohf;
    }


    protected synchronized void unsetOutputHandlerFactory ( OutputHandlerFactory ohf ) {
        if ( this.outputHandlerFactory == ohf ) {
            this.outputHandlerFactory = null;
        }
    }


    /**
     * 
     */
    public ExecutorFactoryImpl () {
        this(DEFAULT_NUM_THREADS, DEFAULT_SHUTDOWN_TIMEOUT);
    }


    /**
     * @param ohf
     * @param jrf
     */
    public ExecutorFactoryImpl ( OutputHandlerFactory ohf, JobRunnableFactoryInternal jrf ) {
        this();
        this.outputHandlerFactory = ohf;
        this.runnableFactory = jrf;
    }


    /**
     * @param numThreads
     * @param shutdownTimeout
     */
    public ExecutorFactoryImpl ( int numThreads, long shutdownTimeout ) {
        this.numThreads = numThreads;
        this.shutdownTimeout = shutdownTimeout;
    }


    @Override
    public boolean canRun ( Job j ) {
        return this.runnableFactory.hasRunnable(j);
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.ExecutorFactory#makeExecutor(eu.agno3.orchestrator.jobs.coord.JobForExecutionProvider,
     *      eu.agno3.orchestrator.jobs.coord.JobStateTracker)
     */
    @Override
    public Executor makeExecutor ( JobForExecutionProvider prov, JobStateTracker jst ) {
        return new ExecutorImpl(prov, jst, this.runnableFactory, this.outputHandlerFactory, this.numThreads, this.shutdownTimeout);
    }

}
