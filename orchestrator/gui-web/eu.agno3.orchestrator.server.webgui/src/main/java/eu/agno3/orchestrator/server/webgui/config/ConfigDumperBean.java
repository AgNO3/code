/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.04.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.service.ConfigurationService;
import eu.agno3.orchestrator.config.model.realm.service.DefaultsService;
import eu.agno3.orchestrator.config.model.realm.service.EnforcementService;
import eu.agno3.orchestrator.config.model.realm.service.InheritanceService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.runtime.util.log.LogWriter;
import eu.agno3.runtime.xml.binding.XMLBindingException;
import eu.agno3.runtime.xml.binding.XmlMarshallingService;


/**
 * @author mbechler
 * 
 */
@ViewScoped
@Named ( "configDumper" )
public class ConfigDumperBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8553398801891516039L;
    /**
     * 
     */
    private static final String INDENT_AMOUNT_OPT = "{http://xml.apache.org/xslt}indent-amount"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(ConfigDumperBean.class);

    @Inject
    private CoreServiceProvider csp;

    @Inject
    private ServerServiceProvider ssp;

    private ConfigurationObject object;

    private UUID objectId;
    private UUID loadedObjectId;


    /**
     * @return the object
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public ConfigurationObject getObject () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( this.objectId == null ) {
            return null;
        }

        if ( this.loadedObjectId != null && this.loadedObjectId.equals(this.objectId) ) {
            return this.object;
        }

        this.loadedObjectId = this.objectId;
        this.object = this.ssp.getService(ConfigurationService.class).fetchById(this.objectId);

        return this.object;
    }


    /**
     * @return the objectId
     */
    public UUID getObjectId () {
        return this.objectId;
    }


    /**
     * @param objectId
     *            the objectId to set
     */
    public void setObjectId ( UUID objectId ) {
        this.objectId = objectId;
    }


    public static <T> void dumpConfig ( Logger l, Level p, XmlMarshallingService ms, T hc ) {
        if ( l.isEnabledFor(p) && ms != null ) {
            try {
                ms.marshall(hc, XMLOutputFactory.newInstance().createXMLStreamWriter(LogWriter.createWriter(l, p)));
            }
            catch (
                XMLBindingException |
                XMLStreamException |
                FactoryConfigurationError e ) {
                log.warn("Failed to dump configuration", e); //$NON-NLS-1$
            }

        }
    }


    protected String formatConfig ( ConfigurationObject config ) {

        if ( config == null ) {
            return StringUtils.EMPTY;
        }

        StringWriter os = new StringWriter();
        try {

            XMLStreamWriter stWrite = XMLOutputFactory.newInstance().createXMLStreamWriter(os);
            this.csp.getMarshallingService().marshall(config, stWrite);

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
            transformer.setOutputProperty(INDENT_AMOUNT_OPT, "2"); //$NON-NLS-1$

            StringWriter formattedOut = new StringWriter();
            transformer.transform(new StreamSource(new StringReader(os.toString())), new StreamResult(formattedOut));

            return formattedOut.toString();
        }
        catch (
            FactoryConfigurationError |
            XMLStreamException |
            XMLBindingException |
            TransformerException |
            TransformerFactoryConfigurationError e ) {
            log.warn("Failed to produce XML output:", e); //$NON-NLS-1$
            return StringUtils.EMPTY;
        }
    }


    public String getFormattedConfig () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        ConfigurationObject obj = this.getObject();
        if ( obj == null ) {
            return StringUtils.EMPTY;
        }
        return formatConfig(obj);
    }


    public String getFormattedDefaultConfig () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        ConfigurationObject obj = this.getObject();
        if ( obj == null ) {
            return StringUtils.EMPTY;
        }
        return formatConfig(this.ssp.getService(DefaultsService.class).getAppliedDefaults(obj, null));
    }


    public String getFormattedEffectiveConfig () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        ConfigurationObject obj = this.getObject();
        if ( obj == null ) {
            return StringUtils.EMPTY;
        }
        return formatConfig(this.ssp.getService(InheritanceService.class).getEffective(obj, null));

    }


    public String getFormattedInheritedConfig () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        ConfigurationObject obj = this.getObject();
        if ( obj == null ) {
            return StringUtils.EMPTY;
        }
        return formatConfig(this.ssp.getService(InheritanceService.class).getInherited(obj, null));

    }


    public String getFormattedEnforcedConfig () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        ConfigurationObject obj = this.getObject();
        if ( obj == null ) {
            return StringUtils.EMPTY;
        }
        return formatConfig(this.ssp.getService(EnforcementService.class).getAppliedEnforcement(obj));

    }

}