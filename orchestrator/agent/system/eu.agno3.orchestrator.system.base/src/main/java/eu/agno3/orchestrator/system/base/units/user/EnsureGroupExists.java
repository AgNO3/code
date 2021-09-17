/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.09.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.user;


import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.system.account.util.UnixAccountException;
import eu.agno3.orchestrator.system.account.util.UnixAccountUtil;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class EnsureGroupExists extends AbstractExecutionUnit<StatusOnlyResult, EnsureGroupExists, EnsureGroupExistsConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 3080632595318001636L;

    private static final Logger log = Logger.getLogger(EnsureGroupExists.class);

    private static final String ADDGROUP = "/usr/sbin/addgroup"; //$NON-NLS-1$

    private String name;
    private Integer gid;
    private boolean system;


    /**
     * @param name
     *            the name to set
     */
    void setName ( String name ) {
        this.name = name;
    }


    /**
     * @param system
     *            the system to set
     */
    void setSystem ( boolean system ) {
        this.system = system;
    }


    /**
     * @param gid
     */
    void setGid ( int gid ) {
        this.gid = gid;
    }


    /**
     * @return the name
     */
    public String getName () {
        return this.name;
    }


    /**
     * @return the gid
     */
    public Integer getGid () {
        return this.gid;
    }


    /**
     * @return the system
     */
    public boolean isSystem () {
        return this.system;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.impl.AbstractExecutionUnit#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context context ) throws ExecutionException {
        super.validate(context);

        if ( StringUtils.isEmpty(getName()) ) {
            throw new InvalidParameterException("Need group name"); //$NON-NLS-1$
        }

        if ( !context.getConfig().isNoVerifyEnv() ) {
            try {
                checkExecutable(ADDGROUP);
            }
            catch ( UnixAccountException e ) {
                throw new ExecutionException("adduser binary does not exist", e); //$NON-NLS-1$
            }
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult prepare ( Context context ) throws ExecutionException {
        if ( !context.getConfig().isNoVerifyEnv() && checkGroupExists() ) {
            return new StatusOnlyResult(Status.SKIPPED);
        }

        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * @return
     * @throws ExecutionException
     * @throws IOException
     */
    private boolean checkGroupExists () throws ExecutionException {

        try {
            GroupPrincipal group = FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByGroupName(this.getName());

            int groupId = UnixAccountUtil.getGroupId(group);

            if ( this.getGid() != null && groupId != this.getGid() ) {
                throw new ExecutionException("Group does already exist with different gid " + groupId); //$NON-NLS-1$
            }
        }
        catch ( UserPrincipalNotFoundException e ) {
            log.debug("Group not found " + this.getName(), e); //$NON-NLS-1$
            return false;
        }
        catch (
            IOException |
            UnixAccountException e ) {
            throw new ExecutionException("Failed to check whether group exists", e); //$NON-NLS-1$
        }

        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult execute ( Context context ) throws ExecutionException {

        if ( !context.getConfig().isNoVerifyEnv() && checkGroupExists() ) {
            log.debug("Group already exists " + this.getName()); //$NON-NLS-1$
            return new StatusOnlyResult(Status.SKIPPED);
        }

        context.getOutput().info("Creating group " + getName()); //$NON-NLS-1$
        if ( !context.getConfig().isDryRun() ) {
            try {
                checkExecutable(ADDGROUP);

                List<String> args = new LinkedList<>();
                args.add(ADDGROUP);
                args.add("--force-badname"); //$NON-NLS-1$
                if ( this.isSystem() ) {
                    args.add("--system"); //$NON-NLS-1$
                }

                if ( this.getGid() != null ) {
                    args.add("--gid"); //$NON-NLS-1$
                    args.add(String.valueOf(getGid()));
                }

                args.add(getName());
                ProcessBuilder pb = new ProcessBuilder(args.toArray(new String[] {}));
                execNoIO(pb, "Failed to add group, return error code"); //$NON-NLS-1$
            }
            catch (
                IOException |
                InterruptedException |
                UnixAccountException e ) {
                throw new ExecutionException("Failed to add group", e); //$NON-NLS-1$
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
    public EnsureGroupExistsConfigurator createConfigurator () {
        return new EnsureGroupExistsConfigurator(this);
    }

}
