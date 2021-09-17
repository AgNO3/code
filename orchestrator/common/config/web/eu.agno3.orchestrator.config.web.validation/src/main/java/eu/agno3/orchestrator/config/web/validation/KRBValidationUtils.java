/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.web.validation;


import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.config.model.validation.ConfigTestResult;
import eu.agno3.orchestrator.config.web.LDAPConfigurationObjectTypeDescriptor;
import eu.agno3.runtime.net.krb5.KerberosClockSkewException;
import eu.agno3.runtime.net.krb5.KerberosCredentialsExpiredException;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.KerberosInvalidCredentialsException;
import eu.agno3.runtime.net.krb5.KerberosPrincipalNotFoundException;
import eu.agno3.runtime.net.krb5.KerberosUnsupportedETypesException;


/**
 * @author mbechler
 *
 */
public final class KRBValidationUtils {

    /**
     * Not really kerberos but binds to web package
     */
    private static final String USE_TYPE = LDAPConfigurationObjectTypeDescriptor.TYPE_NAME;


    /**
     * 
     */
    private KRBValidationUtils () {}


    /**
     * @param r
     * @param cause
     */
    public static void handleException ( ConfigTestResult r, KerberosException cause ) {
        ConfigTestResult br = r.withType(USE_TYPE);

        if ( cause instanceof KerberosInvalidCredentialsException ) {
            br.error(
                "FAIL_KRB_PRINC_CREDS_INVALID", //$NON-NLS-1$
                ( (KerberosInvalidCredentialsException) cause ).getPrincipal().toString(),
                cause.getMessage());
        }
        else if ( cause instanceof KerberosCredentialsExpiredException ) {
            br.error(
                "FAIL_KRB_PRINC_CREDS_EXPIRED", //$NON-NLS-1$
                ( (KerberosCredentialsExpiredException) cause ).getPrincipal().toString());
        }
        else if ( cause instanceof KerberosPrincipalNotFoundException ) {
            br.error(
                "FAIL_KRB_PRINC_UNKNOWN", //$NON-NLS-1$
                ( (KerberosPrincipalNotFoundException) cause ).getPrincipal().toString());
        }
        else if ( cause instanceof KerberosUnsupportedETypesException ) {
            br.error(
                "FAIL_KRB_ETYPE_UNSUPP", //$NON-NLS-1$
                Arrays.toString( ( (KerberosUnsupportedETypesException) cause ).getRequestedETypes()));
        }
        else if ( cause instanceof KerberosClockSkewException ) {
            br.error("FAIL_KRB_CLOCK_SKEW", cause.getMessage()); //$NON-NLS-1$
        }
        else if ( cause.getCause() instanceof IOException ) {
            SocketValidationUtils.handleIOException((IOException) cause.getCause(), br, StringUtils.EMPTY);
        }
        else {
            br.error("FAIL_KRB_INIT", cause.getMessage()); //$NON-NLS-1$
        }
    }
}
