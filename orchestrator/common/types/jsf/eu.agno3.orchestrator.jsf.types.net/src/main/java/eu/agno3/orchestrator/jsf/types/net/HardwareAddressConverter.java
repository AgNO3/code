/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jsf.types.net;


import javax.faces.component.UIComponent;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ValidatorFactory;

import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.orchestrator.types.net.HardwareAddress;
import eu.agno3.orchestrator.types.net.MACAddress;
import eu.agno3.orchestrator.types.net.validation.HardwareAddressValidator;
import eu.agno3.orchestrator.types.net.validation.ValidHardwareAddress;
import eu.agno3.runtime.jsf.types.base.AbstractValidatingConverter;
import eu.agno3.runtime.validation.util.ValueConstraintValidatorContext;


/**
 * @author mbechler
 * 
 */
@Named ( "hwAddressConverter" )
@FacesConverter ( forClass = HardwareAddress.class )
public class HardwareAddressConverter extends AbstractValidatingConverter<HardwareAddress, ValidHardwareAddress> {

    @Inject
    @OsgiService ( dynamic = true )
    private ValidatorFactory validatorFactory;


    /**
     * 
     * @param val
     * @return the converted address
     */
    public HardwareAddress convertFromString ( @ValidHardwareAddress String val ) {
        MACAddress mac = new MACAddress();
        mac.fromString(val);
        return mac;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.types.base.AbstractValidatingConverter#convertFromObject(java.lang.Object)
     */
    @Override
    public String convertFromObject ( HardwareAddress obj ) {
        return obj.toString();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.types.base.AbstractValidatingConverter#getObjectClass()
     */
    @Override
    protected Class<HardwareAddress> getObjectClass () {
        return HardwareAddress.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.types.base.AbstractValidatingConverter#getValidatorFactory()
     */
    @Override
    protected ValidatorFactory getValidatorFactory () {
        return this.validatorFactory;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.types.base.AbstractValidatingConverter#makeConstraint(javax.faces.component.UIComponent)
     */
    @Override
    protected ValidHardwareAddress makeConstraint ( UIComponent c ) {
        return configureConstraint(c);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.types.base.AbstractValidatingConverter#validateObjectValue(java.lang.Object,
     *      java.lang.Object, eu.agno3.orchestrator.validation.ValueConstraintValidatorContext)
     */
    @Override
    protected void validateObjectValue ( ValidHardwareAddress constraint, HardwareAddress res,
            ValueConstraintValidatorContext<HardwareAddress, ValidHardwareAddress> ctx ) {
        HardwareAddressValidator.checkHardwareAddress(res, ctx, constraint);
    }


    /**
     * @param c
     * @return a constraint for the component
     */
    public static ValidHardwareAddress configureConstraint ( UIComponent c ) {
        return HardwareAddressValidator.makeConstraint();
    }

}
