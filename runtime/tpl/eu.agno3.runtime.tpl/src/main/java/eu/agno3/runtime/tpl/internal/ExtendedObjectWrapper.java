/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.03.2015 by mbechler
 */
package eu.agno3.runtime.tpl.internal;


import org.joda.time.base.AbstractInstant;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;


/**
 * @author mbechler
 *
 */
public class ExtendedObjectWrapper extends DefaultObjectWrapper {

    /**
     * @param ver
     * 
     */
    public ExtendedObjectWrapper ( Version ver ) {
        super(ver);
    }


    /**
     * {@inheritDoc}
     *
     * @see freemarker.template.DefaultObjectWrapper#handleUnknownType(java.lang.Object)
     */
    @Override
    protected TemplateModel handleUnknownType ( Object obj ) throws TemplateModelException {
        if ( obj instanceof AbstractInstant ) {
            return new DateTimeAdapter((AbstractInstant) obj, this);
        }

        return super.handleUnknownType(obj);
    }

}
