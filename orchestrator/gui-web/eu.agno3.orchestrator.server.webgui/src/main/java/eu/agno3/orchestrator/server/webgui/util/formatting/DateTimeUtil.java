/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.util.formatting;


import java.util.Locale;

import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;


/**
 * @author mbechler
 *
 */
@Named ( "dateTimeUtil" )
public class DateTimeUtil {

    /**
     * 
     * @param dt
     * @return the formatted date time
     */
    public String formatDateTime ( DateTime dt ) {
        if ( dt == null ) {
            return StringUtils.EMPTY;
        }
        return dt.toString(getDateTimeFormat());
    }


    /**
     * @return
     */
    private static String getDateTimeFormat () {
        Locale l = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        return DateTimeFormat.patternForStyle("MS", l); //$NON-NLS-1$
    }
}
