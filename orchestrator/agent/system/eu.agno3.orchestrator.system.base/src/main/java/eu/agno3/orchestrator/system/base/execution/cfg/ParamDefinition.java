/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.cfg;


import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.RequiredParameterMissingException;


/**
 * @author mbechler
 * @param <T>
 *            value type
 * 
 */
public final class ParamDefinition <T> {

    /**
     * 
     */
    private Class<T> paramType;
    private T defaultValue;
    private boolean required;


    /**
     * @param paramType
     * @param required
     * @param defaultValue
     */
    public ParamDefinition ( Class<T> paramType, boolean required, T defaultValue ) {
        this.paramType = paramType;
        this.required = required;
        this.defaultValue = defaultValue;
    }


    /**
     * @param paramType
     * @param defaultValue
     */
    public ParamDefinition ( Class<T> paramType, T defaultValue ) {
        this(paramType, false, defaultValue);
    }


    /**
     * 
     * @return the parameter type
     */
    public Class<T> getParamType () {
        return this.paramType;
    }


    /**
     * 
     * @param value
     * @throws InvalidParameterException
     * @throws RuntimeException
     *             if validation fails
     */
    public void validateValue ( Object value ) throws InvalidParameterException {
        if ( !this.paramType.isAssignableFrom(value.getClass()) ) {
            throw new InvalidParameterException(String.format("Parameter has wrong type %s (expected: %s)", //$NON-NLS-1$
                value.getClass().getName(),
                this.paramType.getName()));
        }
    }


    /**
     * 
     * @param value
     * @return the processed value, potentially a default value
     * @throws RequiredParameterMissingException
     * @throws RuntimeException
     *             if the value is required but not present
     */
    public T processValue ( Object value ) throws RequiredParameterMissingException {
        if ( value == null && this.required ) {
            throw new RequiredParameterMissingException("Missing a required parameter"); //$NON-NLS-1$
        }
        else if ( value == null ) {
            return this.defaultValue;
        }

        return this.paramType.cast(value);
    }
}
