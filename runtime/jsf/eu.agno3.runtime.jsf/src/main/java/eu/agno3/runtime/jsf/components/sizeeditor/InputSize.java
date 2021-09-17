/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.11.2014 by mbechler
 */
package eu.agno3.runtime.jsf.components.sizeeditor;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.jsf.components.ResettableComponent;


/**
 * @author mbechler
 *
 */
public class InputSize extends UIInput implements NamingContainer, ResettableComponent {

    private static final Logger log = Logger.getLogger(InputSize.class);

    private static final String TERABYTE = "TB"; //$NON-NLS-1$
    private static final String GIGABYTE = "GB"; //$NON-NLS-1$
    private static final String MEGABYTE = "MB"; //$NON-NLS-1$
    private static final String KILOBYTE = "kB"; //$NON-NLS-1$
    private static final String BYTE = "b"; //$NON-NLS-1$

    private static final String DEFAULT_UNIT = "defaultUnit"; //$NON-NLS-1$
    private static final String MIN_UNIT = "minUnit"; //$NON-NLS-1$
    private static final String MAX_UNIT = "maxUnit"; //$NON-NLS-1$

    private static final String SI = "si"; //$NON-NLS-1$

    private static final String[] UNITS = new String[] {
        BYTE, KILOBYTE, MEGABYTE, GIGABYTE, TERABYTE
    };

    private static final Map<String, Integer> UNIT_TO_INDEX = new HashMap<>();
    private static final Serializable SIZE_UNIT = "unit"; //$NON-NLS-1$
    private static final Serializable SIZE_VALUE = "baseValue"; //$NON-NLS-1$

    private static final double LOG2 = Math.log(2.0);

    static {
        UNIT_TO_INDEX.put(BYTE, 0);
        UNIT_TO_INDEX.put(KILOBYTE, 1);
        UNIT_TO_INDEX.put(MEGABYTE, 2);
        UNIT_TO_INDEX.put(GIGABYTE, 3);
        UNIT_TO_INDEX.put(TERABYTE, 4);
    }


    private boolean isSI () {
        return (boolean) this.getAttributes().getOrDefault(SI, true);
    }


    private double getBase () {
        if ( isSI() ) {
            return 1000.0;
        }
        return 1024.0;
    }


    private int getExponent ( long totalVal ) {
        if ( isSI() ) {
            return (int) Math.log10(totalVal) / 3;
        }

        return (int) ( Math.log(totalVal) / LOG2 ) / 10;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.ResettableComponent#resetComponent()
     */
    @Override
    public boolean resetComponent () {
        this.getStateHelper().remove(SIZE_UNIT);
        this.getStateHelper().remove(SIZE_VALUE);
        return true;
    }


    /**
     * 
     * @return size unit (base 1000 exponent)
     */
    public int getSizeUnit () {
        Integer unit = (Integer) this.getStateHelper().get(SIZE_UNIT);

        if ( unit == null ) {
            Long val = (Long) getValue();
            if ( val != null && val != 0 ) {
                int exp = getExponentFor(val);
                this.getStateHelper().put(SIZE_UNIT, exp);
                return exp;
            }

            int def = getDefaultUnit();
            this.getStateHelper().put(SIZE_UNIT, def);
            return def;
        }

        return unit;
    }


    /**
     * 
     * @param unit
     */
    public void setSizeUnit ( int unit ) {
        this.getStateHelper().put(SIZE_UNIT, unit);
    }


    /**
     * 
     * @return the size value in base
     */
    public long getSizeValue () {
        Long val = (Long) this.getStateHelper().get(SIZE_VALUE);

        if ( val == null ) {
            Long totalVal = (Long) getValue();
            if ( log.isDebugEnabled() ) {
                log.debug("Value is " + totalVal); //$NON-NLS-1$
            }
            if ( totalVal != null ) {
                int exp = getExponentFor(totalVal);
                long base = (long) Math.pow(getBase(), exp);
                long inBase = base != 0 ? totalVal / base : totalVal;
                this.getStateHelper().put(SIZE_VALUE, inBase);
                return inBase;
            }

            this.getStateHelper().put(SIZE_VALUE, 0L);
            return 0L;
        }

        return val;
    }


    /**
     * 
     * @param val
     */
    public void setSizeValue ( long val ) {
        this.getStateHelper().put(SIZE_VALUE, val);
    }


    /**
     * @param totalVal
     * @return
     */
    private int getExponentFor ( long totalVal ) {
        int exp = getExponent(totalVal);
        long base = (long) Math.pow(getBase(), exp);

        if ( base == 0 ) {
            return 0;
        }

        long rem = totalVal % base;

        log.debug("Remainder is " + rem); //$NON-NLS-1$

        if ( rem == 0 ) {
            return exp;
        }

        return exp - 1;
    }


    /**
     * 
     * @return the selectable units
     */
    public int[] getSelectableUnits () {
        int min = getMinUnit();
        int max = getMaxUnit();
        int units[] = new int[max - min + 1];
        int j = 0;
        for ( int i = min; i <= max; i++ ) {
            units[ j++ ] = i;
        }
        return units;
    }


    /**
     * @param i
     * @return a unit name for the index
     */
    public String getUnitName ( int i ) {
        return UNITS[ i ];
    }


    /**
     * 
     * @return the default unit index
     */
    private int getDefaultUnit () {
        String defaultUnit = (String) this.getAttributes().get(DEFAULT_UNIT);
        if ( StringUtils.isBlank(defaultUnit) || !UNIT_TO_INDEX.containsKey(defaultUnit) ) {
            return 0;
        }

        return UNIT_TO_INDEX.get(defaultUnit);
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
        long actualValue = (long) ( getSizeValue() * Math.pow(getBase(), getSizeUnit()) );
        if ( log.isDebugEnabled() ) {
            log.debug("Actual value " + actualValue); //$NON-NLS-1$
        }
        setValue(actualValue);
        super.updateModel(context);
    }

}
