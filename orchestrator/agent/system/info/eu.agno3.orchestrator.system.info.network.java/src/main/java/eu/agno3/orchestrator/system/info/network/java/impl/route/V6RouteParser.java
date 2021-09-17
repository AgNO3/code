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
import eu.agno3.orchestrator.types.net.IPv6Address;
import eu.agno3.orchestrator.types.net.NetworkAddress;
import eu.agno3.orchestrator.types.net.NetworkSpecification;


/**
 * @author mbechler
 *
 */
public class V6RouteParser {

    private static final String ROUTE_INFO_PATH = "/proc/net/ipv6_route"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(V6RouteParser.class);


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
        String line;
        while ( ( line = r.readLine() ) != null ) {
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

            IPv6Address network = toNetworkAddress(tok.nextToken());

            short netPrefix = Short.parseShort(tok.nextToken(), 16);
            re.setNetwork(new NetworkSpecification(network, netPrefix));

            NetworkAddress source = toNetworkAddress(tok.nextToken());
            short sourcePrefix = Short.parseShort(tok.nextToken(), 16);
            if ( sourcePrefix > 0 ) {
                re.setSource(new NetworkSpecification(source, sourcePrefix));
            }

            re.setGateway(toNetworkAddress(tok.nextToken()));

            re.setMetric(Integer.parseUnsignedInt(tok.nextToken(), 16));
            re.setRef(Integer.parseUnsignedInt(tok.nextToken(), 16));
            re.setUse(Integer.parseUnsignedInt(tok.nextToken(), 16));
            long flagInt = Long.parseLong(tok.nextToken(), 16);

            if ( ( flagInt & 0x80000000 ) > 0 ) {
                // skip local routes
                return null;
            }

            if ( re.getNetwork().getAddress().getAddress()[ 0 ] == 0xff && re.getNetwork().getPrefixLength() == 8 ) {
                // skip default multicast routes
                return null;
            }

            re.setFlags(RouteFlags.parseFlags(flagInt));
            re.setInterfaceName(tok.nextToken());

            if ( "lo".equals(re.getInterfaceName()) //$NON-NLS-1$
                    && re.getNetwork().getAddress().isUnspecified() && re.getNetwork().getPrefixLength() == 0
                    && re.getFlags().contains(RouteFlags.REJECT) ) {
                // skip lookback reject route
                return null;
            }

            if ( re.getNetwork().getAddress().getAddress()[ 0 ] == 0xfe && re.getNetwork().getAddress().getAddress()[ 1 ] == 0x80
                    && re.getNetwork().getPrefixLength() == 64 ) {
                // set scope for link local addrs
                network.setScopeSpec(re.getInterfaceName());
                re.getFlags().add(RouteFlags.LINKLOCAL);
            }

            if ( ( flagInt & 0x200000 ) > 0 || re.getGateway().isUnspecified() ) {
                re.setGateway(null);
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Found V6 route " + re); //$NON-NLS-1$
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


    /**
     * @param str
     * @return
     */
    private static IPv6Address toNetworkAddress ( String str ) {
        short addr[] = new short[16];
        if ( str.length() != 32 ) {
            throw new IllegalArgumentException("Invalid address"); //$NON-NLS-1$
        }
        for ( int i = 0; i < 16; i++ ) {
            String hex = str.substring(2 * i, 2 * i + 2);
            addr[ i ] = Short.parseShort(hex, 16);
        }
        return (IPv6Address) AbstractIPAddress.fromBytes(addr);
    }
}
