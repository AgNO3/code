/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.11.2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


/**
 * @author mbechler
 * @param <T>
 *
 */
public interface BaseMappingBuilder <T extends BaseMappingBuilder<T>> {

    /**
     * @param type
     * @return self
     */
    T type ( String type );


    /**
     * 
     * @param analyzer
     * @return self
     */
    T analyzer ( String analyzer );


    /**
     * 
     * @return self
     */
    T disableDocValues ();


    /**
     * 
     * @return self
     */
    T disableNorms ();


    /**
     * 
     * @return self
     */
    T disable ();


    /**
     * 
     * @return self
     */
    T dontIncludeInAll ();


    /**
     * 
     * @return self
     */
    T dontIndex ();


    /**
     * 
     * @param b
     * @return self
     */
    T boost ( float b );


    /**
     * @param length
     * @return self
     */
    T maxLength ( int length );


    /**
     * @param name
     * @return field mapping builder for subfield
     */
    FieldMappingBuilder subfield ( String name );

}
