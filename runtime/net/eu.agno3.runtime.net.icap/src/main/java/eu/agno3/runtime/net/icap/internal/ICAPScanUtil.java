/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.06.2015 by mbechler
 */
package eu.agno3.runtime.net.icap.internal;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.net.icap.ICAPConnection;
import eu.agno3.runtime.net.icap.ICAPException;
import eu.agno3.runtime.net.icap.ICAPResponse;
import eu.agno3.runtime.net.icap.ICAPScannerException;


/**
 * @author mbechler
 *
 */
public class ICAPScanUtil {

    /**
     * 
     */
    private static final String UNKNOWN_SIG = "unknown"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(ICAPScanUtil.class);

    private static final Charset ASCII = Charset.forName("US-ASCII"); //$NON-NLS-1$

    private static final List<String> VIRUS_HEADERS = Arrays.asList("X-Virus-Name", //$NON-NLS-1$
        "X-Virus-ID", //$NON-NLS-1$
        "X-Violations-Found"); //$NON-NLS-1$


    /**
     * 
     * @param conn
     * @param data
     * @param size
     * @param filename
     * @param contentType
     * @param clientIp
     * @param clientUser
     * @throws ICAPScannerException
     * @throws ICAPException
     */
    public static void scan ( ICAPConnection conn, InputStream data, long size, String filename, String contentType, String clientIp,
            String clientUser ) throws ICAPScannerException, ICAPException {
        ByteArrayOutputStream resHeader;
        ByteArrayOutputStream reqHeader;
        Map<String, List<String>> icapHeaders = new HashMap<>();

        try {
            reqHeader = makeRequestHeader(filename);
            resHeader = makeResponseHeader(size, contentType);

            ICAPIOUtils.addHeader(icapHeaders, "X-Client-IP", clientIp != null ? clientIp : StringUtils.EMPTY); //$NON-NLS-1$
            ICAPIOUtils.addHeader(icapHeaders, "X-Authenticated-User", clientUser != null ? clientUser : StringUtils.EMPTY); //$NON-NLS-1$
        }
        catch ( IOException e ) {
            throw new ICAPException("Failed to build request", e); //$NON-NLS-1$
        }

        try ( ICAPResponse respmod = conn.respmod(
            data,
            icapHeaders,
            reqHeader != null ? reqHeader.toByteArray() : null,
            resHeader.toByteArray(),
            true) ) {

            if ( respmod.getStatusCode() == 204 ) {
                // no response modification, could not be blocked
                log.debug("Server returned 204"); //$NON-NLS-1$
                return;
            }

            checkICAPHeaders(respmod);
            log.debug("No AV header found, checking for encapuslated HTTP status"); //$NON-NLS-1$
            checkBody(respmod);
        }
        catch (
            ICAPException |
            IOException e ) {
            throw new ICAPException("Scanner internal error", e); //$NON-NLS-1$
        }
    }


    /**
     * @param respmod
     * @throws ICAPScannerException
     */
    private static void checkICAPHeaders ( ICAPResponse respmod ) throws ICAPScannerException {
        for ( String avHeader : VIRUS_HEADERS ) {
            List<String> avHeaderVal = respmod.getResponseHeaders().get(avHeader.toLowerCase());
            if ( avHeaderVal != null && !avHeaderVal.isEmpty() ) {
                String fullHeader = StringUtils.join(avHeaderVal, ", "); //$NON-NLS-1$
                log.debug(String.format("Found AV match header %s: %s", avHeader, fullHeader)); //$NON-NLS-1$
                throw new ICAPScannerException(fullHeader, "Scanner returned match: " + fullHeader); //$NON-NLS-1$
            }
        }

        if ( log.isDebugEnabled() ) {
            for ( Entry<String, List<String>> header : respmod.getResponseHeaders().entrySet() ) {
                log.debug(String.format("Header %s: %s", //$NON-NLS-1$
                    header.getKey(),
                    StringUtils.join(header.getValue(), ", "))); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param respmod
     * @throws ICAPException
     * @throws ICAPScannerException
     */
    private static void checkBody ( ICAPResponse respmod ) throws ICAPException, ICAPScannerException {
        String encapResHeader = respmod.getHeaderParts().get("res-hdr"); //$NON-NLS-1$
        if ( StringUtils.isBlank(encapResHeader) ) {
            log.debug("No encapsulated response header found"); //$NON-NLS-1$
            return;
        }

        int endOfStatus = encapResHeader.indexOf("\r\n"); //$NON-NLS-1$
        if ( endOfStatus < 0 ) {
            throw new ICAPException("No encapsulated response status found"); //$NON-NLS-1$
        }

        String statusLine = encapResHeader.substring(0, endOfStatus);
        String[] parts = StringUtils.split(statusLine, StringUtils.SPACE, 3);

        if ( parts == null || parts.length < 2 ) {
            throw new ICAPException("Invalid response status line " + statusLine); //$NON-NLS-1$
        }

        try {
            String proto = parts[ 0 ];
            int code = Integer.parseInt(parts[ 1 ]);
            String msg = parts.length == 3 ? parts[ 2 ] : StringUtils.EMPTY;

            if ( !proto.toUpperCase().startsWith("HTTP/") ) { //$NON-NLS-1$
                throw new ICAPException("Encapsulated response is not HTTP: " + statusLine); //$NON-NLS-1$
            }

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Encapsulated response status %d: %s", code, msg)); //$NON-NLS-1$
            }

            if ( code >= 400 ) {
                throw new ICAPScannerException(UNKNOWN_SIG, "Response modified to non-success status"); //$NON-NLS-1$
            }
        }
        catch ( NumberFormatException e ) {
            throw new ICAPException("Invalid response status code " + statusLine, e); //$NON-NLS-1$
        }
    }


    /**
     * @param filename
     * @param resHeader
     * @param reqHeader
     * @return
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    private static ByteArrayOutputStream makeRequestHeader ( String filename ) throws IOException, UnsupportedEncodingException {
        ByteArrayOutputStream reqHeader = new ByteArrayOutputStream();
        // build request header

        String realFileName;
        if ( StringUtils.isBlank(filename) ) {
            realFileName = "/dummy"; //$NON-NLS-1$
        }
        else {
            realFileName = "/" + filename; //$NON-NLS-1$
        }

        reqHeader.write("GET ".getBytes(ASCII)); //$NON-NLS-1$
        reqHeader.write(URLEncoder.encode(realFileName, "ASCII").getBytes(ASCII)); //$NON-NLS-1$
        reqHeader.write(" HTTP/1.1".getBytes(ASCII)); //$NON-NLS-1$
        ICAPIOUtils.crlf(reqHeader);
        ICAPIOUtils.crlf(reqHeader);

        if ( log.isDebugEnabled() ) {
            log.debug("Sending request header " + new String(reqHeader.toByteArray(), ASCII)); //$NON-NLS-1$
        }

        return reqHeader;
    }


    /**
     * @param size
     * @param contentType
     * @param resHeader
     * @return
     * @throws IOException
     */
    private static ByteArrayOutputStream makeResponseHeader ( long size, String contentType ) throws IOException {
        // response status line
        ByteArrayOutputStream resHeader = new ByteArrayOutputStream();
        resHeader.write("HTTP/1.1 200 OK".getBytes(ASCII)); //$NON-NLS-1$
        ICAPIOUtils.crlf(resHeader);

        Map<String, List<String>> headers = new HashMap<>();
        // add response headers
        if ( !StringUtils.isBlank(contentType) ) {
            ICAPIOUtils.addHeader(headers, "Content-Type", contentType); //$NON-NLS-1$
        }
        if ( size >= 0 ) {
            ICAPIOUtils.addHeader(headers, "Content-Length", String.valueOf(size)); //$NON-NLS-1$
        }

        ICAPIOUtils.writeHeaders(resHeader, headers);
        ICAPIOUtils.crlf(resHeader);

        if ( log.isDebugEnabled() ) {
            log.debug("Sending response header " + new String(resHeader.toByteArray(), ASCII)); //$NON-NLS-1$
        }
        return resHeader;
    }
}
