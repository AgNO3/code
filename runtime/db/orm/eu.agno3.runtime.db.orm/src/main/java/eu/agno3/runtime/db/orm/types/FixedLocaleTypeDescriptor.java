/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.07.2015 by mbechler
 */
package eu.agno3.runtime.db.orm.types;


import java.util.Locale;

import org.hibernate.type.descriptor.java.LocaleTypeDescriptor;


/**
 * @author mbechler
 *
 */
public class FixedLocaleTypeDescriptor extends LocaleTypeDescriptor {

    /**
     * 
     */
    public static final FixedLocaleTypeDescriptor OVERRIDE_INSTANCE = new FixedLocaleTypeDescriptor();

    /**
     * 
     */
    private static final long serialVersionUID = 4047661798119035403L;


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.type.descriptor.java.LocaleTypeDescriptor#fromString(java.lang.String)
     */
    @Override
    public Locale fromString ( String val ) {
        if ( val != null && val.isEmpty() ) {
            return Locale.ROOT;
        }
        return super.fromString(val);
    }
}
