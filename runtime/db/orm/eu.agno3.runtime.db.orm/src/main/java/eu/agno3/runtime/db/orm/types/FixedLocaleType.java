/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.07.2015 by mbechler
 */
package eu.agno3.runtime.db.orm.types;


import java.util.Locale;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.LiteralType;
import org.hibernate.type.StringType;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;


/**
 * @author mbechler
 *
 */
public class FixedLocaleType extends AbstractSingleColumnStandardBasicType<Locale> implements LiteralType<Locale> {

    /**
     * 
     */
    private static final long serialVersionUID = 6483724363541821835L;
    /**
     * 
     */
    public static final FixedLocaleType INSTANCE = new FixedLocaleType();


    /**
     * 
     */
    public FixedLocaleType () {
        super(VarcharTypeDescriptor.INSTANCE, FixedLocaleTypeDescriptor.OVERRIDE_INSTANCE);
    }


    @Override
    public String getName () {
        return "locale"; //$NON-NLS-1$
    }


    @Override
    protected boolean registerUnderJavaType () {
        return true;
    }


    @Override
    public String objectToSQLString ( Locale value, Dialect dialect ) throws Exception {
        return StringType.INSTANCE.objectToSQLString(toString(value), dialect);
    }
}