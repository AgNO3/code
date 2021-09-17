/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.runtime.security.db;


import java.util.Set;

import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.security.SecurityManagementException;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public interface BaseRoleMappingService {

    /**
     * @param tx
     * @param user
     * @return the mapped roles
     * @throws SecurityManagementException
     */
    Set<String> getMappedRoles ( EntityTransactionContext tx, UserPrincipal user ) throws SecurityManagementException;


    /**
     * @param tx
     * @param user
     * @param roles
     * @throws SecurityManagementException
     */
    void setMappedRoles ( EntityTransactionContext tx, UserPrincipal user, Set<String> roles ) throws SecurityManagementException;


    /**
     * @param tx
     * @param user
     * @param roles
     * @throws SecurityManagementException
     */
    void addMappedRoles ( EntityTransactionContext tx, UserPrincipal user, Set<String> roles ) throws SecurityManagementException;


    /**
     * @param tx
     * @param user
     * @param roles
     * @return the number of removed roles
     * @throws SecurityManagementException
     */
    int removeMappedRoles ( EntityTransactionContext tx, UserPrincipal user, Set<String> roles ) throws SecurityManagementException;

}
