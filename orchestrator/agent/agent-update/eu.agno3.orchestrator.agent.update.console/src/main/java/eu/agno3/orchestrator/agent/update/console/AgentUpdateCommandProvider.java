/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.update.console;


import java.util.UUID;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.console.Session;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.connector.AgentServerConnector;
import eu.agno3.orchestrator.agent.update.UpdateDescriptorGenerator;
import eu.agno3.orchestrator.agent.update.UpdateInstallRunnableFactory;
import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.exceptions.JobRunnableException;
import eu.agno3.orchestrator.jobs.targets.AgentTarget;
import eu.agno3.orchestrator.system.update.UpdateDescriptor;
import eu.agno3.orchestrator.system.update.UpdateException;
import eu.agno3.orchestrator.system.update.jobs.UpdateInstallJob;
import eu.agno3.runtime.console.CommandProvider;
import eu.agno3.runtime.xml.XmlFormattingWriter;
import eu.agno3.runtime.xml.binding.XMLBindingException;
import eu.agno3.runtime.xml.binding.XmlMarshallingService;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
public class AgentUpdateCommandProvider implements CommandProvider {

    private static final Logger log = Logger.getLogger(AgentUpdateCommandProvider.class);
    private ComponentContext componentContext;

    private XmlMarshallingService xmlMarshalling;
    private UpdateDescriptorGenerator descriptorGenerator;

    private UpdateInstallRunnableFactory updateInstallFactory;

    private JobCoordinator coord;
    private AgentServerConnector connector;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.componentContext = ctx;
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        this.componentContext = null;
    }


    @Reference
    protected synchronized void setUpdateDescriptorGenerator ( UpdateDescriptorGenerator udg ) {
        this.descriptorGenerator = udg;
    }


    protected synchronized void unsetUpdateDescriptorGenerator ( UpdateDescriptorGenerator udg ) {
        if ( this.descriptorGenerator == udg ) {
            this.descriptorGenerator = null;
        }
    }


    @Reference
    protected synchronized void setXmlMarshallingService ( XmlMarshallingService xms ) {
        this.xmlMarshalling = xms;
    }


    protected synchronized void unsetXmlMarshallingService ( XmlMarshallingService xms ) {
        if ( this.xmlMarshalling == xms ) {
            this.xmlMarshalling = null;
        }
    }


    @Reference
    protected synchronized void setUpdateInstallFactory ( UpdateInstallRunnableFactory uif ) {
        this.updateInstallFactory = uif;
    }


    protected synchronized void unsetUpdateInstallFactory ( UpdateInstallRunnableFactory uif ) {
        if ( this.updateInstallFactory == uif ) {
            this.updateInstallFactory = null;
        }
    }


    @Reference
    protected synchronized void setJobCoordinator ( JobCoordinator jc ) {
        this.coord = jc;
    }


    protected synchronized void unsetJobCoordinator ( JobCoordinator jc ) {
        if ( this.coord == jc ) {
            this.coord = null;
        }
    }


    @Reference
    protected synchronized void setAgentConnector ( AgentServerConnector asc ) {
        this.connector = asc;
    }


    protected synchronized void unsetAgentConnector ( AgentServerConnector asc ) {
        if ( this.connector == asc ) {
            this.connector = null;
        }
    }


    /**
     * @return the componentContext
     */
    ComponentContext getComponentContext () {
        return this.componentContext;
    }


    /**
     * @return the descriptorGenerator
     */
    UpdateDescriptorGenerator getDescriptorGenerator () {
        return this.descriptorGenerator;
    }


    /**
     * @return the xmlMarshalling
     */
    XmlMarshallingService getXmlMarshalling () {
        return this.xmlMarshalling;
    }


    /**
     * @return the updateInstallFactory
     */
    UpdateInstallRunnableFactory getUpdateInstallFactory () {
        return this.updateInstallFactory;
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * @param descriptor
     * @param session
     * @throws JobRunnableException
     * @throws Exception
     */
    void installFromDescriptor ( UpdateDescriptor descriptor, Session session ) throws JobRunnableException, Exception {
        UpdateInstallJob updateInstallJob = new UpdateInstallJob();
        UUID localAgentId = this.connector.getComponentId();
        if ( localAgentId == null ) {
            session.getConsole().println("Agent not configured"); //$NON-NLS-1$
            return;
        }

        updateInstallJob.setTarget(new AgentTarget(localAgentId));
        updateInstallJob.setJobId(UUID.randomUUID());
        updateInstallJob.setDescriptor(descriptor);
        updateInstallJob.setDescriptorStream("UNCHECKED"); //$NON-NLS-1$

        JobInfo queuedJob = this.coord.queueJob(updateInstallJob);
        session.getConsole().println("Job is " + queuedJob); //$NON-NLS-1$
    }

    /**
     * Show server connector status
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "sysupdate", name = "generate", description = "Generate update descriptor" )
    public class GenerateCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;

        @Argument ( index = 0, name = "stream", required = true )
        private String stream;

        @Argument ( index = 1, name = "sequence", required = true )
        private Long sequence;

        @Argument ( index = 2, name = "imageType", required = false )
        private String imageType;


        @Override
        public Object execute () throws UpdateException, XMLBindingException, XMLStreamException {
            UpdateDescriptor desc = getDescriptorGenerator().generateDescriptor(this.stream, this.imageType, this.sequence);
            XMLStreamWriter sw = XMLOutputFactory.newInstance().createXMLStreamWriter(this.session.getConsole());
            getXmlMarshalling().marshall(desc, new XmlFormattingWriter(sw));
            return null;
        }
    }

    /**
     * 
     * @author mbechler
     *
     */
    @Command ( scope = "sysupdate", name = "install", description = "Install from descriptor" )
    public class InstallCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws Exception {
            UpdateDescriptor descriptor = null;
            installFromDescriptor(descriptor, this.session);
            return null;
        }

    }

    /**
     * 
     * @author mbechler
     *
     */
    @Command ( scope = "sysupdate", name = "installUnchecked", description = "Install all available updates" )
    public class InstallAllCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws Exception {
            installFromDescriptor(getDescriptorGenerator().generateDescriptor("UNCHECKED", null, 1), this.session); //$NON-NLS-1$
            return null;
        }

    }
}
