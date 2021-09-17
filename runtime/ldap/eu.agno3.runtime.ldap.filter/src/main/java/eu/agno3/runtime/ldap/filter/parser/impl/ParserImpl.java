/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2013 by mbechler
 */
package eu.agno3.runtime.ldap.filter.parser.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.ldap.filter.AbstractItemExpression;
import eu.agno3.runtime.ldap.filter.AndExpression;
import eu.agno3.runtime.ldap.filter.EmptyExpression;
import eu.agno3.runtime.ldap.filter.FilterEscaping;
import eu.agno3.runtime.ldap.filter.FilterExpression;
import eu.agno3.runtime.ldap.filter.FilterSyntaxException;
import eu.agno3.runtime.ldap.filter.FilterType;
import eu.agno3.runtime.ldap.filter.NotExpression;
import eu.agno3.runtime.ldap.filter.OrExpression;
import eu.agno3.runtime.ldap.filter.SimpleFilterExpression;
import eu.agno3.runtime.ldap.filter.parser.Parser;


/**
 * @author mbechler
 * 
 */
class ParserImpl implements Parser {

    private static final Logger log = Logger.getLogger(ParserImpl.class);

    private static final String OPEN_PAREN = Pattern.quote("("); //$NON-NLS-1$
    private static final String CLOSING_PARENT = Pattern.quote(")"); //$NON-NLS-1$
    private static final String AND = Pattern.quote("&"); //$NON-NLS-1$
    private static final String OR = Pattern.quote("|"); //$NON-NLS-1$
    private static final String NOT = Pattern.quote("!"); //$NON-NLS-1$

    private FilterExpression expr;


    /**
     * 
     */
    public ParserImpl () {}


    protected void parseString ( String filterSpec ) throws FilterSyntaxException {
        if ( log.isDebugEnabled() ) {
            log.debug("Parsing expression: " + filterSpec); //$NON-NLS-1$
        }
        try ( Scanner scanner = new Scanner(filterSpec) ) {
            scanner.useDelimiter(StringUtils.EMPTY);
            this.expr = parseExpr(scanner, 0, 0);

            if ( scanner.hasNext() ) {
                throw new FilterSyntaxException("Extra parenthesis at end of expression", filterSpec.length() - 1); //$NON-NLS-1$
            }
        }
    }


    @Override
    public FilterExpression getExpression () {
        return this.expr;
    }


    private static FilterExpression parseExpr ( Scanner scanner, int offset, int depth ) throws FilterSyntaxException {
        int pos = 0;

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Parsing expression at offset %d depth %d", offset, depth)); //$NON-NLS-1$
        }

        if ( !scanner.hasNext() ) {
            throw new FilterSyntaxException("The empty string is not a valid filter", offset); //$NON-NLS-1$
        }

        consumeOpeningParenthesis(scanner, offset);
        pos++;
        return parseExprContents(scanner, offset, depth, pos);
    }


    /**
     * @param scanner
     * @param offset
     * @param depth
     * @param pos
     * @return
     * @throws FilterSyntaxException
     */
    private static FilterExpression parseExprContents ( Scanner scanner, int offset, int depth, int pos ) throws FilterSyntaxException {
        if ( scanner.hasNext(CLOSING_PARENT) ) {
            return parseEmptyExpression(scanner);
        }
        else if ( scanner.hasNext(AND) || scanner.hasNext(OR) || scanner.hasNext(NOT) ) {
            return parseComplexExpression(scanner, offset, depth, pos);
        }
        else if ( scanner.hasNext(OPEN_PAREN) ) {
            throw new FilterSyntaxException("Unexpected opening parenthesis", offset); //$NON-NLS-1$
        }
        else {
            return parseItemExpression(scanner, offset, depth, pos);
        }
    }


    /**
     * @param scanner
     * @param offset
     * @param depth
     * @param pos
     * @return
     * @throws FilterSyntaxException
     */
    private static FilterExpression parseComplexExpression ( Scanner scanner, int offset, int depth, int startPos ) throws FilterSyntaxException {
        int pos = startPos;
        FilterExpression complex = parseComplexExpressionContents(scanner, offset, depth, pos);
        pos += complex.getSourceLength();
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Finished at offset %d: %s", offset + pos, complex.toString())); //$NON-NLS-1$
        }

        consumeClosingParenthesis(scanner, offset, complex);

        return complex;
    }


    /**
     * @param scanner
     * @param offset
     * @param depth
     * @param pos
     * @return
     * @throws FilterSyntaxException
     */
    private static FilterExpression parseComplexExpressionContents ( Scanner scanner, int offset, int depth, int pos ) throws FilterSyntaxException {
        if ( scanner.hasNext(AND) ) {
            return parseAndExpression(scanner, offset, depth, pos);
        }
        else if ( scanner.hasNext(OR) ) {
            return parseOrExpression(scanner, offset, depth, pos);
        }
        else if ( scanner.hasNext(NOT) ) {
            return parseNotExpression(scanner, offset, depth, pos);
        }

        throw new FilterSyntaxException("Unknown complex expression", offset); //$NON-NLS-1$
    }


    /**
     * @param scanner
     * @param offset
     * @param depth
     * @param pos
     * @return
     * @throws FilterSyntaxException
     */
    private static FilterExpression parseNotExpression ( Scanner scanner, int offset, int depth, int pos ) throws FilterSyntaxException {
        scanner.next(NOT);
        return new NotExpression(parseExpr(scanner, offset + pos, depth + 1));
    }


    /**
     * @param scanner
     * @param offset
     * @param depth
     * @param pos
     * @return
     * @throws FilterSyntaxException
     */
    private static FilterExpression parseOrExpression ( Scanner scanner, int offset, int depth, int startPos ) throws FilterSyntaxException {
        int pos = startPos;
        scanner.next(OR);
        pos++;
        List<FilterExpression> orClauses = new ArrayList<>();

        while ( !scanner.hasNext(CLOSING_PARENT) ) {
            FilterExpression andExpression = parseExpr(scanner, offset + pos, depth + 1);
            orClauses.add(andExpression);
            pos += andExpression.getSourceLength();
        }

        return new OrExpression(orClauses);
    }


    /**
     * @param scanner
     * @param offset
     * @param depth
     * @param pos
     * @return
     * @throws FilterSyntaxException
     */
    private static FilterExpression parseAndExpression ( Scanner scanner, int offset, int depth, int startPos ) throws FilterSyntaxException {
        int pos = startPos;
        scanner.next(AND);
        pos++;
        List<FilterExpression> andClauses = new ArrayList<>();

        while ( !scanner.hasNext(CLOSING_PARENT) ) {
            FilterExpression andExpression = parseExpr(scanner, offset + pos, depth + 1);
            andClauses.add(andExpression);
            pos += andExpression.getSourceLength();
        }

        return new AndExpression(andClauses);
    }


    /**
     * @param scanner
     * @param offset
     * @throws FilterSyntaxException
     */
    private static void consumeOpeningParenthesis ( Scanner scanner, int offset ) throws FilterSyntaxException {
        try {
            scanner.next(OPEN_PAREN);
        }
        catch ( NoSuchElementException e ) {
            throw new FilterSyntaxException("No opening parenthesis at start of expression", offset, e); //$NON-NLS-1$
        }
    }


    /**
     * @param scanner
     * @param offset
     * @param localExpr
     * @throws FilterSyntaxException
     */
    private static void consumeClosingParenthesis ( Scanner scanner, int offset, FilterExpression localExpr ) throws FilterSyntaxException {
        try {
            scanner.next(CLOSING_PARENT);
        }
        catch ( NoSuchElementException e ) {
            throw new FilterSyntaxException("Unclosed parenthesis at end of filter", offset + localExpr.getSourceLength(), e); //$NON-NLS-1$
        }
    }


    /**
     * @param scanner
     * @param offset
     * @param depth
     * @param pos
     * @return
     */
    private static FilterExpression parseItemExpression ( Scanner scanner, int offset, int depth, int pos ) {
        scanner.useDelimiter(CLOSING_PARENT);
        String itemDesc = scanner.next();
        scanner.useDelimiter(StringUtils.EMPTY);
        scanner.next(CLOSING_PARENT);
        return parseItem(itemDesc, offset + pos, depth);
    }


    /**
     * @param scanner
     * @return
     */
    private static FilterExpression parseEmptyExpression ( Scanner scanner ) {
        scanner.next();
        return new EmptyExpression();
    }


    /**
     * @param scanner
     * @param i
     * @return
     */
    private static AbstractItemExpression parseItem ( String itemDesc, int offset, int depth ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Found item at offset %d depth %d: %s", offset, depth, itemDesc)); //$NON-NLS-1$
        }

        try ( Scanner itemScanner = new Scanner(itemDesc) ) {
            itemScanner.useDelimiter(StringUtils.EMPTY);
            StringBuilder attrBuffer = new StringBuilder();
            FilterType comp = getFilterType(itemScanner, attrBuffer);

            StringBuilder valueBuffer = new StringBuilder();

            while ( itemScanner.hasNext() ) {
                valueBuffer.append(itemScanner.next());
            }

            return new SimpleFilterExpression(FilterEscaping.unescape(attrBuffer.toString()), comp, FilterEscaping.unescape(valueBuffer.toString()));
        }
    }


    /**
     * @param itemScanner
     * @param attrBuffer
     * @param comp
     * @return
     */
    private static FilterType getFilterType ( Scanner itemScanner, StringBuilder attrBuffer ) {
        while ( itemScanner.hasNext() ) {
            if ( itemScanner.hasNext(FilterType.EQUALS.getSymbol()) ) {
                itemScanner.next(FilterType.EQUALS.getSymbol());
                return FilterType.fromSymbol(FilterType.EQUALS.getSymbol());
            }
            else if ( itemScanner.hasNext("[<>~]") ) { //$NON-NLS-1$
                String c1 = itemScanner.next();

                if ( itemScanner.hasNext("=") ) { //$NON-NLS-1$
                    itemScanner.next();
                    String symbol = c1 + "="; //$NON-NLS-1$
                    return FilterType.fromSymbol(symbol);
                }
                attrBuffer.append(c1);
            }
            String sym = itemScanner.next();
            attrBuffer.append(sym);
        }
        return null;
    }
}
