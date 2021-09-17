/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.12.2015 by mbechler
 */
package eu.agno3.runtime.xml.schema;


/**
 * @author mbechler
 *
 */
public enum SchemaValidationLevel {

    /**
     * No schema validation is performed
     */
    OFF,

    /**
     * Schema is validated, errors are logged
     */
    VALIDATE,

    /**
     * Schema is validated, errors cause exceptions
     */
    ENFORCE
}
