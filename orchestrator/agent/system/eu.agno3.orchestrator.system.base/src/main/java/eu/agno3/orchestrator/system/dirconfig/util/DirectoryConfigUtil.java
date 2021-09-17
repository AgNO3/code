/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.11.2014 by mbechler
 */
package eu.agno3.orchestrator.system.dirconfig.util;


import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;


/**
 * @author mbechler
 *
 */
public final class DirectoryConfigUtil {

    /**
     * 
     */
    private DirectoryConfigUtil () {}


    /**
     * @param dir
     * @return a config directory writer
     */
    public static DirectoryWriter getWriter ( Path dir ) {
        return new DirectoryWriterImpl(dir, null, null, false, false);
    }


    /**
     * 
     * @param dir
     * @param user
     * @return a config directory writer
     */
    public static DirectoryWriter getWriter ( Path dir, UserPrincipal user ) {
        return new DirectoryWriterImpl(dir, user, null, false, false);
    }


    /**
     * 
     * @param dir
     * @param user
     * @param group
     * @param groupWrite
     * @return a config directory writer
     */
    public static DirectoryWriter getWriter ( Path dir, UserPrincipal user, GroupPrincipal group, boolean groupWrite ) {
        return new DirectoryWriterImpl(dir, user, group, true, groupWrite);
    }

}
