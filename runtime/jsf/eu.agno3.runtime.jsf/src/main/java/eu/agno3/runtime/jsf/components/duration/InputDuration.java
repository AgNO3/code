/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.11.2014 by mbechler
 */
package eu.agno3.runtime.jsf.components.duration;


import java.util.HashMap;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Duration;

import eu.agno3.runtime.jsf.components.ResettableComponent;
import eu.agno3.runtime.jsf.i18n.BaseMessages;
import eu.agno3.runtime.jsf.util.date.DateFormatter;


/**
 * @author mbechler
 *
 */
public class InputDuration extends UIInput implements NamingContainer, ResettableComponent {

    private static final String YEARS = "years"; //$NON-NLS-1$
    private static final String DAYS = "days"; //$NON-NLS-1$
    private static final String HOURS = "hours"; //$NON-NLS-1$
    private static final String MINUTES = "minutes"; //$NON-NLS-1$
    private static final String SECONDS = "seconds"; //$NON-NLS-1$
    private static final String MILLIS = "millis"; //$NON-NLS-1$

    private static final String MIN_UNIT = "minUnit"; //$NON-NLS-1$
    private static final String MAX_UNIT = "maxUnit"; //$NON-NLS-1$

    private static final String MIN = "min"; //$NON-NLS-1$
    private static final String MAX = "max"; //$NON-NLS-1$

    private static final String[] UNITS = new String[] {
        MILLIS, SECONDS, MINUTES, HOURS, DAYS, YEARS
    };

    private static final Map<String, Integer> UNIT_TO_INDEX = new HashMap<>();


    static {
        UNIT_TO_INDEX.put(MILLIS, 0);
        UNIT_TO_INDEX.put(SECONDS, 1);
        UNIT_TO_INDEX.put(MINUTES, 2);
        UNIT_TO_INDEX.put(HOURS, 3);
        UNIT_TO_INDEX.put(DAYS, 4);
        UNIT_TO_INDEX.put(YEARS, 5);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.ResettableComponent#resetComponent()
     */
    @Override
    public boolean resetComponent () {
        getStateHelper().remove(YEARS);
        getStateHelper().remove(DAYS);
        getStateHelper().remove(HOURS);
        getStateHelper().remove(MINUTES);
        getStateHelper().remove(SECONDS);
        getStateHelper().remove(MILLIS);
        return true;
    }


    /**
     * 
     * @return minimum selectable unit
     */
    private int getMinUnit () {
        String minUnit = (String) this.getAttributes().get(MIN_UNIT);

        if ( StringUtils.isBlank(minUnit) || !UNIT_TO_INDEX.containsKey(minUnit) ) {
            return 0;
        }

        return UNIT_TO_INDEX.get(minUnit);
    }


    /**
     * 
     * @return maximum selectable unit
     */
    private int getMaxUnit () {
        String maxUnit = (String) this.getAttributes().get(MAX_UNIT);

        if ( StringUtils.isEmpty(maxUnit) || !UNIT_TO_INDEX.containsKey(maxUnit) ) {
            return UNITS.length - 1;
        }

        return UNIT_TO_INDEX.get(maxUnit);
    }


    private Duration getMin () {
        String min = (String) this.getAttributes().get(MIN);
        if ( !StringUtils.isBlank(min) ) {
            return DateFormatter.parseDuration(min);
        }
        return new Duration(0);
    }


    private Duration getMax () {
        String max = (String) this.getAttributes().get(MAX);
        if ( !StringUtils.isBlank(max) ) {
            return DateFormatter.parseDuration(max);
        }
        return new Duration(Long.MAX_VALUE);
    }


    /**
     * @param unit
     * @return should display field
     */
    public boolean shouldDisplayField ( String unit ) {

        if ( !UNIT_TO_INDEX.containsKey(unit) ) {
            return false;
        }

        int idx = UNIT_TO_INDEX.get(unit);

        if ( idx < getMinUnit() || idx > getMaxUnit() ) {
            return false;
        }

        return true;
    }


    private int getYearsFromValue () {
        Duration d = (Duration) this.getValue();
        if ( d == null ) {
            return 0;
        }
        return (int) d.getStandardDays() / 365;
    }


    private int getDaysFromValue () {
        Duration d = (Duration) this.getValue();
        if ( d == null ) {
            return 0;
        }
        return (int) d.getStandardDays() % 365;
    }


    private int getHoursFromValue () {
        Duration d = (Duration) this.getValue();
        if ( d == null ) {
            return 0;
        }

        if ( shouldDisplayField(DAYS) ) {
            d = d.minus(Duration.standardDays(d.getStandardDays()));
        }
        return (int) d.getStandardHours();
    }


    private int getMinutesFromValue () {
        Duration d = (Duration) this.getValue();
        if ( d == null ) {
            return 0;
        }
        if ( shouldDisplayField(HOURS) ) {
            d = d.minus(Duration.standardHours(d.getStandardHours()));
        }
        return (int) d.getStandardMinutes();
    }


    private int getSecondsFromValue () {
        Duration d = (Duration) this.getValue();
        if ( d == null ) {
            return 0;
        }
        if ( shouldDisplayField(MINUTES) ) {
            d = d.minus(Duration.standardMinutes(d.getStandardMinutes()));
        }
        return (int) d.getStandardSeconds();
    }


    private int getMillisFromValue () {
        Duration d = (Duration) this.getValue();
        if ( d == null ) {
            return 0;
        }
        if ( shouldDisplayField(SECONDS) ) {
            d = d.minus(Duration.standardSeconds(d.getStandardSeconds()));
        }
        return (int) d.getMillis();
    }


    /**
     * 
     * @return years
     */
    public String getYears () {
        return (String) this.getStateHelper().eval(YEARS, String.valueOf(getYearsFromValue()));
    }


    /**
     * 
     * @param years
     */
    public void setYears ( String years ) {
        this.getStateHelper().put(YEARS, years);
    }


    /**
     * @return days
     */
    public String getDays () {
        return (String) this.getStateHelper().eval(DAYS, String.valueOf(getDaysFromValue()));
    }


    /**
     * @param days
     */
    public void setDays ( String days ) {
        this.getStateHelper().put(DAYS, days);
    }


    /**
     * @return hours
     */
    public String getHours () {
        return (String) this.getStateHelper().eval(HOURS, String.valueOf(getHoursFromValue()));
    }


    /**
     * @param hours
     */
    public void setHours ( String hours ) {
        this.getStateHelper().put(HOURS, hours);
    }


    /**
     * @return minutes
     */
    public String getMinutes () {
        return (String) this.getStateHelper().eval(MINUTES, String.valueOf(getMinutesFromValue()));
    }


    /**
     * @param minutes
     */
    public void setMinutes ( String minutes ) {
        this.getStateHelper().put(MINUTES, minutes);
    }


    /**
     * @return seconds
     */
    public String getSeconds () {
        return (String) this.getStateHelper().eval(SECONDS, String.valueOf(getSecondsFromValue()));
    }


    /**
     * @param secs
     */
    public void setSeconds ( String secs ) {
        this.getStateHelper().put(SECONDS, secs);
    }


    /**
     * @return millis
     */
    public String getMillis () {
        return (String) this.getStateHelper().eval(MILLIS, String.valueOf(getMillisFromValue()));
    }


    /**
     * @param millis
     */
    public void setMillis ( String millis ) {
        this.getStateHelper().put(MILLIS, Integer.valueOf(millis));
    }


    /**
     * @return current state as duration
     */
    public Duration toDuration () {

        Duration d = new Duration(0);
        if ( shouldDisplayField(MILLIS) ) {
            d = d.plus(Integer.parseInt(getMillis()));
        }
        if ( shouldDisplayField(SECONDS) ) {
            d = d.plus(Duration.standardSeconds(Integer.parseInt(getSeconds())));
        }
        if ( shouldDisplayField(MINUTES) ) {
            d = d.plus(Duration.standardMinutes(Integer.parseInt(getMinutes())));
        }
        if ( shouldDisplayField(HOURS) ) {
            d = d.plus(Duration.standardHours(Integer.parseInt(getHours())));
        }
        if ( shouldDisplayField(DAYS) ) {
            d = d.plus(Duration.standardDays(Integer.parseInt(getDays())));
        }
        if ( shouldDisplayField(YEARS) ) {
            d = d.plus(Duration.standardDays(Integer.parseInt(getYears()) * 365));
        }
        return d;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#getFamily()
     */
    @Override
    public String getFamily () {
        return UINamingContainer.COMPONENT_FAMILY;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#processValidators(javax.faces.context.FacesContext)
     */
    @Override
    public void processValidators ( FacesContext context ) {
        this.pushComponentToEL(context, this);
        try {
            super.processValidators(context);
        }
        finally {
            this.popComponentFromEL(context);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#processUpdates(javax.faces.context.FacesContext)
     */
    @Override
    public void processUpdates ( FacesContext context ) {
        this.pushComponentToEL(context, this);
        try {
            super.processUpdates(context);
        }
        finally {
            this.popComponentFromEL(context);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#updateModel(javax.faces.context.FacesContext)
     */
    @Override
    public void updateModel ( FacesContext context ) {
        Duration duration = toDuration();
        if ( duration.isLongerThan(this.getMax()) ) {
            context.addMessage(
                this.getClientId(context),
                new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    BaseMessages.format("duration.durationTooLongFmt", this.getAttributes().get(MAX)), //$NON-NLS-1$
                    StringUtils.EMPTY));
            setValid(false);
        }
        else if ( duration.isShorterThan(this.getMin()) ) {
            context.addMessage(
                this.getClientId(context),
                new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    BaseMessages.format("duration.durationTooShortFmt", this.getAttributes().get(MIN)), //$NON-NLS-1$
                    StringUtils.EMPTY));
            setValid(false);
        }
        else {
            setValue(duration);
        }

        super.updateModel(context);
    }

}
