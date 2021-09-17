/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2014 by mbechler
 */
package eu.agno3.orchestrator.system.account.util;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public final class UnixAccountUtil {

    private static final Logger log = Logger.getLogger(UnixAccountUtil.class);

    private static final String GPASSWD = "/usr/bin/gpasswd"; //$NON-NLS-1$
    private static final String GROUP_ADD = "/usr/sbin/groupadd"; //$NON-NLS-1$
    private static final String ID = "/usr/bin/id"; //$NON-NLS-1$
    private static final String MEMBERS = "/usr/bin/members"; //$NON-NLS-1$
    private static final Charset SYSTEM_CHARSET = Charset.forName("UTF-8"); //$NON-NLS-1$

    private static Field ID_FIELD;


    static {
        try {
            checkExecutable(GPASSWD);
            checkExecutable(ID);
            checkExecutable(MEMBERS);
        }
        catch ( UnixAccountException e ) {
            log.error("Cannot find required executable", e); //$NON-NLS-1$
        }

        try {
            Class<?> unixUserPrincipalCls = UnixAccountUtil.class.getClassLoader().loadClass("sun.nio.fs.UnixUserPrincipals$User"); //$NON-NLS-1$
            ID_FIELD = unixUserPrincipalCls.getDeclaredField("id"); //$NON-NLS-1$
            ID_FIELD.setAccessible(true);
        }
        catch (
            ClassNotFoundException |
            NoSuchFieldException |
            SecurityException e ) {
            log.error("Failed to get unix user id field", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     */
    private UnixAccountUtil () {}


    /**
     * @param name
     * @return the group principal for the given name
     * @throws UnixAccountException
     */
    public static GroupPrincipal resolveGroup ( String name ) throws UnixAccountException {
        try {
            return FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByGroupName(name);
        }
        catch ( IOException e ) {
            throw new UnixAccountException("Failed to get group principal for " + name, e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * @param up
     * @return the unix uid of the principal
     * @throws UnixAccountException
     */
    public static int getUserId ( UserPrincipal up ) throws UnixAccountException {
        try {
            return ID_FIELD.getInt(up);
        }
        catch (
            IllegalArgumentException |
            IllegalAccessException e ) {
            throw new UnixAccountException("Failed to get user id for " + up, e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * @param gp
     * @return the unix gid of the principal
     * @throws UnixAccountException
     */
    public static int getGroupId ( GroupPrincipal gp ) throws UnixAccountException {
        try {
            return ID_FIELD.getInt(gp);
        }
        catch (
            IllegalArgumentException |
            IllegalAccessException e ) {
            throw new UnixAccountException("Failed to get group id for " + gp, e); //$NON-NLS-1$
        }
    }


    /**
     * @param name
     * @return the created group principal
     * @throws UnixAccountException
     */
    public static GroupPrincipal createGroup ( String name ) throws UnixAccountException {
        checkExecutable(GROUP_ADD);
        ProcessBuilder pb = new ProcessBuilder(GROUP_ADD, "-r", name); //$NON-NLS-1$

        try {
            execNoIO(pb, "Failed to create group, return error code"); //$NON-NLS-1$
        }
        catch (
            IOException |
            InterruptedException e ) {
            throw new UnixAccountException("Failed to create group " + name, e); //$NON-NLS-1$
        }

        return resolveGroup(name);
    }


    /**
     * 
     * @param name
     * @return the user principal for the given name
     * @throws UnixAccountException
     */
    public static UserPrincipal resolveUser ( String name ) throws UnixAccountException {
        try {
            return FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(name);
        }
        catch ( IOException e ) {
            throw new UnixAccountException("Failed to get user principal " + name, e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * @param group
     * @return the users that are members of the given UNIX group
     * @throws UnixAccountException
     */
    public static Set<UserPrincipal> getMembers ( GroupPrincipal group ) throws UnixAccountException {
        checkExecutable(MEMBERS);

        Set<UserPrincipal> res = new HashSet<>();
        String gn = group.getName();
        ProcessBuilder pb = new ProcessBuilder(MEMBERS, gn);
        try {
            // return 1 if the group is empty
            for ( String user : execSingleLineSplit(
                pb,
                String.format("Failed to enumerate group %s members, returned error code", gn), //$NON-NLS-1$
                0,
                1) ) {
                res.add(resolveUser(user));
            }

            return res;
        }
        catch (
            IOException |
            InterruptedException e ) {
            throw new UnixAccountException("Failed to enumerate group members of " + group, e); //$NON-NLS-1$
        }

    }


    /**
     * @param pb
     * @param errStr
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws UnixAccountException
     */
    protected static String[] execSingleLineSplit ( ProcessBuilder pb, String errStr, int... acceptCodes )
            throws IOException, InterruptedException, UnixAccountException {
        Process proc = pb.start();
        String userLine;
        try ( BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream(), SYSTEM_CHARSET)) ) {
            proc.getOutputStream().close();
            int waitFor = proc.waitFor();
            if ( !checkReturn(waitFor, acceptCodes) ) {
                throw new UnixAccountException(errStr + ": " + waitFor); //$NON-NLS-1$
            }

            userLine = br.readLine();
        }

        if ( userLine == null ) {
            return new String[0];
        }

        return StringUtils.split(userLine);
    }


    /**
     * @param waitFor
     * @param acceptCodes
     * @return
     */
    private static boolean checkReturn ( int waitFor, int[] acceptCodes ) {
        int codes[] = acceptCodes;
        if ( codes == null || codes.length == 0 ) {
            codes = new int[] {
                0
            };
        }
        for ( int accept : codes ) {
            if ( accept == waitFor ) {
                return true;
            }
        }
        return false;
    }


    /**
     * 
     * @param user
     * @return the UNIX groups that the user is member of
     * @throws UnixAccountException
     */
    public static Set<GroupPrincipal> getUserGroups ( UserPrincipal user ) throws UnixAccountException {
        checkExecutable(ID);
        Set<GroupPrincipal> res = new HashSet<>();
        ProcessBuilder pb = new ProcessBuilder(ID, "-nG", user.getName()); //$NON-NLS-1$
        try {
            for ( String group : execSingleLineSplit(pb, String.format("Failed to enumerate user %s groups, returned error code", user.getName())) ) { //$NON-NLS-1$
                res.add(resolveGroup(group));
            }
            return res;
        }
        catch (
            IOException |
            InterruptedException e ) {
            throw new UnixAccountException("Failed to enumerate user groups for " + user, e); //$NON-NLS-1$
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
     * Add the given user to the UNIX group
     * 
     * @param group
     * @param user
     * @throws UnixAccountException
     */
    public static void addToGroup ( GroupPrincipal group, UserPrincipal user ) throws UnixAccountException {
        checkExecutable(GPASSWD);
        ProcessBuilder pb = new ProcessBuilder(GPASSWD, "-a", user.getName(), group.getName()); //$NON-NLS-1$

        try {
            execNoIO(pb, "Failed to add to group, return error code"); //$NON-NLS-1$
        }
        catch (
            IOException |
            InterruptedException e ) {
            throw new UnixAccountException(String.format("Failed to add %s to group %s", user, group), e); //$NON-NLS-1$
        }
    }


    /**
     * Remove the given user from the UNIX group
     * 
     * @param group
     * @param user
     * @throws UnixAccountException
     */
    public static void removeFromGroup ( GroupPrincipal group, UserPrincipal user ) throws UnixAccountException {
        checkExecutable(GPASSWD);
        ProcessBuilder pb = new ProcessBuilder(GPASSWD, "-d", user.getName(), group.getName()); //$NON-NLS-1$

        try {
            execNoIO(pb, "Failed to remove from group, return error code"); //$NON-NLS-1$
        }
        catch (
            IOException |
            InterruptedException e ) {
            throw new UnixAccountException(String.format("Failed to remove %s from group %s", user, group), e); //$NON-NLS-1$
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
        int exitCode = p.waitFor();
        if ( exitCode != 0 ) {
            throw new UnixAccountException(errStr + ": " + exitCode); //$NON-NLS-1$
        }
    }

}
