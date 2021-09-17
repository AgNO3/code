/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jsf.types.net;


import javax.faces.component.UIComponent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ValidatorFactory;

import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.orchestrator.types.net.NetworkSpecification;
import eu.agno3.orchestrator.types.net.validation.NetworkSpecificationValidator;
import eu.agno3.orchestrator.types.net.validation.ValidNetworkSpecification;
import eu.agno3.runtime.jsf.types.base.AbstractValidatingConverter;
import eu.agno3.runtime.validation.util.ValueConstraintValidatorContext;


/**
 * @author mbechler
 * 
 */
@Named ( "networkSpecificationOrAddressConverter" )
public class NetworkSpecificationOrAddressConverter extends AbstractValidatingConverter<NetworkSpecification, ValidNetworkSpecification> {

    @Inject
    @OsgiService ( dynamic = true )
    private ValidatorFactory validatorFactory;


    /**
     * 
     * @param val
     * @return the converted address
     */
    public NetworkSpecification convertFromString ( @ValidNetworkSpecification ( allowNoPrefix = true ) String val ) {
        return NetworkSpecification.fromString(val, true);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.types.base.AbstractValidatingConverter#convertFromObject(java.lang.Object)
     */
    @Override
    public String convertFromObject ( NetworkSpecification obj ) {
        return obj.toString();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.types.base.AbstractValidatingConverter#getObjectClass()
     */
    @Override
    protected Class<NetworkSpecification> getObjectClass () {
        return NetworkSpecification.class;
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
    protected ValidNetworkSpecification makeConstraint ( UIComponent c ) {
        return NetworkSpecificationConverter.configureConstraint(c);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.types.base.AbstractValidatingConverter#validateObjectValue(java.lang.Object,
     *      java.lang.Object, eu.agno3.orchestrator.validation.ValueConstraintValidatorContext)
     */
    @Override
    protected void validateObjectValue ( ValidNetworkSpecification constraint, NetworkSpecification res,
            ValueConstraintValidatorContext<NetworkSpecification, ValidNetworkSpecification> ctx ) {
        NetworkSpecificationValidator.checkNetworkSpecification(res, ctx, constraint);
    }
}
