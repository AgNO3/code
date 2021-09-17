/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.09.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.user;


import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.system.account.util.UnixAccountException;
import eu.agno3.orchestrator.system.account.util.UnixAccountUtil;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class EnsureMembership extends AbstractExecutionUnit<StatusOnlyResult, EnsureMembership, EnsureMembershipConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = -4497874060911126385L;
    private static final Logger log = Logger.getLogger(EnsureMembership.class);

    private static final String ADDUSER = "/usr/sbin/adduser"; //$NON-NLS-1$

    private String group;
    private String user;


    /**
     * @return the group
     */
    public String getGroup () {
        return this.group;
    }


    /**
     * @param group
     *            the group to set
     */
    void setGroup ( String group ) {
        this.group = group;
    }


    /**
     * @return the user
     */
    public String getUser () {
        return this.user;
    }


    /**
     * @param user
     *            the user to set
     */
    void setUser ( String user ) {
        this.user = user;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult prepare ( Context context ) throws ExecutionException {
        return new StatusOnlyResult(Status.SKIPPED);
    }


    /**
     * @return
     * @throws ExecutionException
     * @throws IOException
     */
    private boolean checkExists () throws ExecutionException {
        try {
            GroupPrincipal g;
            try {
                g = FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByGroupName(this.getGroup());
                UnixAccountUtil.getGroupId(g);
                if ( log.isDebugEnabled() ) {
                    log.debug("Group exists " + this.getGroup()); //$NON-NLS-1$
                }
            }
            catch ( UserPrincipalNotFoundException e ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Group does not exist " + this.getGroup(), e); //$NON-NLS-1$
                }
                throw new ExecutionException("Group does not exist " + this.getGroup()); //$NON-NLS-1$
            }

            UserPrincipal u;
            try {
                u = FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(this.getUser());
                UnixAccountUtil.getUserId(u);
                if ( log.isDebugEnabled() ) {
                    log.debug("User exists " + this.getUser()); //$NON-NLS-1$
                }
            }
            catch ( UserPrincipalNotFoundException e ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("User does not exist " + this.getUser(), e); //$NON-NLS-1$
                }
                throw new ExecutionException("User does not exist " + this.getUser()); //$NON-NLS-1$
            }

            Set<UserPrincipal> members = UnixAccountUtil.getMembers(g);
            return members.contains(u);
        }
        catch (
            IOException |
            UnixAccountException e ) {
            throw new ExecutionException("Failed to check whether group membership exists", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult execute ( Context context ) throws ExecutionException {
        if ( !context.getConfig().isNoVerifyEnv() && checkExists() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("User %s is member of %s, skip", this.getUser(), this.getGroup()); //$NON-NLS-1$
            }
            return new StatusOnlyResult(Status.SKIPPED);
        }

        context.getOutput().info(String.format("Adding user %s to group %s", getUser(), getGroup())); //$NON-NLS-1$

        if ( !context.getConfig().isDryRun() ) {
            try {
                checkExecutable(ADDUSER);

                List<String> args = new LinkedList<>();
                args.add(ADDUSER);
                args.add(getUser());
                args.add(getGroup());

                ProcessBuilder pb = new ProcessBuilder(args.toArray(new String[] {}));
                execNoIO(pb, "Failed to add user to group, return error code"); //$NON-NLS-1$
            }
            catch (
                IOException |
                InterruptedException |
                UnixAccountException e ) {
                throw new ExecutionException("Failed to add user to group", e); //$NON-NLS-1$
            }
        }

        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * @param pb
     * @throws IOException
     * @throws InterruptedException
     * @throws UnixAccountException
     */
    protected static void execNoIO ( ProcessBuilder pb, String errStr ) throws IOException, InterruptedException, UnixAccountException {
        Process p = pb.start();
        p.getOutputStream().close();
        if ( p.waitFor() != 0 ) {
            throw new UnixAccountException(errStr);
        }
    }


    /**
     * @param id2
     * @throws UnixAccountException
     */
    private static void checkExecutable ( String exec ) throws UnixAccountException {
        File f = new File(exec);

        if ( !f.canExecute() ) {
            throw new UnixAccountException("Cannot find executable " + exec); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public EnsureMembershipConfigurator createConfigurator () {
        return new EnsureMembershipConfigurator(this);
    }

}
