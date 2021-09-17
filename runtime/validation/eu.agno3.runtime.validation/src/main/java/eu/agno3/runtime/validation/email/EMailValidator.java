/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.03.2015 by mbechler
 */
package eu.agno3.runtime.validation.email;


import java.lang.annotation.Annotation;
import java.net.IDN;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.validation.domain.FQDNStringValidator;
import eu.agno3.runtime.validation.domain.ValidFQDN;


/**
 * @author mbechler
 *
 */
public class EMailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final String A_REC = "A"; //$NON-NLS-1$
    private static final String MX_REC = "MX"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(EMailValidator.class);

    private ValidEmail annot;


    /**
     * {@inheritDoc}
     *
     * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
     */
    @Override
    public void initialize ( ValidEmail a ) {
        this.annot = a;
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
        return checkEmailValid(val, ctx, this.annot);
    }


    /**
     * @param val
     * @param allowIdn
     * @param hostDNSValidate
     * @param noIPAddresses
     * @param allowEscapes
     * @return whether the address is valid
     */
    public static boolean checkEMailValid ( String val, boolean allowIdn, boolean hostDNSValidate, boolean noIPAddresses, boolean allowEscapes ) {
        return checkEmailValid(val, null, new ValidEMailImplementation(allowIdn, hostDNSValidate, noIPAddresses, allowEscapes));
    }


    /**
     * @param val
     * @param ctx
     * @param cfg
     * @return whether the address is valid
     */
    static boolean checkEmailValid ( String val, ConstraintValidatorContext ctx, ValidEmail cfg ) {
        if ( val.length() > cfg.maxLength() ) {
            if ( ctx != null ) {
                ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.email:email.invalid.maxLength}").addConstraintViolation(); //$NON-NLS-1$
            }
            return false;
        }

        int localPartLength = checkLocalPart(val, ctx, cfg);
        if ( localPartLength < 0 ) {
            return false;
        }

        int domainOffset = localPartLength + 1;
        if ( domainOffset < val.length() && val.charAt(domainOffset) == '[' && val.charAt(val.length() - 1) == ']' ) {
            return checkAddress(val, ctx, cfg, localPartLength);
        }

        return checkDomain(val, ctx, cfg, localPartLength);
    }


    /**
     * @param val
     * @param ctx
     * @param cfg
     * @return
     */
    protected static int checkLocalPart ( String val, ConstraintValidatorContext ctx, ValidEmail cfg ) {
        int localPartLength = validateLocalPart(val, ctx, cfg.allowEscapes());
        if ( localPartLength < 0 ) {
            return -1;
        }

        if ( localPartLength == val.length() ) {
            // no domain part/@ found
            if ( ctx != null ) {
                ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.email:email.invalid.noDomain}").addConstraintViolation(); //$NON-NLS-1$
            }
            return -1;
        }

        if ( localPartLength >= 64 ) {
            // local part longer than 63 characters
            if ( ctx != null ) {
                ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.email:email.invalid.localPartLength}") //$NON-NLS-1$
                        .addConstraintViolation();
            }
            return -1;
        }
        return localPartLength;
    }


    /**
     * @param val
     * @param ctx
     * @param cfg
     * @param localPartLength
     * @return
     */
    protected static boolean checkDomain ( String val, ConstraintValidatorContext ctx, ValidEmail cfg, int localPartLength ) {
        String domain = val.substring(localPartLength + 1);

        if ( StringUtils.isBlank(domain) ) {
            if ( ctx != null ) {
                ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.email:email.invalid.noDomain}") //$NON-NLS-1$
                        .addConstraintViolation();
            }
            return false;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Check domain part " + domain); //$NON-NLS-1$
        }

        if ( !FQDNStringValidator.checkFQDN(domain, ctx, cfg.allowIDN()) ) {
            return false;
        }

        if ( cfg.hostDNSValidate() ) {
            if ( !validMailTarget(domain, ctx, cfg.allowIDN()) ) {
                return false;
            }
        }

        return true;
    }


    /**
     * @param val
     * @param ctx
     * @param cfg
     * @param localPartLength
     */
    protected static boolean checkAddress ( String val, ConstraintValidatorContext ctx, ValidEmail cfg, int localPartLength ) {
        if ( cfg.noIPAddresses() ) {
            if ( ctx != null ) {
                ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.email:email.invalid.ipAddressDisallowed}") //$NON-NLS-1$
                        .addConstraintViolation();
            }
            return false;
        }

        String realAddr = val.substring(localPartLength + 1, val.length() - 1);
        if ( log.isDebugEnabled() ) {
            log.debug("IP address is " + realAddr); //$NON-NLS-1$
        }

        return true;
    }


    /**
     * @param val
     * @param ctx
     * @return
     */
    private static int validateLocalPart ( String val, ConstraintValidatorContext ctx, boolean allowEscapes ) {
        // As per RFC3696
        boolean quoted = false;
        boolean quotedOne = false;
        int pos = 0;
        for ( ; pos < val.length(); pos++ ) {
            char c = val.charAt(pos);

            if ( c == '\\' ) {
                if ( !allowEscapes ) {
                    if ( ctx != null ) {
                        ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.email:email.invalid.char.quoted}") //$NON-NLS-1$
                                .addConstraintViolation();
                    }
                    return -1;
                }
                quotedOne = true;
                continue;
            }
            else if ( c == '"' && !quoted ) {
                if ( !allowEscapes ) {
                    if ( ctx != null ) {
                        ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.email:email.invalid.char.quoted}") //$NON-NLS-1$
                                .addConstraintViolation();
                    }
                    return -1;
                }
                quoted = true;
            }
            else if ( c == '"' && quoted ) {
                quoted = false;
            }

            if ( quotedOne || quoted ) {
                if ( c == 0 || c == '\n' || c == '\r' ) {
                    // not text as per RFC2822
                    if ( ctx != null ) {
                        ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.email:email.invalid.char.control}") //$NON-NLS-1$
                                .addConstraintViolation();
                    }
                    return -1;
                }
                // any other char may appear quoted
                quotedOne = false;
                continue;
            }

            if ( c == '.' && pos > 0 && val.charAt(pos - 1) == '.' ) {
                // consecutive dots are not allowed
                if ( ctx != null ) {
                    ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.email:email.invalid.twodots}").addConstraintViolation(); //$NON-NLS-1$
                }
                return -1;
            }

            if ( isInvalidChar(c) ) {
                // these characters are invalid
                if ( c < 32 ) {
                    if ( ctx != null ) {
                        ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.email:email.invalid.char.control}") //$NON-NLS-1$
                                .addConstraintViolation();
                    }
                }
                else {
                    if ( ctx != null ) {
                        ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.email:email.invalid.char}").addConstraintViolation(); //$NON-NLS-1$
                    }
                }
                return -1;
            }

            if ( c == '@' ) {
                // end of local part
                break;
            }
        }
        return pos;
    }


    /**
     * @param c
     * @return
     */
    protected static boolean isInvalidChar ( char c ) {
        return c == '[' || c == ']' || c == ',' || ! ( c >= 33 && c <= 127 );
    }


    private static boolean validMailTarget ( String domainName, ConstraintValidatorContext ctx, boolean idnEncode ) {
        Map<String, String> env = makeDNSEnvironment();
        try {
            String realDomain = getTargetDomain(domainName, ctx, idnEncode);

            if ( realDomain == null ) {
                return false;
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Resolving domain " + realDomain); //$NON-NLS-1$
            }
            return doDNSLookup(ctx, env, realDomain);
        }
        catch ( NameNotFoundException e ) {
            log.debug("DNS name not found", e); //$NON-NLS-1$
            ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.email:email.invalid.dnsDomainNotFound}").addConstraintViolation(); //$NON-NLS-1$
            return false;
        }
        catch ( NamingException e ) {
            ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.email:email.invalid.dnsOtherError}").addConstraintViolation(); //$NON-NLS-1$
            log.debug("DNS query failed", e); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * @param domainName
     * @param ctx
     * @param idnEncode
     * @return
     */
    protected static String getTargetDomain ( String domainName, ConstraintValidatorContext ctx, boolean idnEncode ) {
        String realDomain = domainName;
        if ( idnEncode ) {
            try {
                realDomain = IDN.toASCII(realDomain);
            }
            catch ( IllegalArgumentException e ) {
                ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.email:email.invalid.dnsOtherError}") //$NON-NLS-1$
                        .addConstraintViolation();
                log.debug("IDN encoding failed", e); //$NON-NLS-1$
                return null;
            }
        }
        return realDomain;
    }


    /**
     * @param ctx
     * @param env
     * @param realDomain
     * @return
     * @throws NamingException
     */
    protected static boolean doDNSLookup ( ConstraintValidatorContext ctx, Map<String, String> env, String realDomain ) throws NamingException {
        DirContext ictx = new InitialDirContext(new Hashtable<>(env));
        Attributes attrs = ictx.getAttributes(realDomain, new String[] {
            MX_REC
        });

        if ( attrs.get(MX_REC) == null ) {
            attrs = ictx.getAttributes(realDomain, new String[] {
                A_REC
            });

            if ( attrs.get(A_REC) == null ) {
                ctx.buildConstraintViolationWithTemplate("{eu.agno3.runtime.validation.email:email.invalid.dnsDomainNoMXorA}") //$NON-NLS-1$
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }


    /**
     * @return
     */
    protected static Map<String, String> makeDNSEnvironment () {
        Map<String, String> env = new HashMap<>();
        env.put(
            "java.naming.factory.initial", //$NON-NLS-1$
            "com.sun.jndi.dns.DnsContextFactory"); //$NON-NLS-1$
        env.put(
            "com.sun.jndi.dns.timeout.initial", //$NON-NLS-1$
            "5000"); //$NON-NLS-1$
        env.put(
            "com.sun.jndi.dns.timeout.retries", //$NON-NLS-1$
            "2"); //$NON-NLS-1$
        return env;
    }

    /**
     * @author mbechler
     * 
     */
    @SuppressWarnings ( "all" )
    private static final class ValidEMailImplementation implements ValidEmail {

        /**
         * 
         */
        private final boolean allowIdn;
        private final int maxLength;
        private final boolean hostDNSValidate;
        private final boolean noIPAddresses;
        private final boolean allowEscapes;


        public ValidEMailImplementation ( boolean allowIdn, boolean hostDNSValidate, boolean noIPAddresses, boolean allowEscapes ) {
            super();
            this.allowIdn = allowIdn;
            this.maxLength = 320;
            this.hostDNSValidate = hostDNSValidate;
            this.noIPAddresses = noIPAddresses;
            this.allowEscapes = allowEscapes;
        }


        @Override
        public Class<? extends Annotation> annotationType () {
            return ValidFQDN.class;
        }


        @SuppressWarnings ( "unchecked" )
        @Override
        public Class<? extends Payload>[] payload () {
            return new Class[] {};
        }


        @Override
        public String message () {
            return null;
        }


        @Override
        public Class<?>[] groups () {
            return new Class[] {};
        }


        /**
         * {@inheritDoc}
         *
         * @see eu.agno3.runtime.validation.email.ValidEmail#allowEscapes()
         */
        @Override
        public boolean allowEscapes () {
            return this.allowEscapes;
        }


        /**
         * {@inheritDoc}
         *
         * @see eu.agno3.runtime.validation.email.ValidEmail#allowIDN()
         */
        @Override
        public boolean allowIDN () {
            return this.allowIdn;
        }


        /**
         * {@inheritDoc}
         *
         * @see eu.agno3.runtime.validation.email.ValidEmail#noIPAddresses()
         */
        @Override
        public boolean noIPAddresses () {
            return this.noIPAddresses;
        }


        /**
         * {@inheritDoc}
         *
         * @see eu.agno3.runtime.validation.email.ValidEmail#hostDNSValidate()
         */
        @Override
        public boolean hostDNSValidate () {
            return this.hostDNSValidate;
        }


        /**
         * {@inheritDoc}
         *
         * @see eu.agno3.runtime.validation.email.ValidEmail#maxLength()
         */
        @Override
        public int maxLength () {
            return this.maxLength;
        }
    }
}
