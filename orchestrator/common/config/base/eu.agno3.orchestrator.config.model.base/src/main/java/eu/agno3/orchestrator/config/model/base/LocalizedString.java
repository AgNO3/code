/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.base;


import java.util.Locale;

import javax.persistence.Embeddable;
import javax.persistence.PersistenceUnit;


/**
 * @author mbechler
 *
 */
@Embeddable
@PersistenceUnit ( unitName = "config" )
public class LocalizedString {

    private Locale locale;

    private String value;


    /**
     * @return the locale
     */
    public Locale getLocale () {
        return this.locale;
    }


    /**
     * @param locale
     *            the locale to set
     */
    public void setLocale ( Locale locale ) {
        this.locale = locale;
    }


    /**
     * @return the value
     */
    public String getValue () {
        return this.value;
    }


    /**
     * @param value
     *            the value to set
     */
    public void setValue ( String value ) {
        this.value = value;
    }
}
