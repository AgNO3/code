/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.network.java.impl.dhcp;


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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import eu.agno3.orchestrator.system.info.network.DHCPAssociationType;
import eu.agno3.orchestrator.system.info.network.DHCPLeaseStatus;
import eu.agno3.orchestrator.system.info.network.DHCPOption;
import eu.agno3.orchestrator.system.info.network.DHCPOptions;
import eu.agno3.orchestrator.system.info.network.java.impl.dhcp.DhclientLeaseLexer.Token;
import eu.agno3.orchestrator.system.info.network.java.impl.dhcp.DhclientLeaseLexer.TokenType;
import eu.agno3.orchestrator.types.net.AbstractIPAddress;
import eu.agno3.orchestrator.types.net.NetworkAddress;
import eu.agno3.orchestrator.types.net.NetworkSpecification;


/**
 * @author mbechler
 *
 */
public class DhclientLeaseParser {

    private static final Logger log = Logger.getLogger(DhclientLeaseParser.class);

    private static final String DEFAULT_LEASE_FILE = "/var/lib/dhcp/dhclient.leases"; //$NON-NLS-1$

    private static final DateTimeFormatter DHCLIENT_DATE_PATTERN = DateTimeFormat.forPattern("y/M/d H:m:s"); //$NON-NLS-1$


    /**
     * 
     * @return leases
     * @throws IOException
     * @throws DhclientLeaseParserException
     */
    public Map<String, DHCPInterfaceStatus> parse () throws IOException, DhclientLeaseParserException {
        return parse(Paths.get(DEFAULT_LEASE_FILE));
    }


    /**
     * 
     * @param p
     * @return leases
     * @throws IOException
     * @throws DhclientLeaseParserException
     */
    public Map<String, DHCPInterfaceStatus> parse ( Path p ) throws IOException, DhclientLeaseParserException {
        try ( FileChannel fc = FileChannel.open(p, StandardOpenOption.READ);
              InputStream is = Channels.newInputStream(fc) ) {
            return parse(is);
        }
    }


    /**
     * 
     * @param is
     * @return leases
     * @throws IOException
     * @throws DhclientLeaseParserException
     */
    public Map<String, DHCPInterfaceStatus> parse ( InputStream is ) throws IOException, DhclientLeaseParserException {
        try ( Reader r = new InputStreamReader(is, StandardCharsets.US_ASCII) ) {
            return parse(r);
        }
    }


    /**
     * 
     * @param r
     * @return found leases
     * @throws IOException
     * @throws DhclientLeaseParserException
     */
    public Map<String, DHCPInterfaceStatus> parse ( Reader r ) throws IOException, DhclientLeaseParserException {
        DhclientLeaseLexer l = new DhclientLeaseLexer(new BufferedReader(r));
        byte[] defaultDuid = null;
        Map<String, DHCPInterfaceStatus> leases = new HashMap<>();
        while ( true ) {
            Token t = l.nextNonComment();
            DHCPInterfaceStatus lease = null;

            if ( t.getType() == TokenType.EOF ) {
                break;
            }
            else if ( t.getType() == TokenType.FAIL ) {
                throw new DhclientLeaseParserException("Failed to parse token: " + t); //$NON-NLS-1$
            }
            else if ( t.getType() == TokenType.KEYWORD && "default-duid".equals(t.getContent()) ) { //$NON-NLS-1$
                defaultDuid = parseDefaultDUID(l);
            }
            else if ( t.getType() == TokenType.KEYWORD && "lease".equals(t.getContent()) ) { //$NON-NLS-1$
                lease = parseLease(l, false);
            }
            else if ( t.getType() == TokenType.KEYWORD && "lease6".equals(t.getContent()) ) { //$NON-NLS-1$
                lease = parseLease(l, true);
            }
            else if ( t.getType() == TokenType.SPACE ) {
                continue;
            }
            else {
                log.warn("Skipping unexpected token " + t); //$NON-NLS-1$
            }

            if ( lease != null && !StringUtils.isBlank(lease.getInterfaceName()) ) {
                leases.put(lease.getInterfaceName(), lease);
            }
            else if ( lease != null ) {
                log.warn("Lease without a interface name"); //$NON-NLS-1$
            }
        }

        if ( log.isDebugEnabled() ) {
            if ( defaultDuid != null ) {
                log.debug("Found default DUID: " + Hex.encodeHexString(defaultDuid)); //$NON-NLS-1$
            }
            log.debug("Found leases for " + leases.keySet()); //$NON-NLS-1$
        }

        return leases;
    }


    /**
     * @param l
     * @return
     * @throws IOException
     * @throws DhclientLeaseParserException
     * 
     */
    private static DHCPInterfaceStatus parseLease ( DhclientLeaseLexer l, boolean v6 ) throws IOException, DhclientLeaseParserException {
        if ( l.nextNonCommentNonWS().getType() != TokenType.BEGIN_BLOCK ) {
            throw new DhclientLeaseParserException("Expected BEGIN_BLOCK"); //$NON-NLS-1$
        }

        DHCPInterfaceStatus lease = new DHCPInterfaceStatus();
        Token t = l.nextNonCommentNonWS();

        DHCPAssociation v4assoc = null;
        DHCPAddressEntry v4addr = null;
        if ( !v6 ) {
            v4assoc = new DHCPAssociation();
            v4assoc.setAssociationType(DHCPAssociationType.V4);
            lease.getAssociations().add(v4assoc);
            v4addr = new DHCPAddressEntry();
            v4assoc.getAddresses().add(v4addr);
        }

        while ( t.getType() == TokenType.KEYWORD ) {
            readStatement(l, v6, t, lease, v4assoc, v4addr);
            t = l.nextNonCommentNonWS();
        }

        if ( t.getType() != TokenType.END_BLOCK ) {
            throw new DhclientLeaseParserException("Unterminated BLOCK"); //$NON-NLS-1$
        }

        return lease;
    }


    /**
     * @param l
     * @param t
     * @throws IOException
     * @throws DhclientLeaseParserException
     */
    private static void readStatement ( DhclientLeaseLexer l, boolean v6, Token t, DHCPInterfaceStatus lease, DHCPAssociation v4assoc,
            DHCPAddressEntry v4addr ) throws IOException, DhclientLeaseParserException {
        l.readNext(TokenType.SPACE);
        switch ( t.getContent() ) {
        case "option": //$NON-NLS-1$
            parseOption(l, lease.getOptions());
            return;
        case "interface": //$NON-NLS-1$
            lease.setInterfaceName(l.readNext(TokenType.STRING).getContent());
            break;
        case "renew": //$NON-NLS-1$
            if ( !v6 ) {
                v4assoc.setRenewTime(readDate(l));
            }
            break;
        case "rebind": //$NON-NLS-1$
            if ( !v6 ) {
                v4assoc.setRebindTime(readDate(l));
            }
            break;
        case "expire": //$NON-NLS-1$
            if ( !v6 ) {
                DateTime exp = readDate(l);
                v4assoc.setExpireTime(exp);
                v4addr.setExpiresTime(exp);
            }
            break;
        case "abandoned": //$NON-NLS-1$
        case "released": //$NON-NLS-1$
            lease.setStatus(DHCPLeaseStatus.RELEASED);
        case "fixed-address": //$NON-NLS-1$
            if ( !v6 ) {
                v4addr.setNetworkAddress(readAddress(l));
            }
            else {
                throw new DhclientLeaseParserException("Found fixed-address in V6 lease"); //$NON-NLS-1$
            }
            break;
        case "bootp": //$NON-NLS-1$
            v4assoc.setAssociationType(DHCPAssociationType.BOOTP);
            break;
        case "filename": //$NON-NLS-1$
        case "server-name": //$NON-NLS-1$
        case "script": //$NON-NLS-1$
        case "name": //$NON-NLS-1$
            l.readNext(TokenType.SPACE);
            String val = l.readNext(TokenType.STRING).getContent();
            lease.getOptions().add(new DHCPOption(t.getContent(), val));
            break;

        case "ia-na": //$NON-NLS-1$
        case "ia-ta": //$NON-NLS-1$
        case "ia-pd": //$NON-NLS-1$
            if ( v6 ) {
                lease.getAssociations().add(readAllocationBlock(l, t.getContent()));
                // this is terminated by END_BLOCK
                return;
            }
        default:
            log.warn(String.format("Skipping unknown statement %s", t.getContent())); //$NON-NLS-1$
            while ( l.nextNonCommentNonWS().getType() != TokenType.END_STMT ) {}
            return;
        }

        Token end = l.nextNonCommentNonWS();
        if ( end.getType() != TokenType.END_STMT ) {
            throw new DhclientLeaseParserException("Missing end statement: " + end); //$NON-NLS-1$
        }
    }


    /**
     * @param l
     * @param options
     * @throws IOException
     * @throws DhclientLeaseParserException
     */
    static void parseOption ( DhclientLeaseLexer l, DHCPOptions options ) throws IOException, DhclientLeaseParserException {
        String optionName = l.readNext(TokenType.KEYWORD).getContent();
        // parse the complete option value for now
        List<String> args = new LinkedList<>();
        Token at;
        while ( ( at = l.nextNonCommentNonWS() ).getType() != TokenType.END_STMT ) {
            if ( at.getType() == TokenType.LIST_SEP ) {
                continue;
            }
            args.add(at.getContent());
        }
        log.debug(String.format("Found option value %s: %s", optionName, args)); //$NON-NLS-1$
        options.add(new DHCPOption(optionName, args));
    }


    /**
     * @param l
     * @param type
     * @return
     * @throws DhclientLeaseParserException
     * @throws IOException
     */
    private static DHCPAssociation readAllocationBlock ( DhclientLeaseLexer l, String type ) throws IOException, DhclientLeaseParserException {

        // (4 byte IAID)
        Token iaidTok = l.readNext(TokenType.KEYWORD, TokenType.STRING);

        if ( log.isDebugEnabled() ) {
            log.debug("IAD is " + iaidTok); //$NON-NLS-1$
        }

        if ( l.nextNonCommentNonWS().getType() != TokenType.BEGIN_BLOCK ) {
            throw new DhclientLeaseParserException("Expecting BEGIN_BLOCK"); //$NON-NLS-1$
        }

        // ia-na = Non-temporary addresses
        // ia-ta = Temporary addresses
        // ia-pd = Prefix delegation

        DHCPAssociation assoc = new DHCPAssociation();
        switch ( type ) {
        case "ia-na": //$NON-NLS-1$
            assoc.setAssociationType(DHCPAssociationType.V6_NA);
            break;
        case "ia-ta": //$NON-NLS-1$
            assoc.setAssociationType(DHCPAssociationType.V6_TA);
            break;
        case "ia-pd": //$NON-NLS-1$
            assoc.setAssociationType(DHCPAssociationType.V6_PD);
            break;
        default:
            throw new DhclientLeaseParserException("Unhandled association type " + type); //$NON-NLS-1$
        }

        Token t = l.nextNonCommentNonWS();

        while ( t.getType() == TokenType.KEYWORD ) {
            readAssociationStatement(l, type, t, assoc);
            t = l.nextNonCommentNonWS();
        }

        if ( t.getType() != TokenType.END_BLOCK ) {
            throw new DhclientLeaseParserException("Unterminated BLOCK " + t); //$NON-NLS-1$
        }

        return assoc;
    }


    /**
     * @param l
     * @param type
     * @param iadTok
     * @param iaidTok
     * @throws DhclientLeaseParserException
     * @throws IOException
     */
    private static void readAssociationStatement ( DhclientLeaseLexer l, String type, Token t, DHCPAssociation assoc )
            throws IOException, DhclientLeaseParserException {

        l.readNext(TokenType.SPACE);
        switch ( t.getContent() ) {
        case "option": //$NON-NLS-1$
            parseOption(l, assoc.getOptions());
            break;
        case "starts": //$NON-NLS-1$
            assoc.setStartTime(new DateTime(readNumber(l) * 1000));
            break;
        case "renew": //$NON-NLS-1$
            if ( !"ia-ta".equals(type) ) { //$NON-NLS-1$
                assoc.setRenewInterval(Duration.standardSeconds(readNumber(l)));
            }
            break;
        case "rebind": //$NON-NLS-1$
            if ( !"ia-ta".equals(type) ) { //$NON-NLS-1$
                assoc.setRebindInterval(Duration.standardSeconds(readNumber(l)));
            }
            break;
        case "iaaddr": //$NON-NLS-1$
        case "iaprefix": //$NON-NLS-1$
            assoc.getAddresses().add(readAddressBlock(l, type));
            return;

        default:
            log.warn(String.format("Skipping unknown statement %s", t.getContent())); //$NON-NLS-1$
            while ( l.nextNonCommentNonWS().getType() != TokenType.END_STMT ) {}
            return;
        }

        Token end = l.nextNonCommentNonWS();
        if ( end.getType() != TokenType.END_STMT ) {
            throw new DhclientLeaseParserException("Missing end statement: " + end); //$NON-NLS-1$
        }
    }


    /**
     * @param l
     * @param content
     * @throws DhclientLeaseParserException
     * @throws IOException
     */
    private static DHCPAddressEntry readAddressBlock ( DhclientLeaseLexer l, String type ) throws IOException, DhclientLeaseParserException {
        Token addrOrPrefixTok = l.readNext(TokenType.KEYWORD);

        if ( log.isDebugEnabled() ) {
            log.debug("Prefix is " + addrOrPrefixTok); //$NON-NLS-1$
        }

        if ( l.nextNonCommentNonWS().getType() != TokenType.BEGIN_BLOCK ) {
            throw new DhclientLeaseParserException("Expecting BEGIN_BLOCK"); //$NON-NLS-1$
        }

        DHCPAddressEntry addr = new DHCPAddressEntry();
        try {
            if ( "ia-pd".equals(type) ) { //$NON-NLS-1$
                NetworkSpecification net = NetworkSpecification.fromString(addrOrPrefixTok.getContent(), true);
                addr.setNetworkAddress(net.getAddress());
                addr.setPrefixLength(net.getPrefixLength());
            }
            else {
                addr.setNetworkAddress(AbstractIPAddress.parse(addrOrPrefixTok.getContent()));
            }
        }
        catch ( IllegalArgumentException e ) {
            throw new DhclientLeaseParserException("Failed to parse address", e); //$NON-NLS-1$
        }

        Token t = l.nextNonCommentNonWS();

        while ( t.getType() == TokenType.KEYWORD ) {
            readAddressStatement(l, type, t, addr);
            t = l.nextNonCommentNonWS();
        }

        if ( t.getType() != TokenType.END_BLOCK ) {
            throw new DhclientLeaseParserException("Unterminated BLOCK " + t); //$NON-NLS-1$
        }

        return addr;
    }


    /**
     * @param l
     * @param type
     * @param t
     * @throws IOException
     * @throws DhclientLeaseParserException
     */
    private static void readAddressStatement ( DhclientLeaseLexer l, String type, Token t, DHCPAddressEntry addr )
            throws IOException, DhclientLeaseParserException {
        l.readNext(TokenType.SPACE);
        switch ( t.getContent() ) {
        case "option": //$NON-NLS-1$
            parseOption(l, addr.getOptions());
            break;
        case "starts": //$NON-NLS-1$
            addr.setStartTime(new DateTime(readNumber(l) * 1000));
            break;
        case "preferred-life": //$NON-NLS-1$
            addr.setPreferredLife(Duration.standardSeconds(readNumber(l)));
            break;
        case "max-life": //$NON-NLS-1$
            addr.setMaxLife(Duration.standardSeconds(readNumber(l)));
            break;
        default:
            log.warn(String.format("Skipping unknown statement %s", t.getContent())); //$NON-NLS-1$
            while ( l.nextNonCommentNonWS().getType() != TokenType.END_STMT ) {}
            return;
        }

        Token end = l.nextNonCommentNonWS();
        if ( end.getType() != TokenType.END_STMT ) {
            throw new DhclientLeaseParserException("Missing end statement: " + end); //$NON-NLS-1$
        }
    }


    private static long readNumber ( DhclientLeaseLexer l ) throws IOException, DhclientLeaseParserException {
        try {
            return Long.parseLong(l.readNext(TokenType.KEYWORD).getContent());
        }
        catch ( IllegalArgumentException e ) {
            throw new DhclientLeaseParserException("Failed to parse number", e); //$NON-NLS-1$
        }
    }


    /**
     * @param l
     * @return
     * @throws DhclientLeaseParserException
     * @throws IOException
     */
    private static NetworkAddress readAddress ( DhclientLeaseLexer l ) throws IOException, DhclientLeaseParserException {
        String addrStr = l.readNext(TokenType.KEYWORD).getContent();
        try {
            return AbstractIPAddress.parse(addrStr);
        }
        catch ( IllegalArgumentException e ) {
            throw new DhclientLeaseParserException("Failed to parse address", e); //$NON-NLS-1$
        }
    }


    /**
     * @param l
     * @return
     * @throws DhclientLeaseParserException
     * @throws IOException
     */
    private static DateTime readDate ( DhclientLeaseLexer l ) throws IOException, DhclientLeaseParserException {

        Token t = l.readNext(TokenType.KEYWORD);
        if ( "never".equals(t.getContent()) ) { //$NON-NLS-1$
            return null;
        }
        else if ( "epoch".equals(t.getContent()) ) { //$NON-NLS-1$
            // db-time-format local
            l.readNext(TokenType.SPACE);
            t = l.readNext(TokenType.KEYWORD);

            try {
                return new DateTime(Long.parseLong(t.getContent()));
            }
            catch ( IllegalArgumentException e ) {
                throw new DhclientLeaseParserException("Invalid epoch date specification", e); //$NON-NLS-1$
            }
        }
        else {
            String weekday = t.getContent();
            l.readNext(TokenType.SPACE);
            String date = l.readNext(TokenType.KEYWORD).getContent();
            l.readNext(TokenType.SPACE);
            String time = l.readNext(TokenType.KEYWORD).getContent();

            try {
                int wkd = 1 + Integer.parseInt(weekday);
                String combined = String.format("%s %s", date, time); //$NON-NLS-1$
                if ( log.isDebugEnabled() ) {
                    log.debug("Parsing " + combined); //$NON-NLS-1$
                }
                LocalDateTime localDateTime = LocalDateTime.parse(combined, DHCLIENT_DATE_PATTERN); // $NON-NLS-1$
                if ( log.isDebugEnabled() ) {
                    log.debug("Result (local zone) " + localDateTime); //$NON-NLS-1$
                }
                if ( localDateTime.getDayOfWeek() == wkd ) {
                    throw new IllegalArgumentException("Weekday does not match expected value"); //$NON-NLS-1$
                }
                return localDateTime.toDateTime();
            }
            catch ( IllegalArgumentException e ) {
                throw new DhclientLeaseParserException("Failed to parse date specification", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param l
     * @return
     * @throws DhclientLeaseParserException
     * @throws IOException
     * 
     */
    private static byte[] parseDefaultDUID ( DhclientLeaseLexer l ) throws IOException, DhclientLeaseParserException {
        l.readNext(TokenType.SPACE);
        String defaultDuid = l.readNext(TokenType.STRING).getContent();
        l.readNext(TokenType.END_STMT);
        return defaultDuid.getBytes(StandardCharsets.ISO_8859_1);
    }
}
