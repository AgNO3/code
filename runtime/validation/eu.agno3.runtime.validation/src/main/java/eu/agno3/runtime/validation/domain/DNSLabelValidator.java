/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.04.2014 by mbechler
 */
package eu.agno3.runtime.validation.domain;


import java.net.IDN;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
public class DNSLabelValidator implements ConstraintValidator<ValidDNSLabel, String> {

    private static final Logger log = Logger.getLogger(DNSLabelValidator.class);

    /**
     * 
     */
    private static final String HYPHEN = "-"; //$NON-NLS-1$
    private static final Pattern ALPHANUM_PATTERN = Pattern.compile("[a-z0-9\\-]*", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

    private boolean allowIdn;


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
     */
    @Override
    public void initialize ( ValidDNSLabel annot ) {
        this.allowIdn = annot.allowIdn();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
     */
    @Override
    public boolean isValid ( String val, ConstraintValidatorContext ctx ) {

        if ( val == null ) {
            return true;
        }

        ctx.disableDefaultConstraintViolation();
        return isValidDNSLabel(val, ctx, this.allowIdn);
    }


    /**
     * @param val
     * @param ctx
     * @param allowIdn
     * @return whether val is a valid dns label/component
     */
    public static boolean isValidDNSLabel ( String val, ConstraintValidatorContext ctx, boolean allowIdn ) {
        boolean valid = true;
        String decoded = val;
        boolean isAlphaNum = ALPHANUM_PATTERN.matcher(decoded).matches();

        if ( !isAlphaNum && allowIdn ) {
            try {
                decoded = IDN.toASCII(decoded);
            }
            catch ( IllegalArgumentException e ) {
                log.debug("Failed to encode IDN label:", e); //$NON-NLS-1$
                ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.domain:label.idn}").addConstraintViolation(); //$NON-NLS-1$
            }
        }

        if ( decoded.isEmpty() ) {
            ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.domain:label.empty}").addConstraintViolation(); //$NON-NLS-1$
            valid = false;
        }

        if ( decoded.length() > 63 ) {
            ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.domain:label.invalidLength}").addConstraintViolation(); //$NON-NLS-1$
            valid = false;
        }

        if ( !isAlphaNum ) {
            ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.domain:label.invalidChar}").addConstraintViolation(); //$NON-NLS-1$
            valid = false;
        }

        if ( decoded.startsWith(HYPHEN) || decoded.endsWith(HYPHEN) ) {
            ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.domain:label.invalidHyphen}").addConstraintViolation(); //$NON-NLS-1$
            valid = false;
        }

        return valid;
    }
}
