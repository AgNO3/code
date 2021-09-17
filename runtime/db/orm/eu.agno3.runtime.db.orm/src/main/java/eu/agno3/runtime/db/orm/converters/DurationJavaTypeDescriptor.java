/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.11.2015 by mbechler
 */
package eu.agno3.runtime.db.orm.converters;


import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.joda.time.Duration;


/**
 * @author mbechler
 *
 */
public class DurationJavaTypeDescriptor extends AbstractTypeDescriptor<Duration> {

    /**
     * 
     */
    public static final DurationJavaTypeDescriptor INSTANCE = new DurationJavaTypeDescriptor();


    /**
     */
    private DurationJavaTypeDescriptor () {
        super(Duration.class);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 3939947931184747921L;


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.type.descriptor.java.AbstractTypeDescriptor#areEqual(java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean areEqual ( Duration one, Duration another ) {
        if ( one == another ) {
            return true;
        }
        else if ( one == null || another == null ) {
            return false;
        }
        return one.isEqual(another);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.type.descriptor.java.JavaTypeDescriptor#toString(java.lang.Object)
     */
    @Override
    public String toString ( Duration value ) {
        return value.toString();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.type.descriptor.java.JavaTypeDescriptor#fromString(java.lang.String)
     */
    @Override
    public Duration fromString ( String string ) {
        return Duration.parse(string);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.type.descriptor.java.JavaTypeDescriptor#unwrap(java.lang.Object, java.lang.Class,
     *      org.hibernate.type.descriptor.WrapperOptions)
     */
    @Override
    public <X> X unwrap ( Duration value, Class<X> type, WrapperOptions options ) {
        if ( value == null ) {
            return null;
        }

        throw unknownUnwrap(type);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.type.descriptor.java.JavaTypeDescriptor#wrap(java.lang.Object,
     *      org.hibernate.type.descriptor.WrapperOptions)
     */
    @Override
    public <X> Duration wrap ( X value, WrapperOptions options ) {
        if ( value == null ) {
            return null;
        }

        throw unknownWrap(value.getClass());
    }
}
