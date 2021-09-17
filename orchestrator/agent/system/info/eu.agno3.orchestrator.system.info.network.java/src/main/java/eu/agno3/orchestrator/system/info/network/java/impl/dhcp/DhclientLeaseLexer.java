/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.network.java.impl.dhcp;


import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;


/**
 * @author mbechler
 *
 */
public class DhclientLeaseLexer {

    private final Reader reader;


    /**
     * @param r
     */
    public DhclientLeaseLexer ( Reader r ) {
        this.reader = r;
    }


    /**
     * 
     * @return next token, skipping comments
     * @throws IOException
     */
    public Token nextNonComment () throws IOException {
        Token t;
        while ( ( t = next() ).getType() == TokenType.COMMENT ) {}
        return t;
    }


    /**
     * 
     * @return next token, skipping comments
     * @throws IOException
     */
    public Token nextNonCommentNonWS () throws IOException {
        Token t;
        while ( ( t = nextNonComment() ).getType() == TokenType.SPACE ) {}
        return t;
    }


    /**
     * @param types
     * @return next token
     * @throws IOException
     * @throws DhclientLeaseParserException
     *             if the token does not match the expected type
     */
    public Token readNext ( TokenType... types ) throws IOException, DhclientLeaseParserException {

        Token t = nextNonComment();

        boolean found = false;

        if ( types == null || types.length == 0 ) {
            return t;
        }

        for ( TokenType tt : types ) {
            if ( tt == t.getType() ) {
                found = true;
                break;
            }
        }

        if ( !found ) {
            throw new DhclientLeaseParserException(String.format("Expected %s have %s", Arrays.toString(types), t.getType())); //$NON-NLS-1$
        }
        return t;
    }


    /**
     * 
     * @return the next token
     * @throws IOException
     */
    public Token next () throws IOException {
        int c = this.reader.read();

        if ( c < 0 ) {
            return new Token(TokenType.EOF);
        }

        if ( Character.isWhitespace(c) ) {
            return readMoreSpace();
        }
        else if ( c == '{' ) {
            return new Token(TokenType.BEGIN_BLOCK);
        }
        else if ( c == '}' ) {
            return new Token(TokenType.END_BLOCK);
        }
        else if ( c == '"' ) {
            return readStringContent();
        }
        else if ( c == '#' ) {
            return readComment();
        }
        else if ( c == ';' ) {
            return new Token(TokenType.END_STMT);
        }
        else if ( c == ',' ) {
            return new Token(TokenType.LIST_SEP);
        }
        else if ( isKeywordCharacter(c) ) {
            // we don't treat number separately
            return readKeyword(c);
        }

        return new Token(TokenType.FAIL, String.valueOf((char) c));
    }


    /**
     * @return
     * @throws IOException
     */
    private Token readComment () throws IOException {
        int c;
        while ( ( c = this.reader.read() ) > 0 ) {
            if ( c == '\n' ) {
                break;
            }
        }
        return new Token(TokenType.COMMENT);
    }


    /**
     * @param c
     * @return
     */
    private static boolean isKeywordCharacter ( int c ) {
        return Character.isLetterOrDigit(c) || c == '-' || c == ':' || c == '/' || c == '.';
    }


    /**
     * @param c
     * @return
     * @throws IOException
     */
    private Token readKeyword ( int c ) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append((char) c);
        while ( true ) {
            this.reader.mark(1);
            int r = this.reader.read();
            if ( r < 0 ) {
                return new Token(TokenType.KEYWORD, sb.toString());
            }
            else if ( !isKeywordCharacter(r) ) {
                this.reader.reset();
                return new Token(TokenType.KEYWORD, sb.toString());
            }
            sb.append((char) r);
        }
    }


    /**
     * @return
     * @throws IOException
     */
    private Token readStringContent () throws IOException {
        StringBuilder sb = new StringBuilder();
        int c = this.reader.read();
        boolean escaped = false;

        while ( c != '"' || escaped ) {
            escaped = false;
            if ( escaped ) {
                char r = readEscapeSequence(sb, c);
                if ( r < 0 ) {
                    return new Token(TokenType.FAIL);
                }
                sb.append(c);
            }
            else {
                sb.append((char) c);
            }
            c = this.reader.read();
            if ( c < 0 ) {
                return new Token(TokenType.FAIL);
            }

            if ( c == '\\' ) {
                escaped = true;
            }
        }
        return new Token(TokenType.STRING, sb.toString());
    }


    /**
     * @param sb
     * @param c
     * @throws IOException
     */
    private char readEscapeSequence ( StringBuilder sb, int c ) throws IOException {
        switch ( c ) {
        case 't':
            return '\t';
        case 'r':
            return '\r';
        case 'n':
            return '\n';
        case 'b':
            return '\b';
        case '0':
        case '1':
        case '2':
        case '3':
            // octal
            return readOctalEscape(c);
        case 'x':
            // hex
            return readHexEscape();
        default:
            return (char) c;
        }
    }


    /**
     * @return
     * @throws IOException
     */
    private char readHexEscape () throws IOException {
        int r = 0;
        for ( int i = 0; i < 2; i++ ) {
            int c = this.reader.read();
            if ( c >= '0' && c <= '9' ) {
                r = r * 16 + ( c - '0' );
            }
            else if ( c >= 'a' && c <= 'f' ) {
                r = r * 16 + ( c - 'a' + 10 );
            }
            else if ( c >= 'A' && c <= 'F' ) {
                r = r * 16 + ( c - 'A' + 10 );
            }
            else {
                return (char) -1;
            }
        }
        return (char) r;
    }


    /**
     * @param i
     * @return
     * @throws IOException
     */
    private char readOctalEscape ( int i ) throws IOException {
        int r = ( i - '0' );
        for ( int j = 0; j < 2; j++ ) {
            this.reader.mark(1);
            int c = this.reader.read();
            if ( c >= '0' && c <= '7' ) {
                r = r * 8 + ( c - '0' );
            }
            else if ( r != 0 ) {
                return (char) -1;
            }
            else {
                this.reader.reset();
                return 0;
            }
        }
        return (char) r;
    }


    /**
     * @param r
     * @return
     * @throws IOException
     */
    private Token readMoreSpace () throws IOException {
        while ( true ) {
            this.reader.mark(1);
            int r = this.reader.read();
            if ( r < 0 ) {
                return new Token(TokenType.SPACE);
            }
            else if ( !Character.isWhitespace(r) ) {
                this.reader.reset();
                return new Token(TokenType.SPACE);
            }
        }
    }

    @SuppressWarnings ( "javadoc" )
    public enum TokenType {
        FAIL, EOF, SPACE, BEGIN_BLOCK, END_BLOCK, STRING, KEYWORD, COMMENT, END_STMT, LIST_SEP
    }

    /**
     * 
     * @author mbechler
     *
     */
    public static class Token {

        private final TokenType type;
        private final String content;


        /**
         * @param type
         * 
         */
        public Token ( TokenType type ) {
            this(type, null);
        }


        /**
         * @param type
         * @param content
         * 
         */
        public Token ( TokenType type, String content ) {
            this.type = type;
            this.content = content;
        }


        /**
         * @return the type
         */
        public TokenType getType () {
            return this.type;
        }


        /**
         * @return the content
         */
        public String getContent () {
            return this.content;
        }


        /**
         * {@inheritDoc}
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString () {
            return String.format(
                "%s : %s", //$NON-NLS-1$
                this.type,
                this.content != null ? this.content : "<none>"); //$NON-NLS-1$
        }
    }

}
