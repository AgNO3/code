/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jsf.types.net;


import java.util.EnumSet;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ValidatorFactory;

import org.apache.commons.lang3.StringUtils;
import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.orchestrator.types.net.AbstractIPAddress;
import eu.agno3.orchestrator.types.net.NetworkAddress;
import eu.agno3.orchestrator.types.net.NetworkAddressType;
import eu.agno3.orchestrator.types.net.validation.NetworkAddressValidator;
import eu.agno3.orchestrator.types.net.validation.ValidNetworkAddress;
import eu.agno3.runtime.jsf.types.base.AbstractValidatingConverter;
import eu.agno3.runtime.validation.util.ValueConstraintValidatorContext;


/**
 * @author mbechler
 * 
 */
@Named ( "ipAddressConverter" )
@FacesConverter ( forClass = AbstractIPAddress.class )
public class NetworkAddressConverter extends AbstractValidatingConverter<NetworkAddress, ValidNetworkAddress> {

    private static final String ALLOW_V4 = "v4"; //$NON-NLS-1$
    private static final String ALLOW_V6 = "v6"; //$NON-NLS-1$
    private static final String ALLOW_TYPES = "allowTypes"; //$NON-NLS-1$

    @Inject
    @OsgiService ( dynamic = true )
    private ValidatorFactory validatorFactory;


    /**
     * 
     * @param val
     * @return the converted address
     */
    public NetworkAddress convertFromString ( @ValidNetworkAddress String val ) {
        return AbstractIPAddress.parse(val);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.types.base.AbstractValidatingConverter#convertFromObject(java.lang.Object)
     */
    @Override
    public String convertFromObject ( NetworkAddress obj ) {
        return obj.toString();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.types.base.AbstractValidatingConverter#getObjectClass()
     */
    @Override
    protected Class<NetworkAddress> getObjectClass () {
        return NetworkAddress.class;
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
    protected ValidNetworkAddress makeConstraint ( UIComponent c ) {
        return configureConstraint(c);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.types.base.AbstractValidatingConverter#validateObjectValue(java.lang.Object,
     *      java.lang.Object, eu.agno3.orchestrator.validation.ValueConstraintValidatorContext)
     */
    @Override
    protected void validateObjectValue ( ValidNetworkAddress constraint, NetworkAddress res,
            ValueConstraintValidatorContext<NetworkAddress, ValidNetworkAddress> ctx ) {
        NetworkAddressValidator.checkNetworkAddress(res, ctx, constraint);
    }


    /**
     * @param c
     * @return a constraint for the component
     */
    public static ValidNetworkAddress configureConstraint ( UIComponent c ) {

        boolean v4 = true;
        boolean v6 = true;
        Set<NetworkAddressType> allowedTypes = EnumSet.of(NetworkAddressType.LOOPBACK, NetworkAddressType.UNICAST, NetworkAddressType.ANYCAST);

        String v4spec = (String) c.getAttributes().get(ALLOW_V4);
        if ( v4spec != null ) {
            v4 = Boolean.parseBoolean(v4spec);
        }

        String v6spec = (String) c.getAttributes().get(ALLOW_V6);
        if ( v6spec != null ) {
            v6 = Boolean.parseBoolean(v6spec);
        }

        String allowTypesSpec = (String) c.getAttributes().get(ALLOW_TYPES);
        if ( allowTypesSpec != null ) {
            String[] typesSpec = StringUtils.split(allowTypesSpec, ',');
            allowedTypes.clear();

            for ( String typeSpec : typesSpec ) {
                allowedTypes.add(NetworkAddressType.valueOf(typeSpec.trim().toUpperCase()));
            }
        }

        return NetworkAddressValidator.makeConstraint(v4, v6, allowedTypes);
    }

}
