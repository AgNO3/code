/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.06.2016 by mbechler
 */
package eu.agno3.orchestrator.agent.update.console;


import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.console.Session;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.update.ServiceReconfigurator;
import eu.agno3.orchestrator.agent.update.units.ReconfigureService;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepository;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepositoryException;
import eu.agno3.orchestrator.jobs.agent.system.ExtraServiceExecutionConfigWrapper;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfig;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.Runner;
import eu.agno3.orchestrator.system.base.execution.RunnerFactory;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.runtime.console.CommandProvider;


/**
 * @author mbechler
 *
 */
@Component ( service = CommandProvider.class, immediate = true )
public class ReconfigurationCommandProvider implements CommandProvider {

    private ServiceReconfigurator serviceReconfigurator;
    private RunnerFactory runnerFactory;
    private ExecutionConfig execConfig;
    private ConfigRepository configRepo;
    private ServiceManager serviceManager;


    @Reference
    protected synchronized void setServiceReconfigurator ( ServiceReconfigurator sr ) {
        this.serviceReconfigurator = sr;
    }


    protected synchronized void unsetServiceReconfigurator ( ServiceReconfigurator sr ) {
        if ( this.serviceReconfigurator == sr ) {
            this.serviceReconfigurator = null;
        }
    }


    @Reference
    protected synchronized void setRunnerFactory ( RunnerFactory rf ) {
        this.runnerFactory = rf;
    }


    protected synchronized void unsetRunnerFactory ( RunnerFactory rf ) {
        if ( this.runnerFactory == rf ) {
            this.runnerFactory = null;
        }
    }


    @Reference
    protected synchronized void setExecConfig ( ExecutionConfig ec ) {
        this.execConfig = ec;
    }


    protected synchronized void unsetExecConfig ( ExecutionConfig ec ) {
        if ( this.execConfig == ec ) {
            this.execConfig = null;
        }
    }


    @Reference
    protected synchronized void setConfigRepository ( ConfigRepository cr ) {
        this.configRepo = cr;
    }


    protected synchronized void unsetConfigRepository ( ConfigRepository cr ) {
        if ( this.configRepo == cr ) {
            this.configRepo = null;
        }
    }


    @Reference
    protected synchronized void setServiceManager ( ServiceManager sm ) {
        this.serviceManager = sm;
    }


    protected synchronized void unsetServiceManager ( ServiceManager sm ) {
        if ( this.serviceManager == sm ) {
            this.serviceManager = null;
        }
    }


    /**
     * @return the serviceReconfigurator
     */
    ServiceReconfigurator getServiceReconfigurator () {
        return this.serviceReconfigurator;
    }


    /**
     * @return the runnerFactory
     */
    RunnerFactory getRunnerFactory () {
        return this.runnerFactory;
    }


    /**
     * @return the execConfig
     */
    ExecutionConfig getExecConfig () {
        return this.execConfig;
    }


    /**
     * @return the configRepo
     */
    ConfigRepository getConfigRepo () {
        return this.configRepo;
    }


    /**
     * @return the serviceManager
     */
    ServiceManager getServiceManager () {
        return this.serviceManager;
    }

    /**
     * Reconfigure
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "service", name = "reconfigure", description = "Reconfigure service" )
    public class ReconfigureCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;

        @Argument ( index = 0, name = "serviceType", required = true )
        private String serviceType;


        @Override
        public Object execute () throws UnitInitializationFailedException, ConfigRepositoryException, InvalidUnitConfigurationException {
            Runner r = getRunnerFactory().createRunner();
            JobBuilder jb = r.makeJobBuilder();
            jb.withService(ServiceReconfigurator.class, getServiceReconfigurator());
            jb.withService(ServiceManager.class, getServiceManager());
            jb.add(ReconfigureService.class).service(getConfigRepo().getSingletonServiceByType(this.serviceType));
            r.run(
                jb.getJob(),
                new ConsoleOut(this.session.getConsole()),
                new ExtraServiceExecutionConfigWrapper(getExecConfig(), jb.getServices()),
                null);
            return null;
        }
    }
}
