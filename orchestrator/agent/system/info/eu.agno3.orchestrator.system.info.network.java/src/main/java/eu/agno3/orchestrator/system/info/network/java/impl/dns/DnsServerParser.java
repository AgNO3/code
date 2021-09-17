/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 11, 2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.network.java.impl.dns;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.types.net.AbstractIPAddress;
import eu.agno3.orchestrator.types.net.NetworkAddress;


/**
 * @author mbechler
 *
 */
public class DnsServerParser {

    private static final String RESOLV_CONF = "/etc/resolv.conf"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(DnsServerParser.class);


    /**
     * 
     * @return active name servers
     * @throws IOException
     */
    public List<NetworkAddress> parse () throws IOException {
        return parse(Paths.get(RESOLV_CONF));
    }


    /**
     * @param path
     * @return active name servers
     * @throws IOException
     */
    public List<NetworkAddress> parse ( Path path ) throws IOException {
        try ( FileChannel fc = FileChannel.open(path, StandardOpenOption.READ);
              InputStream is = Channels.newInputStream(fc) ) {
            return parse(is);
        }
    }


    /**
     * @param is
     * @return active name servers
     * @throws IOException
     */
    public List<NetworkAddress> parse ( InputStream is ) throws IOException {
        try ( InputStreamReader isr = new InputStreamReader(is, StandardCharsets.US_ASCII);
              BufferedReader br = new BufferedReader(isr) ) {
            return parse(br);
        }
    }


    /**
     * @param br
     * @return active name servers
     * @throws IOException
     */
    public List<NetworkAddress> parse ( BufferedReader br ) throws IOException {
        List<NetworkAddress> servers = new ArrayList<>();
        String line;
        while ( ( line = br.readLine() ) != null ) {
            line = line.trim();
            if ( line.isEmpty() || line.charAt(0) == '#' ) {
                continue;
            }
            try {
                StringTokenizer tok = new StringTokenizer(line);
                String opt = tok.nextToken();
                if ( log.isDebugEnabled() ) {
                    log.debug("Found line " + line); //$NON-NLS-1$
                }
                if ( !"nameserver".equals(opt) ) { //$NON-NLS-1$
                    continue;
                }
                String server = tok.nextToken();
                AbstractIPAddress servAddr = AbstractIPAddress.parse(server);
                if ( log.isDebugEnabled() ) {
                    log.debug("Have server " + servAddr); //$NON-NLS-1$
                }
                servers.add(servAddr);
            }
            catch (
                IllegalArgumentException |
                NoSuchElementException e ) {
                log.warn("Failed to parse resolv.conf line", e); //$NON-NLS-1$
            }
        }
        return servers;
    }
}
