/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.11.2014 by mbechler
 */
package eu.agno3.orchestrator.system.dirconfig.util;


import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;


/**
 * @author mbechler
 *
 */
public interface DirectoryWriter {

    /**
     * @param pid
     * @param props
     * @return a sha512 hash of the file contents
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    byte[] createConfig ( String pid, Properties props ) throws IOException, NoSuchAlgorithmException;


    /**
     * @param pid
     * @param instance
     * @param props
     * @return a sha512 hash of the file contents
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    byte[] createConfig ( String pid, String instance, Properties props ) throws IOException, NoSuchAlgorithmException;


    /**
     * @param pid
     * @param props
     * @return a sha512 hash of the new file contents
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    byte[] updateConfig ( String pid, Properties props ) throws NoSuchAlgorithmException, IOException;


    /**
     * @param pid
     * @param instance
     * @param props
     * @return a sha512 hash of the new file contents
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    byte[] updateConfig ( String pid, String instance, Properties props ) throws NoSuchAlgorithmException, IOException;


    /**
     * @param pid
     * @throws IOException
     */
    void removeConfig ( String pid ) throws IOException;


    /**
     * @param pid
     * @param instance
     * @throws IOException
     */
    void removeConfig ( String pid, String instance ) throws IOException;


    /**
     * @param pid
     * @return the config properties
     * @throws IOException
     */
    Properties readConfig ( String pid ) throws IOException;


    /**
     * @param pid
     * @param instance
     * @return the config properties
     * @throws IOException
     */
    Properties readConfig ( String pid, String instance ) throws IOException;


    /**
     * @param pid
     * @param instance
     * @return whether the configuration exists
     */
    boolean exists ( String pid, String instance );


    /**
     * @param pid
     * @return whether the configuration exists
     */
    boolean exists ( String pid );

}