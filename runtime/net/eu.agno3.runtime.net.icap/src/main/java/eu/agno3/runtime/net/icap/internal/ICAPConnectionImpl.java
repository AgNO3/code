/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.05.2015 by mbechler
 */
package eu.agno3.runtime.net.icap.internal;


import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.io.input.ClosedInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.net.icap.ICAPConfiguration;
import eu.agno3.runtime.net.icap.ICAPConnection;
import eu.agno3.runtime.net.icap.ICAPException;
import eu.agno3.runtime.net.icap.ICAPOptions;
import eu.agno3.runtime.net.icap.ICAPResponse;
import eu.agno3.runtime.net.icap.ICAPScanRequest;
import eu.agno3.runtime.net.icap.ICAPScannerException;


/**
 * @author mbechler
 *
 */
public class ICAPConnectionImpl implements ICAPConnection {

    private static final String CONNECTION_UPGRADE = "Upgrade"; //$NON-NLS-1$
    private static final String CONNECTION = "Connection"; //$NON-NLS-1$
    private static final String TLS_1_0_UPGRADE = "TLS/1.0"; //$NON-NLS-1$
    private static final String UPGRADE = CONNECTION_UPGRADE;
    private static final String CLIENT_NAME = "AgNO3 ICAP Client"; //$NON-NLS-1$
    private static final String CLIENT_VERSION = "1.0"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(ICAPConnectionImpl.class);

    private static final String ICAP_PROTO = "icap"; //$NON-NLS-1$
    private static final String ICAPS_PROTO = "icaps"; //$NON-NLS-1$

    private static final Set<String> ENCAPSULATED_TYPES = new HashSet<>(Arrays.asList(
        "req-hdr", //$NON-NLS-1$
        "res-hdr", //$NON-NLS-1$
        "req-body", //$NON-NLS-1$
        "res-body", //$NON-NLS-1$
        "opt-body", //$NON-NLS-1$
        "null-body")); //$NON-NLS-1$

    /**
     * 
     */
    private static final String ENCAPSULATED = "encapsulated"; //$NON-NLS-1$

    private SocketFactory socketFactory;
    private String host;
    private TLSContext tlsContext;

    private boolean connected;
    private int port;
    private URI serverUri;

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ICAPConfiguration cfg;
    private int chunkSize = 4096;

    private AtomicInteger openResponses = new AtomicInteger(0);

    private ICAPOptions options;
    private boolean ssl;
    private boolean tlsStarted;


    /**
     * @return the inputStream
     */
    public InputStream getInputStream () {
        return this.inputStream;
    }


    /**
     * @param inputStream
     *            the inputStream to set
     */
    public void setInputStream ( InputStream inputStream ) {
        this.inputStream = inputStream;
    }


    /**
     * @return the outputStream
     */
    public OutputStream getOutputStream () {
        return this.outputStream;
    }


    /**
     * @param outputStream
     *            the outputStream to set
     */
    public void setOutputStream ( OutputStream outputStream ) {
        this.outputStream = outputStream;
    }


    /**
     * 
     * @param cfg
     * @param tc
     * @throws CryptoException
     * @throws URISyntaxException
     */
    public ICAPConnectionImpl ( ICAPConfiguration cfg, TLSContext tc ) throws CryptoException, URISyntaxException {
        this.cfg = cfg;
        this.tlsContext = tc;
        int defaultPort = 1344;
        URI uri = cfg.selectServerUri();
        if ( ICAPS_PROTO.equals(uri.getScheme()) ) {
            this.socketFactory = tc.getSocketFactory();
            this.ssl = true;
            defaultPort = 11344;
        }
        else if ( !ICAP_PROTO.equals(uri.getScheme()) ) {
            throw new IllegalArgumentException("Only icap/icaps protocols are supported"); //$NON-NLS-1$
        }
        else {
            this.socketFactory = SocketFactory.getDefault();
        }

        this.host = uri.getHost();

        if ( uri.getPort() == -1 ) {
            this.port = defaultPort;
        }
        else {
            this.port = uri.getPort();
        }

        if ( this.cfg.getOverrideRequestURI() != null ) {
            this.serverUri = this.cfg.getOverrideRequestURI();
        }
        else if ( this.ssl && !this.cfg.isSendICAPSInRequest() ) {
            this.serverUri = new URI(ICAP_PROTO, uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), null);
        }
        else {
            this.serverUri = uri;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConnection#check()
     */
    @Override
    public boolean check () {
        return this.connected;
    }


    /**
     * 
     * @throws UnknownHostException
     * @throws IOException
     * @throws ICAPException
     */
    @Override
    public synchronized void ensureConnected () throws IOException, ICAPException {

        if ( this.connected ) {
            return;
        }

        log.debug("Connecting to " + this.serverUri); //$NON-NLS-1$
        if ( this.inputStream == null || this.outputStream == null ) {
            this.socket = this.socketFactory.createSocket(this.host, this.port);

            setupSocket(this.socket);

        }
        this.connected = true;
        log.debug("Connected"); //$NON-NLS-1$

        if ( !this.ssl && this.cfg.isTryStartTLS() ) {
            try ( ICAPResponse res = this.options(true) ) {
                // ignore, options are handeled internally
            }

            if ( this.cfg.isRequireStartTLS() && !this.tlsStarted ) {
                throw new ICAPException("TLS is required but could not be established"); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ICAPException
     * @throws ICAPScannerException
     *
     * @see eu.agno3.runtime.net.icap.ICAPConnection#scan(eu.agno3.runtime.net.icap.ICAPScanRequest)
     */
    @Override
    public void scan ( ICAPScanRequest req ) throws ICAPScannerException, ICAPException {
        ICAPScanUtil.scan(this, req.getData(), req.getSize(), req.getFileName(), req.getContentType(), req.getClientIp(), req.getClientUser());
    }


    /**
     * @throws IOException
     */
    private void setupSocket ( Socket s ) throws IOException {
        if ( s instanceof SSLSocket ) {
            SSLSocket sslSock = (SSLSocket) s;
            sslSock.getSSLParameters().setEndpointIdentificationAlgorithm("HTTPS"); //$NON-NLS-1$
        }

        s.setKeepAlive(true);
        s.setSoTimeout(this.cfg.getSocketTimeout());

        this.inputStream = s.getInputStream();
        this.outputStream = new BufferedOutputStream(s.getOutputStream());
    }


    private synchronized void doStartTLSUpgrade () throws IOException, ICAPException {
        try {
            SSLSocketFactory sslSockFactory = this.tlsContext.getSocketFactory();
            SSLSocket newSocket = (SSLSocket) sslSockFactory.createSocket(this.socket, this.host, this.port, true);
            setupSocket(newSocket);
            this.socket = newSocket;
            this.tlsStarted = true;
        }
        catch ( CryptoException e ) {
            error(e);
            throw new ICAPException("TLS setup failed", e); //$NON-NLS-1$
        }
        catch ( IOException e ) {
            error(e);
            throw e;
        }
    }


    /**
     * @throws ICAPException
     * @throws IOException
     * 
     */
    private void ensureOptionsLoaded () throws IOException, ICAPException {
        if ( this.options == null || this.options.isExpired() ) {
            try ( ICAPResponse res = this.options(false) ) {
                // ignore, options are handeled internally
            }
        }
    }


    /**
     * 
     * @param icapResponse
     * @throws IOException
     * @throws ICAPException
     */
    public void releaseResponse ( ICAPResponse icapResponse ) throws IOException, ICAPException {

        if ( this.openResponses.decrementAndGet() < 0 ) {
            throw new ICAPException("Closed non open result"); //$NON-NLS-1$
        }

        List<String> connHeader = icapResponse.getResponseHeaders().get("connection"); //$NON-NLS-1$
        if ( connHeader != null && !connHeader.isEmpty() ) {
            if ( "close".equalsIgnoreCase(connHeader.get(0)) ) { //$NON-NLS-1$
                log.debug("Server sent connection close header, closing now"); //$NON-NLS-1$
                close();
            }
        }
    }


    @Override
    public synchronized void close () throws IOException {

        if ( this.openResponses.get() != 0 ) {
            log.warn("There are unclosed responses"); //$NON-NLS-1$
        }

        log.debug("Closing connection"); //$NON-NLS-1$
        if ( !this.connected || this.socket == null ) {
            return;
        }

        if ( this.outputStream != null ) {
            this.outputStream.close();
            this.outputStream = null;
        }

        if ( this.inputStream != null ) {
            this.inputStream.close();
            this.inputStream = null;
        }

        this.connected = false;
        this.socket.close();
        this.socket = null;
    }


    /**
     * @param e
     */
    public void error ( Exception e ) {
        log.warn("ICAP Connection error", e); //$NON-NLS-1$
        this.connected = false;
        if ( this.socket != null && !this.socket.isClosed() ) {
            if ( this.outputStream != null ) {
                try {
                    this.outputStream.close();
                }
                catch ( IOException ex ) {
                    log.warn("Failed to close output stream", ex); //$NON-NLS-1$
                }
                this.outputStream = null;
            }

            if ( this.inputStream != null ) {
                try {
                    this.inputStream.close();
                }
                catch ( IOException ex ) {
                    log.warn("Failed to close input stream", ex); //$NON-NLS-1$
                }
                this.inputStream = null;
            }

            try {
                this.socket.close();
            }
            catch ( IOException ex ) {
                log.warn("Failed to close socket", ex); //$NON-NLS-1$
            }
        }
        this.socket = null;
    }


    @Override
    public synchronized ICAPResponse respmod ( InputStream is, Map<String, List<String>> icapHeaders, byte[] reqHeader, byte[] resHeader,
            boolean preview ) throws IOException, ICAPException {

        List<ICAPSegment> segments = new ArrayList<>();
        Map<String, byte[]> headerParts = new HashMap<>();

        if ( reqHeader != null ) {
            segments.add(new ICAPSegment("req-hdr", -1, reqHeader.length)); //$NON-NLS-1$
            headerParts.put("req-hdr", reqHeader); //$NON-NLS-1$
        }

        if ( resHeader != null ) {
            segments.add(new ICAPSegment("res-hdr", -1, resHeader.length)); //$NON-NLS-1$
            headerParts.put("res-hdr", resHeader); //$NON-NLS-1$
        }

        ensureConnected();
        ICAPOptions opts = this.getOptions();
        if ( !opts.isAllowRespmod() ) {
            throw new ICAPException("Server does not support RESPMOD method"); //$NON-NLS-1$
        }

        return request(is, segments, icapHeaders, headerParts, opts, preview, "RESPMOD"); //$NON-NLS-1$
    }


    @Override
    public synchronized ICAPResponse reqmod ( InputStream reqBody, Map<String, List<String>> icapHeaders, byte[] reqHeader, boolean preview )
            throws IOException, ICAPException {
        List<ICAPSegment> segments = new ArrayList<>();
        Map<String, byte[]> headerParts = new HashMap<>();

        if ( reqHeader != null ) {
            segments.add(new ICAPSegment("req-hdr", -1, reqHeader.length)); //$NON-NLS-1$
            headerParts.put("req-hdr", reqHeader); //$NON-NLS-1$
        }

        ensureConnected();
        ICAPOptions opts = this.getOptions();
        if ( !opts.isAllowReqmod() ) {
            throw new ICAPException("Server does not support REQMOD method"); //$NON-NLS-1$
        }

        return request(reqBody, segments, icapHeaders, headerParts, opts, preview, "REQMOD"); //$NON-NLS-1$
    }


    /**
     * @param bodyStream
     * @param segments
     * @param headerParts
     * @param input
     * @param opts
     * @param previewSize
     * @param method
     * @return
     * @throws Exception
     */
    private ICAPResponse request ( InputStream bodyStream, List<ICAPSegment> segments, Map<String, List<String>> icapHeaders,
            Map<String, byte[]> headerParts, ICAPOptions opts, boolean preview, String method ) throws IOException, ICAPException {
        InputStream input;
        if ( bodyStream == null ) {
            segments.add(new ICAPSegment("null-body", -1, -1)); //$NON-NLS-1$
            input = ClosedInputStream.CLOSED_INPUT_STREAM;
        }
        else {
            segments.add(new ICAPSegment("res-body", -1, -1)); //$NON-NLS-1$
            input = bodyStream;
        }

        int previewSize = preview ? opts.getPreviewSize() : -1;

        try {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Sending %s request", method)); //$NON-NLS-1$
            }
            ICAPIOUtils.writeRequestLine(this.outputStream, this.serverUri, method);
            Map<String, List<String>> reqHeaders = getDefaultHeaders();

            if ( previewSize >= 0 ) {
                ICAPIOUtils.addHeader(reqHeaders, "Preview", String.valueOf(previewSize)); //$NON-NLS-1$
            }

            addEncapsulationHeader(reqHeaders, segments);
            ICAPIOUtils.writeHeaders(this.outputStream, reqHeaders);
            if ( icapHeaders != null ) {
                ICAPIOUtils.writeHeaders(this.outputStream, icapHeaders);
            }
            ICAPIOUtils.crlf(this.outputStream);

            if ( log.isTraceEnabled() ) {
                log.trace("Sending bodies with preview size " + previewSize); //$NON-NLS-1$
            }
            sendBodies(segments, previewSize, headerParts, input);

            this.outputStream.flush();

            int status;
            try {
                status = ICAPIOUtils.readStatusLine(this.inputStream);
            }
            catch ( ICAPException e ) {
                log.warn(String.format("Failure reading %s response", method), e); //$NON-NLS-1$
                ICAPIOUtils.readHeaders(this.inputStream);
                throw e;
            }

            if ( status == 100 ) {
                if ( previewSize < 0 || bodyStream == null ) {
                    throw new ICAPException("Server sent 100 when we did not send a preview or there is no body"); //$NON-NLS-1$
                }
                ICAPIOUtils.readHeaders(this.inputStream);
                log.debug("Sending remaining body"); //$NON-NLS-1$
                ICAPIOUtils.sendChunked(this.outputStream, this.chunkSize, input, false, Collections.EMPTY_MAP);
                this.outputStream.flush();
                try {
                    status = ICAPIOUtils.readStatusLine(this.inputStream);
                }
                catch ( ICAPException e ) {
                    log.warn(String.format("Failure reading %s response", method), e); //$NON-NLS-1$
                    ICAPIOUtils.readHeaders(this.inputStream);
                    throw e;
                }
            }

            Map<String, List<String>> icapResponseHeaders = ICAPIOUtils.readHeaders(this.inputStream);
            if ( !icapResponseHeaders.containsKey(ENCAPSULATED) ) {
                // insert null-body for servers that do not send it
                addEncapsulationHeader(icapResponseHeaders, Collections.EMPTY_LIST);
            }

            if ( status == 204 ) {
                if ( !opts.isAllow204() ) {
                    throw new ICAPException("Server sent a 204 but did not announce it"); //$NON-NLS-1$
                }

                input = ClosedInputStream.CLOSED_INPUT_STREAM;
                Map<String, String> stringHeaderParts = new HashMap<>();
                for ( Entry<String, byte[]> header : headerParts.entrySet() ) {
                    stringHeaderParts.put(header.getKey(), new String(header.getValue(), Charset.forName("US-ASCII"))); //$NON-NLS-1$
                }
                return new ICAP204Response(icapResponseHeaders, bodyStream, stringHeaderParts);
            }

            ICAPResponse resp = readBodies(status, icapResponseHeaders, icapResponseHeaders.get(ENCAPSULATED));
            this.openResponses.incrementAndGet();
            return resp;
        }
        catch (
            IOException |
            ICAPException e ) {
            error(e);
            throw e;
        }
    }


    /**
     * @return the options
     * @throws ICAPException
     * @throws IOException
     */
    @Override
    public ICAPOptions getOptions () throws IOException, ICAPException {
        ensureOptionsLoaded();
        return this.options;
    }


    private ICAPResponse options ( boolean onConnect ) throws IOException, ICAPException {
        if ( !onConnect ) {
            ensureConnected();
        }
        try {
            log.debug("Sending OPTIONS request"); //$NON-NLS-1$
            ICAPIOUtils.writeRequestLine(this.outputStream, this.serverUri, "OPTIONS"); //$NON-NLS-1$
            Map<String, List<String>> reqHeaders = getDefaultHeaders();
            if ( !this.tlsStarted && this.cfg.isTryStartTLS() ) {
                log.debug("Requesting TLS upgrade"); //$NON-NLS-1$
                ICAPIOUtils.addHeader(reqHeaders, UPGRADE, TLS_1_0_UPGRADE);
                ICAPIOUtils.addHeader(reqHeaders, CONNECTION, CONNECTION_UPGRADE);
            }
            addEncapsulationHeader(reqHeaders, Collections.EMPTY_LIST);
            ICAPIOUtils.writeHeaders(this.outputStream, reqHeaders);
            ICAPIOUtils.crlf(this.outputStream);
            this.outputStream.flush();

            int status;
            try {
                status = ICAPIOUtils.readStatusLine(this.inputStream);

                if ( status != 200 && ! ( status == 101 && this.cfg.isTryStartTLS() && !this.tlsStarted ) ) {
                    throw new ICAPException("Status returned non 200 response"); //$NON-NLS-1$
                }
            }
            catch ( ICAPException e ) {
                log.warn("Failure reading OPTIONS response", e); //$NON-NLS-1$
                ICAPIOUtils.readHeaders(this.inputStream);
                throw e;
            }

            Map<String, List<String>> optionHeaders = ICAPIOUtils.readHeaders(this.inputStream);

            if ( !optionHeaders.containsKey(ENCAPSULATED) ) {
                // insert null-body for servers that do not send it
                addEncapsulationHeader(optionHeaders, Collections.EMPTY_LIST);
            }

            if ( status == 101 ) {
                return handleConnectionUpgrade(status, optionHeaders);
            }

            ICAPResponse resp = readBodies(status, optionHeaders, optionHeaders.get(ENCAPSULATED));
            this.openResponses.incrementAndGet();
            this.options = ICAPOptionsImpl.fromResponse(resp);
            return resp;
        }
        catch (
            IOException |
            ICAPException e ) {
            error(e);
            throw e;
        }
    }


    /**
     * @param status
     * @param optionHeaders
     * @return
     * @throws IOException
     * @throws ICAPException
     */
    private ICAPResponse handleConnectionUpgrade ( int status, Map<String, List<String>> optionHeaders ) throws IOException, ICAPException {
        log.debug("Server sent 101"); //$NON-NLS-1$
        try ( ICAPResponse resp = readBodies(status, optionHeaders, optionHeaders.get(ENCAPSULATED)) ) {
            // ignore, really also should not be needed as 101s have no bodies
        }
        List<String> upgradeHeaders = optionHeaders.get(UPGRADE);
        String all = StringUtils.join(upgradeHeaders, ", "); //$NON-NLS-1$
        boolean foundTLS = false;
        for ( String upgradeProto : StringUtils.split(all, ',') ) {
            upgradeProto = upgradeProto.trim();
            if ( TLS_1_0_UPGRADE.equalsIgnoreCase(upgradeProto) ) {
                foundTLS = true;
                break;
            }
        }

        if ( !foundTLS ) {
            throw new ICAPException("Server responded with 101 but does not allow TLS upgrade"); //$NON-NLS-1$
        }

        try {
            doStartTLSUpgrade();
            return options(true);
        }
        catch (
            IOException |
            ICAPException e ) {
            this.tlsStarted = false;
            throw e;
        }
    }


    /**
     * @param headers
     * @param emptyList
     * @throws IOException
     */
    private static void addEncapsulationHeader ( Map<String, List<String>> headers, List<ICAPSegment> segments ) throws IOException {
        List<String> segmentDefs = new LinkedList<>();
        boolean foundBody = false;
        long at = 0;

        for ( ICAPSegment seg : segments ) {
            segmentDefs.add(String.format("%s=%d", seg.getType(), at)); //$NON-NLS-1$
            if ( seg.getType().endsWith("-body") ) { //$NON-NLS-1$
                foundBody = true;
            }
            at += seg.getLength();
        }

        if ( !foundBody ) {
            segmentDefs.add("null-body=" + at); //$NON-NLS-1$
        }

        ICAPIOUtils.addHeader(headers, ENCAPSULATED, StringUtils.join(segmentDefs, ", ")); //$NON-NLS-1$
    }


    private void sendBodies ( List<ICAPSegment> segments, int previewSize, Map<String, byte[]> headerParts, InputStream bodyInputStream )
            throws ICAPException, IOException {

        for ( int i = 0; i < segments.size(); i++ ) {
            ICAPSegment seg = segments.get(i);
            ICAPSegment next = null;
            if ( i + 1 < segments.size() ) {
                next = segments.get(i + 1);
            }

            if ( log.isTraceEnabled() ) {
                log.trace("sending body " + seg.getType()); //$NON-NLS-1$
            }

            if ( "null-body".equals(seg.getType()) ) { //$NON-NLS-1$
                return;
            }
            else if ( seg.getType().endsWith("-body") ) { //$NON-NLS-1$
                if ( next != null ) {
                    throw new ICAPException("Body must be last segment"); //$NON-NLS-1$
                }

                Map<String, List<String>> trailers = Collections.EMPTY_MAP;
                if ( previewSize >= 0 ) {
                    byte[] buffer = new byte[previewSize];
                    int read = 0;
                    int res = -1;
                    boolean eof = false;
                    while ( ( previewSize - read ) > 0 && ( res = bodyInputStream.read(buffer, read, previewSize - read) ) >= 0 ) {
                        log.trace(res);
                        if ( res >= 0 ) {
                            read += res;
                        }
                        else {
                            eof = true;
                        }
                    }

                    if ( log.isTraceEnabled() ) {
                        log.trace("Sending preview with size " + read); //$NON-NLS-1$
                    }
                    ICAPIOUtils.sendChunked(this.outputStream, this.chunkSize, new ByteArrayInputStream(buffer, 0, read), eof, trailers);
                }
                else {
                    log.trace("Sending full body"); //$NON-NLS-1$
                    ICAPIOUtils.sendChunked(this.outputStream, this.chunkSize, bodyInputStream, false, trailers);
                }
                return;
            }
            else if ( next == null ) {
                throw new ICAPException("Body must be last segment"); //$NON-NLS-1$
            }
            else if ( seg.getType().endsWith("-hdr") ) { //$NON-NLS-1$
                byte[] headerData = headerParts.get(seg.getType());
                if ( headerData == null ) {
                    throw new ICAPException("Missing or illegal header data"); //$NON-NLS-1$
                }

                if ( headerData.length < 4 || headerData[ headerData.length - 4 ] != 13 || headerData[ headerData.length - 3 ] != 10
                        || headerData[ headerData.length - 2 ] != 13 || headerData[ headerData.length - 1 ] != 10 ) {
                    throw new ICAPException("Header termination missing"); //$NON-NLS-1$
                }

                if ( headerData.length != seg.getLength() ) {
                    throw new ICAPException("Illegal size specification"); //$NON-NLS-1$
                }

                ICAPIOUtils.sendRaw(this.outputStream, new ByteArrayInputStream(headerData));
            }
        }
    }


    /**
     * @param headers
     * @param list
     * @return
     * @throws ICAPException
     * @throws IOException
     */
    private ICAPResponse readBodies ( int statusCode, Map<String, List<String>> headers, List<String> list ) throws ICAPException, IOException {
        List<ICAPSegment> segments = parseEncapsulationHeader(list);

        Map<String, String> headerParts = new HashMap<>();

        for ( int i = 0; i < segments.size(); i++ ) {
            ICAPSegment seg = segments.get(i);
            ICAPSegment next = null;
            if ( i + 1 < segments.size() ) {
                next = segments.get(i + 1);
            }

            if ( "null-body".equals(seg.getType()) ) { //$NON-NLS-1$
                return new ICAPResponseImpl(this, statusCode, headers, headerParts);
            }
            else if ( seg.getType().endsWith("-body") ) { //$NON-NLS-1$
                if ( next != null ) {
                    throw new ICAPException("Body must be last segment"); //$NON-NLS-1$
                }
                if ( log.isTraceEnabled() ) {
                    log.trace(String.format("Reading chunked body %s", seg.getType())); //$NON-NLS-1$
                }
                return new ICAPResponseImpl(this, statusCode, headers, headerParts, ICAPIOUtils.readChunkedBody(this.inputStream));
            }
            else if ( next == null ) {
                throw new ICAPException("No next segment for non-body part"); //$NON-NLS-1$
            }
            else if ( seg.getType().endsWith("-hdr") ) { //$NON-NLS-1$
                long length = next.getOffset() - seg.getOffset();
                if ( log.isTraceEnabled() ) {
                    log.trace(String.format("Reading header segment %s with length %d", seg.getType(), length)); //$NON-NLS-1$
                }
                headerParts.put(seg.getType(), ICAPIOUtils.readHeaderBody(this.inputStream, length));
            }
        }

        return new ICAPResponseImpl(this, statusCode, headers, headerParts);
    }


    /**
     * @param list
     * @return
     * @throws ICAPException
     */
    private static List<ICAPSegment> parseEncapsulationHeader ( List<String> list ) throws ICAPException {
        String encHeaderVal = StringUtils.join(list, ", "); //$NON-NLS-1$
        String[] bodyParts = StringUtils.split(encHeaderVal, ',');

        long lastOffset = 0;

        List<ICAPSegment> segments = new ArrayList<>();
        for ( String bodyPart : bodyParts ) {
            int eqPos = bodyPart.indexOf('=');
            if ( eqPos < 7 ) {
                throw new ICAPException("Invalid encapsulation header " + encHeaderVal); //$NON-NLS-1$
            }

            String type = bodyPart.substring(0, eqPos).trim().toLowerCase();

            if ( !ENCAPSULATED_TYPES.contains(type) ) {
                throw new ICAPException("Invalid body type " + type); //$NON-NLS-1$
            }

            String offsetStr = bodyPart.substring(eqPos + 1);
            long offset;
            try {
                offset = Long.parseLong(offsetStr);

            }
            catch ( NumberFormatException e ) {
                throw new ICAPException("Invalid encapsulation offset " + offsetStr); //$NON-NLS-1$
            }

            if ( offset < lastOffset ) {
                throw new ICAPException("Non increasing encapsulation offsets" + encHeaderVal); //$NON-NLS-1$
            }

            segments.add(new ICAPSegment(type, offset, -1));
        }
        return segments;
    }


    /**
     * @return
     * @throws IOException
     */
    private Map<String, List<String>> getDefaultHeaders () throws IOException {
        Map<String, List<String>> headers = new HashMap<>();
        ICAPIOUtils.addHeader(
            headers,
            "User-Agent", //$NON-NLS-1$
            CLIENT_NAME + "/" + CLIENT_VERSION); //$NON-NLS-1$
        ICAPIOUtils.addHeader(headers, "Host", this.host); //$NON-NLS-1$
        ICAPIOUtils.addHeader(
            headers,
            "Allow", //$NON-NLS-1$
            "204"); //$NON-NLS-1$

        Map<String, String> customHeaders = this.cfg.getRequestHeaders();
        if ( customHeaders != null ) {
            for ( Entry<String, String> header : customHeaders.entrySet() ) {
                ICAPIOUtils.addHeader(headers, header.getKey(), header.getValue());
            }
        }
        return headers;
    }
}
