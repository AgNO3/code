/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 10, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.validation.internal;


import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.events.ConfigTestResultUpdateEvent;
import eu.agno3.orchestrator.config.model.jobs.ConfigurationTestJob;
import eu.agno3.orchestrator.config.model.msg.ConfigTestResultUpdateRequest;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestAsyncHandler;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestContext;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginAsync;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginRegistry;
import eu.agno3.orchestrator.config.model.validation.ConfigTestParams;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResult;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResultImpl;
import eu.agno3.orchestrator.config.model.validation.ConfigTestState;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.JobType;
import eu.agno3.orchestrator.jobs.exceptions.JobRunnableException;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.client.MessagingClient;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
@Component ( service = JobRunnableFactory.class, property = "jobType=eu.agno3.orchestrator.config.model.jobs.ConfigurationTestJob" )
@JobType ( value = ConfigurationTestJob.class )
public class ConfigTestJobRunnableFactory implements JobRunnableFactory<ConfigurationTestJob> {

    private static final Logger log = Logger.getLogger(ConfigTestJobRunnableFactory.class);
    private ConfigTestPluginRegistry testPluginRegistry;

    private MessagingClient<MessageSource> msgClient;


    @Reference
    protected synchronized void setTestPluginRegistry ( ConfigTestPluginRegistry ctpr ) {
        this.testPluginRegistry = ctpr;
    }


    protected synchronized void unsetTestPluginRegistry ( ConfigTestPluginRegistry ctpr ) {
        if ( this.testPluginRegistry == ctpr ) {
            this.testPluginRegistry = null;
        }
    }


    @Reference
    protected synchronized void setMessagingClient ( MessagingClient<MessageSource> mc ) {
        this.msgClient = mc;
    }


    protected synchronized void unsetMessagingClient ( MessagingClient<MessageSource> mc ) {
        if ( this.msgClient == mc ) {
            this.msgClient = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.JobRunnableFactory#getRunnableForJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public JobRunnable getRunnableForJob ( ConfigurationTestJob j ) throws JobRunnableException {
        ConfigurationObject effc = j.getEffectiveConfig();
        ConfigTestResultImpl r = j.getInitialResult();
        r.setTestId(j.getJobId());
        ConfigTestPlugin<? extends ConfigurationObject> tp = this.testPluginRegistry.getTestPlugin(effc.getType());
        return new ConfigTestRunnable(tp, effc, r, j.getParameters(), j.getOwner());
    }


    /**
     * @param r
     */
    void pushState ( ConfigTestResultImpl r ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Updating test %s result state %s", r.getTestId(), r.getState())); //$NON-NLS-1$
        }

        ConfigTestResultUpdateEvent ctr = new ConfigTestResultUpdateEvent(this.msgClient.getMessageSource());
        ctr.setSequence(System.currentTimeMillis());
        ctr.setResult(r);
        try {
            this.msgClient.publishEvent(ctr);
        }
        catch (
            MessagingException |
            InterruptedException e ) {
            log.debug("Failed to publish config test result update", e); //$NON-NLS-1$
        }
    }


    void pushStateSync ( ConfigTestResultImpl r ) throws MessagingException, InterruptedException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Updating test %s result state %s (sync)", r.getTestId(), r.getState())); //$NON-NLS-1$
        }

        ConfigTestResultUpdateRequest ctr = new ConfigTestResultUpdateRequest(this.msgClient.getMessageSource());
        ctr.setSequence(System.currentTimeMillis());
        ctr.setResult(r);
        this.msgClient.sendMessage(ctr);
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }

    private class ConfigTestRunnable implements JobRunnable {

        private ConfigTestPlugin<? extends ConfigurationObject> tp;
        private ConfigurationObject effc;
        private ConfigTestResultImpl result;
        private ConfigTestParams params;
        private ConfigTestContext context;


        /**
         * @param tp
         * @param effc
         * @param result
         * @param params
         * @param userPrincipal
         */
        public ConfigTestRunnable ( ConfigTestPlugin<? extends ConfigurationObject> tp, ConfigurationObject effc, ConfigTestResultImpl result,
                ConfigTestParams params, UserPrincipal userPrincipal ) {
            this.tp = tp;
            this.effc = effc;
            this.result = result;
            this.params = params;
            this.context = new ConfigTestContextImpl(userPrincipal);
        }


        /**
         * {@inheritDoc}
         *
         * @see eu.agno3.orchestrator.jobs.exec.JobRunnable#run(eu.agno3.orchestrator.jobs.exec.JobOutputHandler)
         */
        @Override
        public JobState run ( @NonNull JobOutputHandler outHandler ) throws Exception {

            ConfigTestResult btr = this.result.withType("base"); //$NON-NLS-1$

            if ( this.tp == null ) {
                btr.error("NO_TEST"); //$NON-NLS-1$
                btr.state(ConfigTestState.NO_TEST);
                pushState(this.result);
                outHandler.logLineError("No test plugin found for " + this.effc.getType().getName()); //$NON-NLS-1$
                return JobState.FINISHED;
            }

            if ( !this.tp.getTargetType().equals(this.effc.getType()) ) {
                throw new ModelServiceException("Test plugin type mismatch"); //$NON-NLS-1$
            }

            try {
                btr.state(ConfigTestState.RUNNING);
                outHandler.logLineInfo("Running test plugin " + this.tp.getClass().getName()); //$NON-NLS-1$
                ConfigTestResultImpl test;
                ConfigTestAsyncHandler async = new AsyncHandler();
                test = doRunTest(async);
                if ( test == null ) {
                    btr.state(ConfigTestState.UNKNOWN);
                    pushStateSync(this.result);
                    outHandler.logLineError("Test plugin did not produce output"); //$NON-NLS-1$
                    return JobState.FAILED;
                }
                pushStateSync(this.result);
                return JobState.FINISHED;
            }
            catch ( Throwable e ) {
                outHandler.logLineError("Error running test plugin", e); //$NON-NLS-1$
                getLog().error("Uncaught exception in test plugin " + this.tp.getClass().getSimpleName(), e); //$NON-NLS-1$
                btr.error("FAIL_UNKNOWN", e.getMessage()); //$NON-NLS-1$
                try {
                    pushStateSync(this.result);
                }
                catch ( Exception e2 ) {
                    getLog().warn("Failed to push state to server", e2); //$NON-NLS-1$
                }
                return JobState.FAILED;
            }
        }


        /**
         * @param async
         * @return
         * @throws ModelServiceException
         */
        @SuppressWarnings ( "unchecked" )
        private ConfigTestResultImpl doRunTest ( ConfigTestAsyncHandler async ) throws ModelServiceException {
            ConfigTestResultImpl test;
            if ( this.tp instanceof ConfigTestPluginAsync ) {
                test = ( (ConfigTestPluginAsync<ConfigurationObject>) this.tp ).testAsync(this.effc, this.context, this.params, this.result, async)
                        .get();
            }
            else {
                test = ( (ConfigTestPlugin<ConfigurationObject>) this.tp ).test(this.effc, this.context, this.result, this.params).get();
            }
            return test;
        }

    }

    private class AsyncHandler implements ConfigTestAsyncHandler {

        /**
         * 
         */
        public AsyncHandler () {}


        /**
         * 
         * {@inheritDoc}
         *
         * @see eu.agno3.orchestrator.config.model.realm.validation.ConfigTestAsyncHandler#update(eu.agno3.orchestrator.config.model.validation.ConfigTestResult)
         */
        @Override
        public ConfigTestResultImpl update ( ConfigTestResult tr ) {
            ConfigTestResultImpl r = tr.get();
            pushState(r);
            return r;
        }

    }

}
