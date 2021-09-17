/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.admin;


import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.GroupNotFoundException;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.query.SubjectQueryResult;
import eu.agno3.runtime.jmx.MBean;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public interface SubjectServiceMBean extends MBean {

    /**
     * Get a subject
     * 
     * Access control:
     * - user is subject or member of subject
     * - OR have manage:subjects:list
     * 
     * @param id
     * @return the subject
     * @throws FileshareException
     */
    Subject getSubject ( UUID id ) throws FileshareException;


    /**
     * Get subject info
     * 
     * Access control:
     * - have manage:subjects:list
     * - OR have subjects:query
     * - if neither, only known subjects will be queried (known subject currently include the users/groups that shared
     * to the current user)
     * 
     * @param id
     * @return the subject info
     * @throws FileshareException
     */
    SubjectQueryResult getSubjectInfo ( UUID id ) throws FileshareException;


    /**
     * 
     * Get subject info
     * 
     * Access control:
     * - have manage:subjects:list
     * - OR have subjects:query
     * - if neither, only known subjects will be queried (known subject currently include the users/groups that shared
     * to the current user)
     * 
     * @param principal
     * @return the subject info
     * @throws FileshareException
     */
    SubjectQueryResult getUserInfo ( UserPrincipal principal ) throws FileshareException;


    /**
     * Query subjects
     * 
     * Access control:
     * - have manage:subjects:list
     * - OR have subjects:query
     * - if neither, only known subjects will be queried (known subject currently include the users/groups that shared
     * to the current user)
     * 
     * @param query
     * @param i
     * @return the query results
     * @throws FileshareException
     */
    List<SubjectQueryResult> querySubjects ( String query, int i ) throws FileshareException;


    /**
     * Query subjects excluding members of a given group
     * 
     * Access control:
     * - have manage:subjects:list
     * OR
     * - have subjects:query AND current user is member of group
     * 
     * @param query
     * @param groupId
     * @param i
     * @return the query results
     * @throws FileshareException
     * @throws GroupNotFoundException
     * @throws AccessDeniedException
     */
    List<SubjectQueryResult> querySubjectsExcludingMembers ( String query, UUID groupId, int i ) throws FileshareException;


    /**
     * Add a role to a subject
     * 
     * Access control:
     * - have manage:roles:add
     * 
     * @param id
     * @param role
     * @throws FileshareException
     */
    void addRole ( UUID id, String role ) throws FileshareException;


    /**
     * Set roles to a subject
     * 
     * Access control:
     * - have manage:roles:set
     * 
     * @param id
     * @param roles
     * @throws FileshareException
     */
    void setRoles ( UUID id, Set<String> roles ) throws FileshareException;


    /**
     * Remove a role from a subject
     * 
     * - have manage:roles:remove
     * 
     * @param id
     * @param role
     * @throws FileshareException
     */
    void removeRole ( UUID id, String role ) throws FileshareException;


    /**
     * Get the effective roles of the subject
     * 
     * Access control:
     * - have manage:roles:list
     * 
     * @param id
     * @return the effective roles of the subject
     * @throws FileshareException
     */
    Set<String> getEffectiveRoles ( UUID id ) throws FileshareException;


    /**
     * Get roles known to the server
     * 
     * Access control:
     * - have manage:roles:add
     * 
     * @return the roles known by the server
     * @throws FileshareException
     */
    Collection<String> getAvailableRoles () throws FileshareException;


    /**
     * Get the user subject root
     * 
     * Access control:
     * - have manage:subjects:list
     * 
     * @param subjectId
     * @return the user's subject root label
     * @throws FileshareException
     */
    SecurityLabel getSubjectRootLabel ( UUID subjectId ) throws FileshareException;


    /**
     * 
     * Access control:
     * - have manage:subjects:subjectRootSecurityLabel
     * 
     * @param id
     * @param label
     * @throws FileshareException
     */
    void setSubjectRootSecurityLabel ( UUID id, String label ) throws FileshareException;


    /**
     * 
     * Access control:
     * - have manage:subjects:subjectRootSecurityLabel
     * 
     * @param id
     * @param label
     * @throws FileshareException
     */
    void setSubjectRootSecurityLabelRecursive ( UUID id, String label ) throws FileshareException;

}
