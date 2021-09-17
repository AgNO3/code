/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 25, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.validation;


import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@SafeSerialization
public class ConfigTestResultEntry implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2250851832212516417L;

    private ConfigTestResultSeverity severity = ConfigTestResultSeverity.ERROR;
    private String objectType;
    private String messageTemplate;
    private List<String> messageArgs;


    /**
     * 
     */
    public ConfigTestResultEntry () {}


    /**
     * 
     * @param sev
     * @param msgTemplate
     * @param args
     */
    public ConfigTestResultEntry ( ConfigTestResultSeverity sev, String msgTemplate, String... args ) {
        this.severity = sev;
        this.messageTemplate = msgTemplate;
        this.messageArgs = Arrays.asList(args);
    }


    /**
     * 
     * @return message severity
     */
    public ConfigTestResultSeverity getSeverity () {
        return this.severity;
    }


    /**
     * @param sev
     *            the level to set
     */
    public void setSeverity ( ConfigTestResultSeverity sev ) {
        this.severity = sev;
    }


    /**
     * 
     * @return target object type
     */
    public String getObjectType () {
        return this.objectType;
    }


    /**
     * @return the messageTemplate
     */
    public String getMessageTemplate () {
        return this.messageTemplate;
    }


    /**
     * @param messageTemplate
     *            the messageTemplate to set
     */
    public void setMessageTemplate ( String messageTemplate ) {
        this.messageTemplate = messageTemplate;
    }


    /**
     * @return the messageArgs
     */
    public List<String> getMessageArgs () {
        return this.messageArgs;
    }


    /**
     * @param messageArgs
     *            the messageArgs to set
     */
    public void setMessageArgs ( List<String> messageArgs ) {
        this.messageArgs = messageArgs;
    }


    /**
     * @param objectType
     *            the objectType to set
     */
    public void setObjectType ( String objectType ) {
        this.objectType = objectType;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format(
            "Result (%s): %s", //$NON-NLS-1$
            this.severity,
            this.messageTemplate);
    }

}
