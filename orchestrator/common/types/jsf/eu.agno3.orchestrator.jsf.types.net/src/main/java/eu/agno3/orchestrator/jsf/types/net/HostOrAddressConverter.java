/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jsf.types.net;


import javax.faces.component.UIComponent;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ValidatorFactory;

import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.orchestrator.types.net.name.HostOrAddress;
import eu.agno3.orchestrator.types.net.validation.HostOrAddressValidator;
import eu.agno3.orchestrator.types.net.validation.ValidHostOrAddress;
import eu.agno3.orchestrator.types.net.validation.ValidNetworkAddress;
import eu.agno3.runtime.jsf.types.base.AbstractValidatingConverter;
import eu.agno3.runtime.validation.domain.FQDNStringValidator;
import eu.agno3.runtime.validation.util.ValueConstraintValidatorContext;


/**
 * @author mbechler
 * 
 */
@Named ( "hostOrAddressConverter" )
@FacesConverter ( forClass = HostOrAddress.class )
public class HostOrAddressConverter extends AbstractValidatingConverter<HostOrAddress, ValidHostOrAddress> implements Converter {

    private static final String ALLOW_IDN = "allowIdn"; //$NON-NLS-1$
    @Inject
    @OsgiService ( dynamic = true )
    private ValidatorFactory validatorFactory;


    /**
     * @param val
     * @return the converted object
     */
    public HostOrAddress convertFromString ( @ValidHostOrAddress ( addr = @ValidNetworkAddress ( parsableOnly = true ) ) String val ) {
        HostOrAddress addr = HostOrAddress.fromString(val);

        return addr;
    }


    @Override
    public String convertFromObject ( HostOrAddress obj ) {
        return obj.toString();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.types.base.AbstractValidatingConverter#getObjectClass()
     */
    @Override
    protected Class<HostOrAddress> getObjectClass () {
        return HostOrAddress.class;
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
    protected ValidHostOrAddress makeConstraint ( UIComponent c ) {
        boolean allowIdn = true;
        String allowIdnSpec = (String) c.getAttributes().get(ALLOW_IDN);
        if ( allowIdnSpec != null ) {
            allowIdn = Boolean.parseBoolean(allowIdnSpec);
        }

        return HostOrAddressValidator.makeConstraint(FQDNStringValidator.makeConstraint(allowIdn), NetworkAddressConverter.configureConstraint(c));
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.types.base.AbstractValidatingConverter#validateObjectValue(java.lang.Object,
     *      java.lang.Object, eu.agno3.orchestrator.validation.ValueConstraintValidatorContext)
     */
    @Override
    protected void validateObjectValue ( ValidHostOrAddress constraint, HostOrAddress res,
            ValueConstraintValidatorContext<HostOrAddress, ValidHostOrAddress> ctx ) {
        HostOrAddressValidator.checkHostOrAddress(res, ctx, constraint);
    }

}
