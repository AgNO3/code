/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 31, 2017 by mbechler
 */
package eu.agno3.orchestrator.system.acl.util;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntry.Builder;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.file.util.FileSystemUtil;


/**
 * @author mbechler
 *
 */
public final class ACLUtil {

    private static final Logger log = Logger.getLogger(ACLUtil.class);

    private static final int CACHE_SIZE = 20;
    private static final Map<String, Boolean> ACL_SUPPORT_CACHE = new LRUMap<>(CACHE_SIZE);

    private static final Set<String> DEFAULT_ENABLED_FS_TYPES = new HashSet<>(Arrays.asList(
        "ext4", //$NON-NLS-1$
        // rootfs gets returned for the root fs ... which we know is ext4
        "rootfs")); //$NON-NLS-1$


    /**
     * 
     */
    private ACLUtil () {}


    /**
     * 
     * @param p
     * @return whether acls are supported for that path
     * @throws IOException
     */
    public static boolean aclsSupported ( Path p ) throws IOException {
        FileStore fs = Files.getFileStore(p);
        return aclsSupported(fs);
    }


    /**
     * @param fs
     * @return whether acls are supported for that file store
     * @throws IOException
     */
    public static boolean aclsSupported ( FileStore fs ) throws IOException {
        Path root = FileSystemUtil.getRootPath(fs);
        if ( root == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Failure looking up root path for " + fs.name()); //$NON-NLS-1$
            }
            return false;
        }
        String spath = root.toString();
        Boolean cached = ACL_SUPPORT_CACHE.get(spath);
        if ( cached != null ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Cached " + cached); //$NON-NLS-1$
            }
            return cached;
        }
        boolean res = false;
        try {
            String fsType = FileSystemUtil.getFsType(fs);
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Filesystem %s fstype %s", spath, fsType)); //$NON-NLS-1$
            }

            if ( fsType == null ) {
                log.debug("Unable to determine filesystem type on " + spath); //$NON-NLS-1$
                return res;
            }

            res = DEFAULT_ENABLED_FS_TYPES.contains(fsType) || FileSystemUtil.hasMountOption(fs, "acl") || //$NON-NLS-1$
                    FileSystemUtil.hasMountOption(fs, "posixacl"); //$NON-NLS-1$
        }
        finally {
            ACL_SUPPORT_CACHE.put(spath, res);
        }
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("ACL support on %s: %s", spath, res)); //$NON-NLS-1$
        }
        return res;
    }


    /**
     * 
     * @param t
     * @param acl
     * @throws IOException
     */
    public static void setACL ( Path t, List<AclEntry> acl ) throws IOException {

        Set<PosixFilePermission> posix = Files.getPosixFilePermissions(t);
        String posixStr = PosixFilePermissions.toString(posix);

        List<String> base = new LinkedList<>();
        base.add("user::" + posixStr.substring(0, 3)); //$NON-NLS-1$
        base.add("group::" + posixStr.substring(3, 6)); //$NON-NLS-1$
        base.add("other::" + posixStr.substring(6, 9)); //$NON-NLS-1$

        if ( log.isDebugEnabled() ) {
            log.debug("Base permissions are " + base); //$NON-NLS-1$
        }

        execNoOutput(
            Stream.concat(base.stream(), acl.stream().map(x -> {
                String name = x.principal().getName();
                String perms = mapPermissions(x.permissions());
                if ( x.principal() instanceof GroupPrincipal ) {
                    return String.format("group:%s:%s", name, perms); //$NON-NLS-1$
                }
                return String.format("user:%s:%s", name, perms); //$NON-NLS-1$
            })),
            "/bin/setfacl", //$NON-NLS-1$
            "-P", //$NON-NLS-1$
            "-n", //$NON-NLS-1$
            "--set-file=-", //$NON-NLS-1$
            t.toString());
    }


    /**
     * @param t
     * @return ACL
     * @throws IOException
     */
    public static List<AclEntry> getACL ( Path t ) throws IOException {
        UserPrincipalLookupService ups = FileSystems.getDefault().getUserPrincipalLookupService();
        try {
            return execNoInput(
                x -> x.charAt(0) != '#' && !x.startsWith("user::") && //$NON-NLS-1$
                        !x.startsWith("group::") && //$NON-NLS-1$
                        !x.startsWith("other::") && //$NON-NLS-1$
                        !x.startsWith("mask:"), //$NON-NLS-1$
                x -> {
                    return parseAclEntry(ups, x);
                } ,
                "/bin/getfacl", //$NON-NLS-1$
                "-P", //$NON-NLS-1$
                "-p", //$NON-NLS-1$
                "-a", //$NON-NLS-1$
                "-c", //$NON-NLS-1$
                "-E", //$NON-NLS-1$
                t.toString());
        }
        catch ( IllegalArgumentException e ) {
            throw new IOException("Failed to parse output", e); //$NON-NLS-1$
        }
    }


    /**
     * @param ups
     * @param x
     * @return
     */
    private static AclEntry parseAclEntry ( UserPrincipalLookupService ups, String x ) {

        if ( log.isDebugEnabled() ) {
            log.debug("Have ACL line " + x); //$NON-NLS-1$
        }

        String[] parts = StringUtils.split(x, ':');

        if ( parts == null || parts.length != 3 ) {
            throw new IllegalArgumentException("Invalid line format " + x); //$NON-NLS-1$
        }

        Builder b = AclEntry.newBuilder().setType(AclEntryType.ALLOW);
        String type = parts[ 0 ];
        String princName = parts[ 1 ];
        String perms = parts[ 2 ];

        try {
            if ( "user".equals(type) ) { //$NON-NLS-1$
                b.setPrincipal(ups.lookupPrincipalByName(princName));
            }
            else if ( "group".equals(type) ) { //$NON-NLS-1$
                b.setPrincipal(ups.lookupPrincipalByGroupName(princName));
            }
            else {
                throw new IllegalArgumentException("Invalid ACL type " + type); //$NON-NLS-1$
            }
        }
        catch ( IOException e ) {
            log.warn("Failed to lookup principal for name " + princName, e); //$NON-NLS-1$
        }

        b.setPermissions(mapPermissions(perms));

        return b.build();
    }


    /**
     * @param permsString
     * @return
     */
    private static Set<AclEntryPermission> mapPermissions ( String permsString ) {
        Set<AclEntryPermission> perms = EnumSet.noneOf(AclEntryPermission.class);
        if ( permsString.charAt(0) == 'r' ) {
            perms.add(AclEntryPermission.READ_DATA);
        }
        if ( permsString.charAt(1) == 'w' ) {
            perms.add(AclEntryPermission.WRITE_DATA);
        }
        if ( permsString.charAt(2) == 'x' ) {
            perms.add(AclEntryPermission.EXECUTE);
        }
        return perms;
    }


    /**
     * @param permissions
     * @return
     */
    private static String mapPermissions ( Set<AclEntryPermission> permissions ) {
        StringBuilder sb = new StringBuilder();
        if ( permissions.contains(AclEntryPermission.READ_DATA) ) {
            sb.append('r');
        }
        else {
            sb.append('-');
        }

        if ( permissions.contains(AclEntryPermission.WRITE_DATA) ) {
            sb.append('w');
        }
        else {
            sb.append('-');
        }

        if ( permissions.contains(AclEntryPermission.EXECUTE) ) {
            sb.append('x');
        }
        else {
            sb.append('-');
        }
        return sb.toString();
    }


    private static <R> R exec ( Function<Process, R> handler, boolean redirectOutput, String... cmd ) throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("Cmdline is " + Arrays.toString(cmd)); //$NON-NLS-1$
        }
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.environment().clear();
        pb.directory(new File("/")); //$NON-NLS-1$
        Path t = null;
        try {
            t = Files.createTempFile(
                "aclerr", //$NON-NLS-1$
                ".log", //$NON-NLS-1$
                PosixFilePermissions.asFileAttribute(FileSecurityUtils.getOwnerOnlyFilePermissions()));

            if ( redirectOutput ) {
                pb.redirectErrorStream(true);
                pb.redirectOutput(Redirect.to(t.toFile()));
            }
            else {
                pb.redirectError(Redirect.to(t.toFile()));
            }

            Process p = pb.start();
            try {
                return handler.apply(p);
            }
            catch ( RuntimeIOException e ) {
                throw (IOException) e.getCause(); // $NON-NLS-1$
            }
            finally {
                int exit = -1;

                try {
                    if ( !p.waitFor(1000, TimeUnit.MILLISECONDS) ) {
                        log.warn("Execution did not return within timeout, killing"); //$NON-NLS-1$
                        p.destroyForcibly();
                        exit = p.waitFor();
                    }
                    else {
                        exit = p.exitValue();
                    }
                }
                catch ( InterruptedException e ) {
                    log.warn("Interrupted during execution, killing"); //$NON-NLS-1$
                    p.destroyForcibly();
                    throw new IOException("Failed to execute command", e); //$NON-NLS-1$
                }

                if ( exit != 0 ) {
                    throw new IOException("Command exited with error " + exit); //$NON-NLS-1$
                }
            }

        }
        finally {
            if ( t != null ) {
                try {
                    List<String> errors = Files.readAllLines(t, StandardCharsets.US_ASCII);
                    if ( errors != null && !errors.isEmpty() ) {
                        log.warn("Error output:\n" + StringUtils.join(errors, '\n')); //$NON-NLS-1$
                    }
                }
                catch ( IOException e ) {
                    log.warn("Failed to read error output", e); //$NON-NLS-1$
                }
                Files.deleteIfExists(t);
            }
        }

    }


    private static void execNoOutput ( Stream<String> inputs, String... cmd ) throws IOException {
        exec(p -> {
            try {
                try ( OutputStream os = p.getOutputStream();
                      OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.US_ASCII);
                      BufferedWriter bw = new BufferedWriter(osw) ) {
                    inputs.forEach(x -> {
                        try {
                            if ( log.isTraceEnabled() ) {
                                log.trace("Write line " + x); //$NON-NLS-1$
                            }
                            bw.write(x);
                            bw.write('\n');
                        }
                        catch ( IOException e ) {
                            throw new RuntimeIOException(e);
                        }
                    });
                }
            }
            catch ( IOException e ) {
                throw new RuntimeIOException(e);
            }
            return null;
        } , true, cmd);
    }


    private static <R> List<R> execNoInput ( Predicate<String> filter, Function<String, R> mapper, String... cmd ) throws IOException {
        return exec(p -> {
            List<R> output = new LinkedList<>();
            try {
                p.getOutputStream().close();
                try ( InputStream is = p.getInputStream();
                      InputStreamReader isr = new InputStreamReader(is, StandardCharsets.US_ASCII);
                      BufferedReader br = new BufferedReader(isr) ) {
                    String line;
                    while ( ( line = br.readLine() ) != null ) {
                        if ( log.isTraceEnabled() ) {
                            log.trace("Read line " + line); //$NON-NLS-1$
                        }
                        line = line.trim();
                        if ( !line.isEmpty() && filter.test(line) ) {
                            output.add(mapper.apply(line));
                        }
                    }

                    return output;
                }
            }
            catch ( IOException e ) {
                throw new RuntimeIOException(e);
            }
        } , false, cmd);
    }

}
