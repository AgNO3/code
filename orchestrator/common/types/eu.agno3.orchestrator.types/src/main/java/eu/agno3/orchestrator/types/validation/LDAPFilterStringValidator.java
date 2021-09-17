/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.01.2016 by mbechler
 */
package eu.agno3.orchestrator.types.validation;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.ldap.filter.FilterParserException;
import eu.agno3.runtime.ldap.filter.parser.impl.ParserFactoryImpl;


/**
 * @author mbechler
 *
 */
public class LDAPFilterStringValidator implements ConstraintValidator<ValidLDAPFilter, String> {

    private static final Logger log = Logger.getLogger(LDAPFilterStringValidator.class);


    /**
     * {@inheritDoc}
     *
     * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
     */
    @Override
    public void initialize ( ValidLDAPFilter spec ) {}


    /**
     * {@inheritDoc}
     *
     * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
     */
    @Override
    public boolean isValid ( String val, ConstraintValidatorContext ctx ) {
        ctx.disableDefaultConstraintViolation();
        try {
            if ( StringUtils.isBlank(val) ) {
                return true;
            }

            ( new ParserFactoryImpl() ).parseString(val);
            return true;
        }
        catch (
            IllegalArgumentException |
            FilterParserException e ) {
            log.debug("Invalid LDAP filter", e); //$NON-NLS-1$
            ctx.buildConstraintViolationWithTemplate("{ldap.filter.invalid}").addConstraintViolation(); //$NON-NLS-1$
            return false;
        }
    }

}
