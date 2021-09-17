/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.orchestrator.bootstrap.msg;


import java.security.cert.X509Certificate;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfiguration;
import eu.agno3.runtime.messaging.addressing.DefaultMessageTarget;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageTarget;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 *
 */
public class BootstrapRequestMessage extends XmlMarshallableMessage<@NonNull AgentMessageSource>
        implements RequestMessage<@NonNull AgentMessageSource, BootstrapResponseMessage, DefaultXmlErrorResponseMessage> {

    private String adminPassword;
    private String imageType;
    private X509Certificate caCertificate;
    private X509Certificate webCertificate;

    private ServiceStructuralObject hostConfigService;
    private HostConfiguration bootstrapHostConfig;
    private ServiceStructuralObject serverService;
    private OrchestratorConfiguration bootstrapServerConfig;
    private boolean autoRun;


    /**
     * 
     */
    public BootstrapRequestMessage () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public BootstrapRequestMessage ( @NonNull AgentMessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public BootstrapRequestMessage ( @NonNull AgentMessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public BootstrapRequestMessage ( @NonNull AgentMessageSource origin ) {
        super(origin);
    }


    /**
     * @return the autoRun
     */
    public boolean getAutoRun () {
        return this.autoRun;
    }


    /**
     * @param autoRun
     */
    public void setAutoRun ( boolean autoRun ) {
        this.autoRun = autoRun;
    }


    /**
     * @param adminPassword
     *            the adminPassword to set
     */
    public void setAdminPassword ( String adminPassword ) {
        this.adminPassword = adminPassword;
    }


    /**
     * @return the adminPassword
     */
    public String getAdminPassword () {
        return this.adminPassword;
    }


    /**
     * @return the image type
     */
    public String getImageType () {
        return this.imageType;
    }


    /**
     * @param imageType
     *            the imageType to set
     */
    public void setImageType ( String imageType ) {
        this.imageType = imageType;
    }


    /**
     * @return the hostConfigService
     */
    public ServiceStructuralObject getHostConfigService () {
        return this.hostConfigService;
    }


    /**
     * @param hostService
     */
    public void setHostConfigService ( ServiceStructuralObject hostService ) {
        this.hostConfigService = hostService;
    }


    /**
     * @param bootstrapHostConfig
     *            the bootstrapHostConfig to set
     */
    public void setBootstrapHostConfig ( HostConfiguration bootstrapHostConfig ) {
        this.bootstrapHostConfig = bootstrapHostConfig;
    }


    /**
     * @return the bootstrapHostConfig
     */
    public HostConfiguration getBootstrapHostConfig () {
        return this.bootstrapHostConfig;
    }


    /**
     * @return the caCertificate
     */
    public X509Certificate getCaCertificate () {
        return this.caCertificate;
    }


    /**
     * @param caCertificate
     *            the caCertificate to set
     */
    public void setCaCertificate ( X509Certificate caCertificate ) {
        this.caCertificate = caCertificate;
    }


    /**
     * @param webCert
     */
    public void setWebCertificate ( X509Certificate webCert ) {
        this.webCertificate = webCert;
    }


    /**
     * @return the webCertificate
     */
    public X509Certificate getWebCertificate () {
        return this.webCertificate;
    }


    /**
     * @return the serverService
     */
    public ServiceStructuralObject getServerService () {
        return this.serverService;
    }


    /**
     * @param serverService
     */
    public void setServerService ( ServiceStructuralObject serverService ) {
        this.serverService = serverService;
    }


    /**
     * @return the bootstrapServerConfig
     */
    public OrchestratorConfiguration getBootstrapServerConfig () {
        return this.bootstrapServerConfig;
    }


    /**
     * @param serverConfiguration
     */
    public void setBootstrapServerConfig ( @NonNull OrchestratorConfiguration serverConfiguration ) {
        this.bootstrapServerConfig = serverConfiguration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getResponseType()
     */
    @Override
    public Class<BootstrapResponseMessage> getResponseType () {
        return BootstrapResponseMessage.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getErrorResponseType()
     */
    @Override
    public Class<DefaultXmlErrorResponseMessage> getErrorResponseType () {
        return DefaultXmlErrorResponseMessage.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getTarget()
     */
    @Override
    public MessageTarget getTarget () {
        return new DefaultMessageTarget();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getReplyTimeout()
     */
    @Override
    public long getReplyTimeout () {
        return 5000;
    }

}
