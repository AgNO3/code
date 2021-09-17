/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.04.2014 by mbechler
 */
package eu.agno3.runtime.validation.domain;


import java.net.IDN;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
public class DomainNameStringValidator implements ConstraintValidator<ValidDomainName, String> {

    private static final Logger log = Logger.getLogger(DomainNameStringValidator.class);

    private boolean allowIdn;


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
     */
    @Override
    public void initialize ( ValidDomainName annot ) {
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
        return checkDomainName(val, ctx, this.allowIdn);
    }


    /**
     * @param val
     * @param ctx
     * @param allowIdn
     * @return whether the given FQDN is valid
     */
    public static boolean checkDomainName ( String val, ConstraintValidatorContext ctx, boolean allowIdn ) {
        boolean valid = true;
        String decoded = val;

        if ( log.isDebugEnabled() ) {
            log.debug("Checking domain " + val); //$NON-NLS-1$
        }

        if ( allowIdn ) {
            try {
                decoded = IDN.toASCII(decoded);
            }
            catch ( IllegalArgumentException e ) {
                log.debug("Failed to encode IDN name:", e); //$NON-NLS-1$
                valid = false;
                if ( ctx != null ) {
                    ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.domain:domainname.idn}").addConstraintViolation(); //$NON-NLS-1$
                }
            }
        }

        if ( decoded.length() > 255 ) {
            if ( ctx != null ) {
                ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.domain:domainname.length}").addConstraintViolation(); //$NON-NLS-1$
            }
            valid = false;
        }

        String[] components = StringUtils.splitPreserveAllTokens(decoded, '.');
        List<String> realComponents = new ArrayList<>();

        if ( components.length >= 1 && StringUtils.isBlank(components[ components.length - 1 ]) ) {
            log.debug("Last label is empty, ignore"); //$NON-NLS-1$
            realComponents.addAll(Arrays.asList(Arrays.copyOf(components, components.length - 1)));
        }
        else {
            realComponents.addAll(Arrays.asList(components));
        }

        for ( String component : realComponents ) {
            if ( !DNSLabelValidator.isValidDNSLabel(component, ctx, allowIdn) ) {
                valid = false;
            }
        }

        if ( realComponents.size() > 0 ) {
            String lastLabel = realComponents.get(realComponents.size() - 1);
            if ( StringUtils.isNumeric(lastLabel) ) {
                if ( ctx != null ) {
                    ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.domain:domainname.lastLabelNumeric}") //$NON-NLS-1$
                            .addConstraintViolation();
                    valid = false;
                }
            }
        }

        return valid;
    }
}
