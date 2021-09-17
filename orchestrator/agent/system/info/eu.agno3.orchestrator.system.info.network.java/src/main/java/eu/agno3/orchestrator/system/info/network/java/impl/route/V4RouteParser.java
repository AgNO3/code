/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 10, 2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.network.java.impl.route;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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

import eu.agno3.orchestrator.system.info.network.RouteEntry;
import eu.agno3.orchestrator.system.info.network.RouteFlags;
import eu.agno3.orchestrator.types.net.AbstractIPAddress;
import eu.agno3.orchestrator.types.net.IPv4Address;
import eu.agno3.orchestrator.types.net.NetworkSpecification;


/**
 * @author mbechler
 *
 */
public class V4RouteParser {

    private static final String ROUTE_INFO_PATH = "/proc/net/route"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(V4RouteParser.class);


    /**
     * 
     * @return route entries
     * @throws IOException
     */
    public List<RouteEntry> parse () throws IOException {
        return parse(Paths.get(ROUTE_INFO_PATH));
    }


    /**
     * @param path
     * @return route entries
     * @throws IOException
     */
    public List<RouteEntry> parse ( Path path ) throws IOException {
        try ( FileChannel fc = FileChannel.open(path, StandardOpenOption.READ);
              InputStream is = Channels.newInputStream(fc) ) {
            return parse(is);
        }
    }


    /**
     * @param is
     * @return route entries
     * @throws IOException
     */
    public List<RouteEntry> parse ( InputStream is ) throws IOException {
        try ( Reader r = new InputStreamReader(is, StandardCharsets.US_ASCII);
              BufferedReader br = new BufferedReader(r) ) {
            return parse(br);
        }
    }


    /**
     * @param r
     * @return route entries
     * @throws IOException
     */
    public List<RouteEntry> parse ( BufferedReader r ) throws IOException {
        List<RouteEntry> entries = new ArrayList<>();
        int skip = 2;
        String line;
        while ( ( line = r.readLine() ) != null ) {
            if ( --skip > 0 ) {
                continue;
            }
            line = line.trim();
            if ( line.length() == 0 ) {
                continue;
            }
            RouteEntry re = parseLine(line);
            if ( re != null ) {
                entries.add(re);
            }
        }
        return entries;
    }


    /**
     * @param line
     * @return
     */
    RouteEntry parseLine ( String line ) {
        StringTokenizer tok = new StringTokenizer(line);
        try {
            RouteEntry re = new RouteEntry();
            re.setInterfaceName(tok.nextToken());
            IPv4Address dest = toNetworkAddress(Long.parseUnsignedLong(tok.nextToken(), 16));
            re.setGateway(toNetworkAddress(Long.parseUnsignedLong(tok.nextToken(), 16)));
            int flagsInt = Integer.parseUnsignedInt(tok.nextToken(), 16);
            re.setRef(Integer.parseUnsignedInt(tok.nextToken()));
            re.setUse(Integer.parseUnsignedInt(tok.nextToken()));
            re.setMetric(Integer.parseUnsignedInt(tok.nextToken()));
            long mask = Long.parseUnsignedLong(tok.nextToken(), 16);
            short prefixLen = maskToPrefix(mask);
            re.setMtu(Integer.parseUnsignedInt(tok.nextToken()));
            re.setWindow(Integer.parseUnsignedInt(tok.nextToken()));
            re.setIrtt(Integer.parseUnsignedInt(tok.nextToken()));

            re.setFlags(RouteFlags.parseFlags(flagsInt));
            if ( !re.getFlags().contains(RouteFlags.GATEWAY) ) {
                re.setGateway(null);
            }

            re.setNetwork(new NetworkSpecification(dest, prefixLen));

            if ( log.isDebugEnabled() ) {
                log.debug("Found route entry " + re); //$NON-NLS-1$
            }

            return re;
        }
        catch (
            IllegalArgumentException |
            NoSuchElementException e ) {
            log.warn("Skipping unparsable line " + line, e); //$NON-NLS-1$
        }
        return null;
    }


    private static IPv4Address toNetworkAddress ( long v ) {
        // Little endian
        long be = toBigEndian(v);
        return (IPv4Address) AbstractIPAddress.fromBytes(new short[] {
            (short) ( be >> 24 & 0xFF ), (short) ( be >> 16 & 0xFF ), (short) ( be >> 8 & 0xFF ), (short) ( be & 0xFF ),
        });
    }


    /**
     * @param mask
     * @return
     */
    private static short maskToPrefix ( long mask ) {
        // Little endian
        long be = toBigEndian(mask);
        return (short) Math.max(0, 32 - Long.numberOfTrailingZeros(be));
    }


    private static long toBigEndian ( long v ) {
        return ( v & 0xFF ) << 24 | ( v >> 8 & 0xFF ) << 16 | ( v >> 16 & 0xFF ) << 8 | ( v >> 24 & 0XFF );
    }
}
