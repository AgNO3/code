/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.config;


import java.util.List;
import java.util.Set;

import org.apache.shiro.subject.Subject;

import eu.agno3.fileshare.exceptions.PolicyNotFoundException;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.runtime.security.password.PasswordType;


/**
 * @author mbechler
 *
 */
public interface SecurityPolicyConfiguration {

    /**
     * @return the defined security labels
     */
    List<String> getDefinedLabels ();


    /**
     * 
     * @return the default label
     */
    String getDefaultLabel ();


    /**
     * @param roles
     * @return the default label
     */
    String getDefaultUserLabelForRoles ( Set<String> roles );


    /**
     * @param s
     * @return the default label
     */
    String getDefaultUserLabelForSubject ( Subject s );


    /**
     * @return the label to use for root containers
     */
    String getRootContainerLabel ();


    /**
     * 
     * @param label
     * @return the policy
     * @throws PolicyNotFoundException
     */
    PolicyConfiguration getPolicy ( String label ) throws PolicyNotFoundException;


    /**
     * @param label
     * @return whether a policy exists
     */
    boolean hasPolicy ( String label );


    /**
     * @param a
     * @param b
     * @return lt 0 when a is lower than b, eq 0 when equal, gt 0 when a is higher than b
     */
    int compareLabels ( String a, String b );


    /**
     * @param a
     * @param b
     * @return lt 0 when a is lower than b, eq 0 when equal, gt 0 when a is higher than b
     */
    int compareLabels ( SecurityLabel a, SecurityLabel b );


    /**
     * @return the type of generated share passwords
     */
    PasswordType getSharePasswordType ();


    /**
     * @return the bit entropy of the generated share passwords
     */
    int getSharePasswordBits ();

}
