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
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

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
public class EnsureUserExists extends AbstractExecutionUnit<StatusOnlyResult, EnsureUserExists, EnsureUserExistsConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 3080632595318001636L;

    private static final String ADDUSER = "/usr/sbin/adduser"; //$NON-NLS-1$

    private String name;
    private boolean system;
    private String home;
    private String shell = "/bin/false"; //$NON-NLS-1$
    private Integer uid;

    private String primaryGroup;
    private List<String> groups;


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
     * @param uid
     */
    void setUid ( int uid ) {
        this.uid = uid;
    }


    /**
     * @param home
     *            the home to set
     */
    void setHome ( String home ) {
        this.home = home;
    }


    /**
     * @param shell
     *            the shell to set
     */
    void setShell ( String shell ) {
        this.shell = shell;
    }


    /**
     * @param groups
     *            the groups to set
     */
    void setGroups ( List<String> groups ) {
        this.groups = groups;
    }


    /**
     * @param primaryGroup
     *            the primaryGroup to set
     */
    void setPrimaryGroup ( String primaryGroup ) {
        this.primaryGroup = primaryGroup;
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
    public Integer getUid () {
        return this.uid;
    }


    /**
     * @return the primaryGroup
     */
    public String getPrimaryGroup () {
        return this.primaryGroup;
    }


    /**
     * @return the groups
     */
    public List<String> getGroups () {
        return this.groups;
    }


    /**
     * @return the home
     */
    public String getHome () {
        return this.home;
    }


    /**
     * @return the shell
     */
    public String getShell () {
        return this.shell;
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
                checkExecutable(ADDUSER);
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
        if ( context.getConfig().isNoVerifyEnv() || checkUserExists() ) {
            return new StatusOnlyResult(Status.SKIPPED);
        }

        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * @return
     * @throws ExecutionException
     * @throws IOException
     */
    private boolean checkUserExists () throws ExecutionException {
        try {
            UserPrincipal user = FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(this.getName());
            int userId = UnixAccountUtil.getUserId(user);
            if ( this.getUid() != null && userId != this.getUid() ) {
                throw new ExecutionException("User does already exist with uid " + userId); //$NON-NLS-1$
            }
        }
        catch ( UserPrincipalNotFoundException e ) {
            return false;
        }
        catch (
            IOException |
            UnixAccountException e ) {
            throw new ExecutionException("Failed to check whether user exists", e); //$NON-NLS-1$
        }

        return true;
    }


    /**
     * @param group
     * @throws ExecutionException
     */
    private static void checkGroupExists ( String group ) throws ExecutionException {
        try {
            FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByGroupName(group);
        }
        catch ( IOException e ) {
            throw new ExecutionException("Group does not exist " + group, e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult execute ( Context context ) throws ExecutionException {

        if ( !context.getConfig().isNoVerifyEnv() && checkUserExists() ) {
            return new StatusOnlyResult(Status.SKIPPED);
        }

        verifyGroups(context);

        context.getOutput().info("Creating user " + getName()); //$NON-NLS-1$
        if ( !context.getConfig().isDryRun() ) {
            try {
                checkExecutable(ADDUSER);
                List<String> args = makeArguments();
                ProcessBuilder pb = new ProcessBuilder(args.toArray(new String[] {}));
                execNoIO(pb, "Failed to add user, return error code"); //$NON-NLS-1$

            }
            catch (
                IOException |
                InterruptedException |
                UnixAccountException e ) {
                throw new ExecutionException("Failed to add user", e); //$NON-NLS-1$
            }

            try {
                addSupplementaryGroups();
            }
            catch (
                IOException |
                InterruptedException |
                UnixAccountException e ) {
                throw new ExecutionException("Failed to add supplementary groups", e); //$NON-NLS-1$
            }
        }

        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * @throws UnixAccountException
     * @throws InterruptedException
     * @throws IOException
     * 
     */
    private void addSupplementaryGroups () throws IOException, InterruptedException, UnixAccountException {
        if ( this.getGroups() == null ) {
            return;
        }

        for ( String group : this.getGroups() ) {
            List<String> args = new LinkedList<>();
            args.add(ADDUSER);
            args.add(getName());
            args.add(group);
            ProcessBuilder pb = new ProcessBuilder(args.toArray(new String[] {}));
            execNoIO(pb, "Failed to add supplementary group " + group); //$NON-NLS-1$
        }
    }


    /**
     * @return
     */
    private List<String> makeArguments () {
        List<String> args = new LinkedList<>();
        args.add(ADDUSER);
        args.add("--force-badname"); //$NON-NLS-1$
        args.add("--no-create-home"); //$NON-NLS-1$
        if ( this.isSystem() ) {
            args.add("--system"); //$NON-NLS-1$
        }

        if ( this.getHome() != null ) {
            args.add("--home"); //$NON-NLS-1$
            args.add(getHome());
        }

        if ( this.getShell() != null ) {
            args.add("--shell"); //$NON-NLS-1$
            args.add(getShell());
        }

        if ( this.getUid() != null ) {
            args.add("--uid"); //$NON-NLS-1$
            args.add(String.valueOf(getUid()));
        }

        if ( this.getPrimaryGroup() != null ) {
            args.add("--ingroup"); //$NON-NLS-1$
            args.add(getPrimaryGroup());
        }

        args.add("--disabled-password"); //$NON-NLS-1$

        args.add(getName());
        return args;
    }


    /**
     * @param context
     * @throws ExecutionException
     */
    private void verifyGroups ( Context context ) throws ExecutionException {
        if ( !context.getConfig().isNoVerifyEnv() && this.getPrimaryGroup() != null ) {
            checkGroupExists(this.getPrimaryGroup());
        }

        if ( !context.getConfig().isNoVerifyEnv() && this.getGroups() != null ) {
            for ( String group : this.getGroups() ) {
                checkGroupExists(group);
            }
        }
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
    public EnsureUserExistsConfigurator createConfigurator () {
        return new EnsureUserExistsConfigurator(this);
    }

}
