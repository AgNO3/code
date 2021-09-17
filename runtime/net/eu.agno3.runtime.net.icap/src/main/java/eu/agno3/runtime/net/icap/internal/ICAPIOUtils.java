/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.05.2015 by mbechler
 */
package eu.agno3.runtime.net.icap.internal;


import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.net.icap.ICAPException;
import eu.agno3.runtime.net.icap.ICAPProtocolException;
import eu.agno3.runtime.net.icap.ICAPProtocolStatusException;


/**
 * @author mbechler
 *
 */
public class ICAPIOUtils {

    private static final Logger log = Logger.getLogger(ICAPIOUtils.class);

    /**
     * 
     */
    private static final int LINE_LIMIT = 998;
    private static final int HEADER_LIMIT = 64;
    private static final Charset ASCII = Charset.forName("US-ASCII"); //$NON-NLS-1$
    private static final String ICAP_1_0 = "ICAP/1.0"; //$NON-NLS-1$
    private static final int SPACE = ' ';
    private static final int READ_LINE_LIMIT = 4096;

    private static byte[] CRLF = new byte[] {
        13, 10
    };;


    /**
     * @param is
     * @param l
     * @return the body contents
     * @throws ICAPException
     * @throws IOException
     * 
     */
    public static String readHeaderBody ( InputStream is, long l ) throws ICAPException, IOException {
        if ( l > HEADER_LIMIT * LINE_LIMIT ) {
            throw new ICAPException("Header segment size exceeds limit"); //$NON-NLS-1$
        }
        byte[] data = new byte[(int) l];
        int read = 0;
        while ( read < l ) {
            int res = is.read(data, read, (int) l);
            if ( res == -1 ) {
                throw new IOException("EOF"); //$NON-NLS-1$
            }
            read += res;
        }
        return new String(data, ASCII);
    }


    /**
     * @param is
     * @return a chunked input stream
     * 
     */
    public static ChunkedInputStream readChunkedBody ( InputStream is ) {
        return new ChunkedInputStream(is);
    }


    /**
     * @param os
     * @param chunkSize
     * @param is
     * @param includeIEOF
     * @param trailers
     * @throws IOException
     */
    public static void sendChunked ( OutputStream os, int chunkSize, InputStream is, boolean includeIEOF, Map<String, List<String>> trailers )
            throws IOException {
        byte[] chunkBuffer = new byte[chunkSize];
        while ( true ) {
            int read = 0;
            int ret = -1;
            boolean eof = false;
            // fill chunk buffer
            while ( read < chunkSize && ( ret = is.read(chunkBuffer, read, chunkSize - read) ) >= 0 ) {
                read += ret;
            }
            if ( ret < 0 ) {
                eof = true;
            }

            if ( read > 0 ) {
                os.write(Integer.toHexString(read).getBytes(ASCII));
                crlf(os);
                os.write(chunkBuffer, 0, read);
                crlf(os);
            }

            if ( eof ) {
                break;
            }
        }

        os.write(Integer.toHexString(0).getBytes(ASCII));
        if ( includeIEOF ) {
            log.debug("Writing ieof"); //$NON-NLS-1$
            os.write("; ieof".getBytes(ASCII)); //$NON-NLS-1$
        }
        crlf(os);

        if ( !trailers.isEmpty() ) {
            writeHeaders(os, trailers);
        }
        crlf(os);

    }


    /**
     * @param os
     * @param input
     * @throws IOException
     */
    public static void sendRaw ( OutputStream os, InputStream input ) throws IOException {
        IOUtils.copy(input, os);
    }


    /**
     * 
     * @param os
     * @throws IOException
     */
    public static void crlf ( OutputStream os ) throws IOException {
        os.write(CRLF);
    }


    /**
     * 
     * @param os
     * @param url
     * @param method
     * @throws IOException
     */
    public static void writeRequestLine ( OutputStream os, URI url, String method ) throws IOException {
        os.write(method.getBytes(ASCII));
        os.write(SPACE);
        os.write(url.toString().getBytes(ASCII));
        os.write(SPACE);
        os.write(ICAP_1_0.getBytes(ASCII));
        crlf(os);
    }


    /**
     * 
     * @param is
     * @return the parsed headers
     * @throws IOException
     */
    public static Map<String, List<String>> readHeaders ( InputStream is ) throws IOException {
        Map<String, List<String>> headers = new HashMap<>();
        String line = readLine(is);

        int numHeaders = 0;
        StringBuilder lastHeaderLine = new StringBuilder();

        while ( !StringUtils.isBlank(line) && numHeaders < HEADER_LIMIT ) {
            if ( line.charAt(0) == SPACE || lastHeaderLine.length() == 0 ) {
                lastHeaderLine.append(line);
            }
            else if ( line.charAt(0) == SPACE ) {
                throw new IOException("Invalid header continuation"); //$NON-NLS-1$
            }
            else if ( lastHeaderLine.length() > 0 ) {

                parseHeader(headers, lastHeaderLine.toString());
                lastHeaderLine = new StringBuilder(line);
            }

            line = readLine(is);
        }

        if ( lastHeaderLine.length() > 0 ) {
            parseHeader(headers, lastHeaderLine.toString());
            lastHeaderLine = new StringBuilder();
        }

        return headers;
    }


    /**
     * @param headers
     * @param headerLine
     * @throws ICAPException
     * @throws IOException
     */
    private static void parseHeader ( Map<String, List<String>> headers, String headerLine ) throws IOException {
        int sepPos = headerLine.indexOf(':');
        if ( sepPos <= 0 || ( sepPos + 1 ) == headerLine.length() ) {
            throw new IOException("Invalid header line " + headerLine); //$NON-NLS-1$
        }

        String name = headerLine.substring(0, sepPos).toLowerCase();
        String value = headerLine.substring(sepPos + 2);

        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Found header %s: %s", name, value)); //$NON-NLS-1$
        }
        addHeader(headers, name, value);
    }


    /**
     * @param headers
     * @param name
     * @param value
     * @throws IOException
     */
    public static void addHeader ( Map<String, List<String>> headers, String name, String value ) throws IOException {

        if ( name.indexOf('\r') >= 0 || name.indexOf('\n') >= 0 ) {
            throw new IOException("Header name contains newline"); //$NON-NLS-1$
        }

        if ( value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0 ) {
            throw new IOException("Header value contains newline"); //$NON-NLS-1$
        }

        List<String> headerValues = headers.get(name);
        if ( headerValues == null ) {
            headerValues = new LinkedList<>();
            headers.put(name, headerValues);
        }
        headerValues.add(value);
    }


    /**
     * 
     * @param os
     * @param headers
     * @throws IOException
     */
    public static void writeHeaders ( OutputStream os, Map<String, List<String>> headers ) throws IOException {
        for ( Entry<String, List<String>> header : headers.entrySet() ) {
            for ( String value : header.getValue() ) {
                writeHeader(os, header.getKey(), value);
            }
        }
    }


    /**
     * 
     * @param os
     * @param name
     * @param value
     * @throws IOException
     */
    public static void writeHeader ( OutputStream os, String name, String value ) throws IOException {
        os.write(name.getBytes(ASCII));
        os.write(':');
        os.write(SPACE);

        if ( value.isEmpty() ) {
            crlf(os);
            return;
        }

        int wrote = 0;
        boolean first = true;

        while ( wrote < value.length() ) {
            // fold overly long lines
            int remainLength;
            if ( !first ) {
                os.write(SPACE);
                remainLength = LINE_LIMIT;
            }
            else {
                remainLength = LINE_LIMIT - 2 - name.length();
                first = false;
            }
            os.write(value.substring(wrote, Math.min(value.length(), wrote + remainLength)).getBytes(ASCII));
            wrote += remainLength;
            crlf(os);
        }
    }


    /**
     * 
     * @param is
     * @return the status code
     * @throws ICAPException
     * @throws IOException
     */
    public static int readStatusLine ( InputStream is ) throws ICAPException, IOException {
        String statusLine;
        try {
            statusLine = readLine(is);
        }
        catch ( IOException e ) {
            throw new IOException("Failed to read response", e); //$NON-NLS-1$
        }

        if ( StringUtils.isBlank(statusLine) ) {
            throw new ICAPProtocolException("Empty response status"); //$NON-NLS-1$
        }

        String[] parts = StringUtils.split(statusLine, StringUtils.SPACE, 3);

        if ( parts == null || parts.length != 3 ) {
            throw new ICAPProtocolException("Invalid response status line: " + statusLine); //$NON-NLS-1$
        }

        if ( !ICAP_1_0.equalsIgnoreCase(parts[ 0 ]) ) {
            throw new ICAPProtocolException("Invalid icap protocol: " + parts[ 0 ]); //$NON-NLS-1$
        }

        try {
            int code = Integer.parseInt(parts[ 1 ]);

            if ( code >= 400 || code < 100 ) {
                throw new ICAPProtocolStatusException(code, parts[ 2 ]);
            }

            return code;
        }
        catch ( NumberFormatException e ) {
            throw new ICAPProtocolException("Invalid response: " + statusLine); //$NON-NLS-1$
        }
    }


    /**
     * 
     * @param is
     * @return the read line
     * @throws IOException
     */
    public static String readLine ( InputStream is ) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        int ret = -1;
        boolean lastWasCR = false;
        while ( ( ret = is.read() ) > 0 ) {
            c = ret;
            if ( c == 10 && lastWasCR ) {
                break;
            }
            else if ( c == 13 ) {
                lastWasCR = true;
            }

            if ( sb.length() > READ_LINE_LIMIT ) {
                throw new IOException("Line length exceeds " + READ_LINE_LIMIT); //$NON-NLS-1$
            }

            sb.append((char) c);
        }

        if ( ret < 0 ) {
            throw new EOFException("End of file while reading line"); //$NON-NLS-1$
        }

        if ( sb.length() > 0 ) {
            sb.deleteCharAt(sb.length() - 1);
        }
        String line = sb.toString();
        if ( log.isTraceEnabled() ) {
            log.trace("Read line " + line); //$NON-NLS-1$
        }
        return line;
    }

}
