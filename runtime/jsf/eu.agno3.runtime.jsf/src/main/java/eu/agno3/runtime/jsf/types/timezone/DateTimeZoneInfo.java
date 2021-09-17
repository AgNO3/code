/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.04.2014 by mbechler
 */
package eu.agno3.runtime.jsf.types.timezone;


import java.io.Serializable;

import org.joda.time.DateTimeZone;


/**
 * Structured localized timezone information
 * 
 * @author mbechler
 * 
 */
public class DateTimeZoneInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 966004735097093189L;
    private DateTimeZone tz;
    private String id;
    private String tzShortName;
    private String tzName;
    private String tzOff;
    private String displayId;


    /**
     * @param tz
     * @param id
     * @param tzShortName
     * @param tzName
     * @param tzOff
     */
    public DateTimeZoneInfo ( DateTimeZone tz, String id, String tzShortName, String tzName, String tzOff ) {
        this.tz = tz;
        this.id = id;
        this.displayId = id.replace('_', ' ');
        this.tzShortName = tzShortName;
        this.tzName = tzName;
        this.tzOff = tzOff;
    }


    /**
     * @return the tz
     */
    public DateTimeZone getTz () {
        return this.tz;
    }


    /**
     * @return the id
     */
    public String getId () {
        return this.id;
    }


    /**
     * @return the displayId
     */
    public String getDisplayId () {
        return this.displayId;
    }


    /**
     * @return the tzName
     */
    public String getTzName () {
        return this.tzName;
    }


    /**
     * @return the tzOff
     */
    public String getTzOff () {
        return this.tzOff;
    }


    /**
     * @return the tzShortName
     */
    public String getTzShortName () {
        return this.tzShortName;
    }
}