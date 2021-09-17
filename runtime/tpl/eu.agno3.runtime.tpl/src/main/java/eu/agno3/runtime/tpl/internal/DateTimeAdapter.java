/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.03.2015 by mbechler
 */
package eu.agno3.runtime.tpl.internal;


import java.util.Date;

import org.joda.time.base.AbstractInstant;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.WrappingTemplateModel;


/**
 * @author mbechler
 *
 */
public class DateTimeAdapter extends WrappingTemplateModel implements TemplateDateModel {

    private AbstractInstant dateTime;


    /**
     * @param obj
     * @param ow
     * 
     */
    public DateTimeAdapter ( AbstractInstant obj, ObjectWrapper ow ) {
        super(ow);
        this.dateTime = obj;
    }


    /**
     * {@inheritDoc}
     *
     * @see freemarker.template.TemplateDateModel#getAsDate()
     */
    @Override
    public Date getAsDate () throws TemplateModelException {
        return this.dateTime.toDate();
    }


    /**
     * {@inheritDoc}
     *
     * @see freemarker.template.TemplateDateModel#getDateType()
     */
    @Override
    public int getDateType () {
        return TemplateDateModel.UNKNOWN;
    }

}
