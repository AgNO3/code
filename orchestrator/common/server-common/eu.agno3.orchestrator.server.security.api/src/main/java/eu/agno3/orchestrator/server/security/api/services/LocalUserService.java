/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.security.api.services;


import java.util.Set;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElementWrapper;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.runtime.security.SecurityManagementException;
import eu.agno3.runtime.security.UserLicenseLimitExceededException;
import eu.agno3.runtime.security.password.PasswordChangePolicyException;
import eu.agno3.runtime.security.password.PasswordPolicyException;
import eu.agno3.runtime.security.principal.UserInfo;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 *
 */
@WebService ( targetNamespace = LocalUserServiceDescriptor.NAMESPACE )
public interface LocalUserService extends SOAPWebService {

    /**
     * @param service
     * @return the locally stored users
     * @throws SecurityManagementException
     */
    @XmlElementWrapper
    @WebResult ( name = "user" )
    Set<UserInfo> getUsers ( @WebParam ( name = "service" ) ServiceStructuralObject service) throws SecurityManagementException;


    /**
     * 
     * @param service
     * @param username
     * @param password
     * @param roles
     * @param disabled
     * @param forcePasswordChange
     * @return the created user principal
     * @throws SecurityManagementException
     * @throws PasswordPolicyException
     * @throws UserLicenseLimitExceededException
     */
    @WebResult ( name = "createdUser" )
    UserInfo addUser ( @WebParam ( name = "service" ) ServiceStructuralObject service, @WebParam ( name = "user" ) String username,
            @WebParam ( name = "password" ) String password, Set<String> roles, boolean disabled, boolean forcePasswordChange)
                    throws SecurityManagementException, PasswordPolicyException, UserLicenseLimitExceededException;


    /**
     * 
     * @param service
     * @param user
     * @param password
     * @throws SecurityManagementException
     * @throws PasswordPolicyException
     * @throws PasswordChangePolicyException
     */
    void changePassword ( @WebParam ( name = "service" ) ServiceStructuralObject service, @WebParam ( name = "user" ) UserPrincipal user,
            @WebParam ( name = "password" ) String password)
                    throws SecurityManagementException, PasswordPolicyException, PasswordChangePolicyException;


    /**
     * 
     * @param service
     * @param user
     * @param em
     * @throws SecurityManagementException
     */
    void removeUser ( @WebParam ( name = "service" ) ServiceStructuralObject service, @WebParam ( name = "user" ) UserPrincipal user)
            throws SecurityManagementException;


    /**
     * 
     * @param service
     * @param user
     * @throws SecurityManagementException
     */
    void disableUser ( @WebParam ( name = "service" ) ServiceStructuralObject service, @WebParam ( name = "user" ) UserPrincipal user)
            throws SecurityManagementException;


    /**
     * 
     * @param service
     * @param user
     * @throws SecurityManagementException
     */
    void enableUser ( @WebParam ( name = "service" ) ServiceStructuralObject service, @WebParam ( name = "user" ) UserPrincipal user)
            throws SecurityManagementException;
}
