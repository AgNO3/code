/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.02.2015 by mbechler
 */
package eu.agno3.runtime.security.ldap.internal;


import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.agno3.runtime.ldap.client.LDAPClientFactory;
import eu.agno3.runtime.security.ldap.LDAPGroup;
import eu.agno3.runtime.security.ldap.LDAPRealmConfig;
import eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler;
import eu.agno3.runtime.security.ldap.LDAPUser;
import eu.agno3.runtime.util.iter.ClosableIterator;
import eu.agno3.runtime.util.iter.NoCloseIterator;


/**
 * @author mbechler
 *
 */
public class LoggingSynchronizationHandler implements LDAPSynchronizationHandler {

    private static final Logger log = Logger.getLogger(LoggingSynchronizationHandler.class);

    private LDAPSynchronizationHandler delegate;


    /**
     * @param realHandler
     */
    public LoggingSynchronizationHandler ( LDAPSynchronizationHandler realHandler ) {
        this.delegate = realHandler;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getLastRun()
     */
    @Override
    public DateTime getLastRun () {
        if ( this.delegate != null ) {
            return this.delegate.getLastRun();
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getClientFactory()
     */
    @Override
    public LDAPClientFactory getClientFactory () {
        return this.delegate.getClientFactory();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getConfig()
     */
    @Override
    public LDAPRealmConfig getConfig () {
        return this.delegate.getConfig();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getRealm()
     */
    @Override
    public String getRealm () {
        return this.delegate.getRealm();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#userExistsById(java.util.UUID)
     */
    @Override
    public boolean userExistsById ( UUID uuid ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("userExistsById: %s", uuid)); //$NON-NLS-1$
        }

        if ( this.delegate != null ) {
            return this.delegate.userExistsById(uuid);
        }
        return false;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getUserByName(java.lang.String)
     */
    @Override
    public UUID getUserByName ( String name ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("getUserByName: %s", name)); //$NON-NLS-1$
        }

        if ( this.delegate != null ) {
            return this.delegate.getUserByName(name);
        }
        return null;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getUserByDN(java.lang.String)
     */
    @Override
    public UUID getUserByDN ( String dn ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("getUserByDN: %s", dn)); //$NON-NLS-1$
        }

        if ( this.delegate != null ) {
            return this.delegate.getUserByDN(dn);
        }
        return null;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#groupExistsById(java.util.UUID)
     */
    @Override
    public boolean groupExistsById ( UUID uuid ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("groupExistsById: %s", uuid)); //$NON-NLS-1$
        }

        if ( this.delegate != null ) {
            return this.delegate.groupExistsById(uuid);
        }
        return false;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getGroupByDN(java.lang.String)
     */
    @Override
    public UUID getGroupByDN ( String dn ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("getGroupByDn: %s", dn)); //$NON-NLS-1$
        }

        if ( this.delegate != null ) {
            return this.delegate.getGroupByDN(dn);
        }
        return null;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getGroupByName(java.lang.String)
     */
    @Override
    public UUID getGroupByName ( String name ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("getGroupByName: %s", name)); //$NON-NLS-1$
        }

        if ( this.delegate != null ) {
            return this.delegate.getGroupByName(name);
        }
        return null;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#createGroup(java.lang.String, java.util.UUID,
     *      eu.agno3.runtime.security.ldap.LDAPGroup)
     */
    @Override
    public UUID createGroup ( String dn, UUID directoryId, LDAPGroup groupEntry ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("createGroup: %s", groupEntry)); //$NON-NLS-1$
        }

        if ( this.delegate != null ) {
            return this.delegate.createGroup(dn, directoryId, groupEntry);
        }

        return null;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#updateGroup(java.util.UUID, java.lang.String,
     *      eu.agno3.runtime.security.ldap.LDAPGroup)
     */
    @Override
    public void updateGroup ( UUID groupId, String dn, LDAPGroup groupEntry ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("updateGroup %s: %s", groupId, groupEntry)); //$NON-NLS-1$
        }

        if ( this.delegate != null ) {
            this.delegate.updateGroup(groupId, dn, groupEntry);
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#createUser(java.lang.String, java.util.UUID,
     *      eu.agno3.runtime.security.ldap.LDAPUser, java.util.Set)
     */
    @Override
    public UUID createUser ( String dn, UUID directoryId, LDAPUser userEntry, Set<String> roles ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("createUser: %s", userEntry)); //$NON-NLS-1$
        }

        if ( this.delegate != null ) {
            return this.delegate.createUser(dn, directoryId, userEntry, roles);
        }

        return null;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#updateUser(java.util.UUID, java.lang.String,
     *      eu.agno3.runtime.security.ldap.LDAPUser, java.util.Set)
     */
    @Override
    public void updateUser ( UUID userId, String dn, LDAPUser userEntry, Set<String> roles ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("updateUser %s: %s", userId, userEntry)); //$NON-NLS-1$
        }

        if ( this.delegate != null ) {
            this.delegate.updateUser(userId, dn, userEntry, roles);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#setMembers(java.util.UUID, java.util.Set)
     */
    @Override
    public void setMembers ( UUID groupId, Set<UUID> memberIds ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("setMembers %s: %s", groupId, memberIds)); //$NON-NLS-1$
        }

        if ( this.delegate != null ) {
            this.delegate.setMembers(groupId, memberIds);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#setNestedGroups(java.util.UUID, java.util.Set)
     */
    @Override
    public void setNestedGroups ( UUID groupId, Set<UUID> nestedGroups ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("setNestedGroups %s: %s", groupId, nestedGroups)); //$NON-NLS-1$
        }

        if ( this.delegate != null ) {
            this.delegate.setNestedGroups(groupId, nestedGroups);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#setForwardMembership(java.util.UUID,
     *      java.util.Set)
     */
    @Override
    public void setForwardMembership ( UUID userId, Set<UUID> membershipUUIDs ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("setForwardMemberships %s: %s", userId, membershipUUIDs)); //$NON-NLS-1$
        }

        if ( this.delegate != null ) {
            this.delegate.setForwardMembership(userId, membershipUUIDs);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#setForwardNestedGroups(java.util.UUID,
     *      java.util.Set)
     */
    @Override
    public void setForwardNestedGroups ( UUID groupId, Set<UUID> forwardNestedGroups ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("setForwardNestedGroups %s: %s", groupId, forwardNestedGroups)); //$NON-NLS-1$
        }

        if ( this.delegate != null ) {
            this.delegate.setForwardNestedGroups(groupId, forwardNestedGroups);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getUserIds()
     */
    @Override
    public ClosableIterator<UUID> getUserIds () {
        if ( log.isDebugEnabled() ) {
            log.debug("getUserIDs()"); //$NON-NLS-1$
        }
        if ( this.delegate != null ) {
            this.delegate.getUserIds();
        }

        return new NoCloseIterator<>(Collections.EMPTY_LIST.iterator());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getGroupIds()
     */
    @Override
    public ClosableIterator<UUID> getGroupIds () {
        if ( log.isDebugEnabled() ) {
            log.debug("getGroupIds()"); //$NON-NLS-1$
        }
        if ( this.delegate != null ) {
            this.delegate.getGroupIds();
        }

        return new NoCloseIterator<>(Collections.EMPTY_LIST.iterator());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getUserDNs()
     */
    @Override
    public ClosableIterator<String> getGroupDNs () {
        if ( log.isDebugEnabled() ) {
            log.debug("getGroupDNs()"); //$NON-NLS-1$
        }
        if ( this.delegate != null ) {
            this.delegate.getGroupDNs();
        }

        return new NoCloseIterator<>(Collections.EMPTY_LIST.iterator());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getUserDNs()
     */
    @Override
    public ClosableIterator<String> getUserDNs () {
        if ( log.isDebugEnabled() ) {
            log.debug("getUserDNs()"); //$NON-NLS-1$
        }
        if ( this.delegate != null ) {
            this.delegate.getUserDNs();
        }

        return new NoCloseIterator<>(Collections.EMPTY_LIST.iterator());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#removeGroup(java.util.UUID)
     */
    @Override
    public void removeGroup ( UUID groupId ) {
        if ( log.isDebugEnabled() ) {
            log.debug("removeGroup: " + groupId); //$NON-NLS-1$
        }

        if ( this.delegate != null ) {
            this.delegate.removeGroup(groupId);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#removeGroups(java.util.Set)
     */
    @Override
    public void removeGroups ( Set<UUID> toDelete ) {
        if ( log.isDebugEnabled() ) {
            log.debug("removeGroups: " + toDelete); //$NON-NLS-1$
        }

        if ( this.delegate != null ) {
            this.delegate.removeGroups(toDelete);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#removeUser(java.util.UUID)
     */
    @Override
    public void removeUser ( UUID userId ) {
        if ( log.isDebugEnabled() ) {
            log.debug("removeUser: " + userId); //$NON-NLS-1$
        }

        if ( this.delegate != null ) {
            this.delegate.removeUser(userId);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#removeUsers(java.util.Set)
     */
    @Override
    public void removeUsers ( Set<UUID> toDelete ) {
        if ( log.isDebugEnabled() ) {
            log.debug("removeUsers: " + toDelete); //$NON-NLS-1$
        }

        if ( this.delegate != null ) {
            this.delegate.removeUsers(toDelete);
        }
    }

}
