/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.validation;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.ConstraintViolation;
import javax.validation.Path;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( ViolationEntry.class )
public class ViolationEntryImpl implements ViolationEntry {

    /**
     * 
     */
    private static final long serialVersionUID = -1265421475703481380L;

    private ViolationLevel level;
    private String objectType;
    private String messageTemplate;
    private List<String> messageArgs;
    private List<String> path;


    /**
     * 
     */
    public ViolationEntryImpl () {}


    /**
     * @param level
     * @param objectType
     * @param objectPath
     * @param messageTemplate
     * @param messageArgs
     */
    public ViolationEntryImpl ( ViolationLevel level, String objectType, List<String> objectPath, String messageTemplate, List<String> messageArgs ) {
        super();
        this.level = level;
        this.objectType = objectType;
        this.path = objectPath;
        this.messageTemplate = messageTemplate;
        this.messageArgs = messageArgs;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.validation.ViolationEntry#getLevel()
     */
    @Override
    public ViolationLevel getLevel () {
        return this.level;
    }


    /**
     * @param level
     *            the level to set
     */
    public void setLevel ( ViolationLevel level ) {
        this.level = level;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.validation.ViolationEntry#getObjectType()
     */
    @Override
    public String getObjectType () {
        return this.objectType;
    }


    /**
     * @return the path
     */
    @Override
    public List<String> getPath () {
        return this.path;
    }


    /**
     * @param path
     *            the path to set
     */
    public void setPath ( List<String> path ) {
        this.path = path;
    }


    /**
     * @return the messageTemplate
     */
    @Override
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
    @Override
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
            "Violation (%s): %s at %s", //$NON-NLS-1$
            this.level,
            this.messageTemplate,
            StringUtils.join(this.path, '/'));
    }


    /**
     * @param violation
     * @param objectTypeName
     * @return a violation entry for a constraint violation
     */
    public static ViolationEntry fromConstraintViolation ( ConstraintViolation<?> violation, String objectTypeName ) {
        return new ViolationEntryImpl(
            ViolationLevel.ERROR,
            objectTypeName,
            mapPropertyPath(violation.getPropertyPath()),
            violation.getMessageTemplate(),
            new ArrayList<String>());
    }


    /**
     * @param propertyPath
     * @return
     */
    private static List<String> mapPropertyPath ( Path propertyPath ) {
        return Arrays.asList(StringUtils.split(propertyPath.toString(), '.'));
    }
}
