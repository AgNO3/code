/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.11.2015 by mbechler
 */
package eu.agno3.runtime.db.orm.converters;


import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public class DateTimeJavaTypeDescriptor extends AbstractTypeDescriptor<DateTime> {

    /**
     * 
     */
    public static final DateTimeJavaTypeDescriptor INSTANCE = new DateTimeJavaTypeDescriptor();


    /**
     */
    private DateTimeJavaTypeDescriptor () {
        super(DateTime.class);
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
    public boolean areEqual ( DateTime one, DateTime another ) {
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
    public String toString ( DateTime value ) {
        return value.toString();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.type.descriptor.java.JavaTypeDescriptor#fromString(java.lang.String)
     */
    @Override
    public DateTime fromString ( String string ) {
        return DateTime.parse(string);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.type.descriptor.java.JavaTypeDescriptor#unwrap(java.lang.Object, java.lang.Class,
     *      org.hibernate.type.descriptor.WrapperOptions)
     */
    @Override
    public <X> X unwrap ( DateTime value, Class<X> type, WrapperOptions options ) {
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
    public <X> DateTime wrap ( X value, WrapperOptions options ) {
        if ( value == null ) {
            return null;
        }

        throw unknownWrap(value.getClass());
    }
}
