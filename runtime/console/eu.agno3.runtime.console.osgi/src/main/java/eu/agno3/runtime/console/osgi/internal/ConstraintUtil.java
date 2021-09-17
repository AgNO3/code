/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2013 by mbechler
 */
package eu.agno3.runtime.console.osgi.internal;


import org.apache.log4j.Logger;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.ldap.filter.AndExpression;
import eu.agno3.runtime.ldap.filter.FilterException;
import eu.agno3.runtime.ldap.filter.FilterExpression;
import eu.agno3.runtime.ldap.filter.FilterParserException;
import eu.agno3.runtime.ldap.filter.FilterType;
import eu.agno3.runtime.ldap.filter.NotExpression;
import eu.agno3.runtime.ldap.filter.SimpleFilterExpression;
import eu.agno3.runtime.ldap.filter.parser.Parser;
import eu.agno3.runtime.ldap.filter.parser.ParserFactory;


/**
 * @author mbechler
 * 
 */
@Component ( service = ConstraintUtil.class )
public class ConstraintUtil {

    private static final String NEITHER_SIMPLE_NOR_AND = "Given filter is neither simple nor pure-AND."; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(ConstraintUtil.class);

    private ParserFactory filterParserFactory;


    /**
     * @param parserFactory
     *            the filterParserFactory to set
     */
    @Reference
    protected synchronized void setFilterParserFactory ( ParserFactory parserFactory ) {
        this.filterParserFactory = parserFactory;
    }


    /**
     * @param parserFactory
     * 
     */
    protected synchronized void unsetFilterParserFactory ( ParserFactory parserFactory ) {
        if ( this.filterParserFactory == parserFactory ) {
            this.filterParserFactory = null;
        }
    }


    /**
     * @param filter
     * @return whether the filter is either pure AND or simple
     */
    public boolean isSimpleFilter ( String filter ) {
        try {
            Parser p = this.filterParserFactory.parseString(filter);
            FilterExpression expr = p.getExpression();

            if ( expr instanceof SimpleFilterExpression ) {
                return true;
            }

            if ( expr instanceof AndExpression ) {
                return isSimpleAndFilter(expr);
            }

        }
        catch ( FilterException e ) {
            log.warn("Failed to parse filter expression:", e); //$NON-NLS-1$
            return false;
        }
        return false;
    }


    /**
     * @param expr
     * @return
     */
    private static boolean isSimpleAndFilter ( FilterExpression expr ) {
        AndExpression andEx = (AndExpression) expr;

        for ( FilterExpression clause : andEx.getClauses() ) {

            if ( clause instanceof NotExpression ) {
                NotExpression ne = (NotExpression) clause;

                if ( ! ( ne.getNegated() instanceof SimpleFilterExpression ) ) {
                    return false;
                }
            }

            if ( ! ( clause instanceof SimpleFilterExpression ) ) {
                return false;
            }
        }

        return true;
    }


    /**
     * Extract attribute values from simple or pure-AND filter
     * 
     * @param filter
     *            The filter to parse
     * @param attr
     *            The attribute to extract
     * @param type
     *            The comparison type to extract
     * @return The value of the specified constraint
     * @throws FilterParserException
     */
    public String extractSimpleFilterAttribute ( String filter, String attr, FilterType type ) throws FilterParserException {
        Parser p = this.filterParserFactory.parseString(filter);
        FilterExpression expr = p.getExpression();
        return extractSimpleFilterAttribute(expr, attr, type);
    }


    /**
     * @param expr
     * @param attr
     * @param type
     * @return
     */
    private static String extractSimpleFilterAttribute ( FilterExpression expr, String attr, FilterType type ) {
        if ( expr instanceof SimpleFilterExpression ) {
            SimpleFilterExpression se = (SimpleFilterExpression) expr;

            if ( se.getAttr().equals(attr) && se.getType().equals(type) ) {
                return se.getValue();
            }
        }

        if ( expr instanceof AndExpression ) {
            AndExpression andEx = (AndExpression) expr;
            String valueFound = extractFilterAttribute(attr, type, andEx);

            if ( valueFound == null ) {
                throw new IllegalArgumentException(String.format("The specified attribute '%s' (%s) could not be found.", attr, type.toString())); //$NON-NLS-1$
            }

            return valueFound;
        }

        throw new IllegalArgumentException(NEITHER_SIMPLE_NOR_AND);
    }


    /**
     * @param attr
     * @param type
     * @param andEx
     * @param valueFound
     * @return
     */
    private static String extractFilterAttribute ( String attr, FilterType type, AndExpression andEx ) {
        String valueFound = null;
        for ( FilterExpression clause : andEx.getClauses() ) {

            if ( clause instanceof AndExpression ) {
                try {
                    valueFound = extractSimpleFilterAttribute(clause, attr, type);
                }
                catch ( Exception e ) {
                    log.trace("Extracting simple filter attribute failed:", e); //$NON-NLS-1$
                    continue;
                }
            }

            if ( ! ( clause instanceof SimpleFilterExpression ) ) {
                throw new IllegalArgumentException(NEITHER_SIMPLE_NOR_AND);
            }

            SimpleFilterExpression se = (SimpleFilterExpression) clause;

            if ( se.getAttr().equals(attr) && se.getType().equals(type) ) {
                if ( valueFound != null ) {
                    throw new IllegalArgumentException(String.format("Multiple occurances of attribute '%s' (%s) found.", attr, type.toString())); //$NON-NLS-1$
                }
                valueFound = se.getValue();
            }

        }
        return valueFound;
    }


    /**
     * Extract negated attribute values from simple or pure-AND filter
     * 
     * @param filter
     *            The filter to parse
     * @param attr
     *            The attribute to extract
     * @param type
     *            The comparison type to extract
     * @return The value of the specified negated constraint
     * @throws FilterParserException
     */
    public String extractNegatedFilterAttribute ( String filter, String attr, FilterType type ) throws FilterParserException {
        Parser p = this.filterParserFactory.parseString(filter);
        FilterExpression expr = p.getExpression();

        return extractNegatedFilterAttribute(expr, attr, type);
    }


    /**
     * @param attr
     * @param type
     * @param expr
     * @return
     */
    private static String extractNegatedFilterAttribute ( FilterExpression expr, String attr, FilterType type ) {
        if ( expr instanceof AndExpression ) {
            return extactNegatedFilterAttributeAnd(expr, attr, type);
        }

        throw new IllegalArgumentException(NEITHER_SIMPLE_NOR_AND);
    }


    /**
     * @param expr
     * @param attr
     * @param type
     * @return
     */
    private static String extactNegatedFilterAttributeAnd ( FilterExpression expr, String attr, FilterType type ) {
        AndExpression andEx = (AndExpression) expr;
        String valueFound = null;

        for ( FilterExpression clause : andEx.getClauses() ) {

            if ( clause instanceof AndExpression ) {
                try {
                    valueFound = extractNegatedFilterAttribute(clause, attr, type);
                }
                catch ( IllegalArgumentException e ) {
                    log.trace("Failed to extract negated filter attribute:", e); //$NON-NLS-1$
                    continue;
                }
            }

            String foundInClause = extractValueFromNegatedFilter(clause, attr, type, valueFound);

            if ( foundInClause != null ) {
                valueFound = foundInClause;
            }
        }

        if ( valueFound == null ) {
            throw new IllegalArgumentException(String.format("The specified negated attribute '%s' (%s) could not be found.", attr, type.toString())); //$NON-NLS-1$
        }

        return valueFound;
    }


    private static String extractValueFromNegatedFilter ( FilterExpression clause, String attr, FilterType type, String prevValue ) {
        if ( ! ( clause instanceof NotExpression ) ) {
            return null;
        }

        NotExpression ne = (NotExpression) clause;

        if ( ! ( ne.getNegated() instanceof SimpleFilterExpression ) ) {
            return null;
        }

        SimpleFilterExpression se = (SimpleFilterExpression) ne.getNegated();

        if ( se.getAttr().equals(attr) && se.getType().equals(type) ) {
            if ( prevValue != null ) {
                throw new IllegalArgumentException(String.format("Multiple occurances of negated attribute '%s' (%s) found.", attr, type.toString())); //$NON-NLS-1$
            }
            return se.getValue();
        }

        return null;
    }


    /**
     * Extract a version range from a filter
     * 
     * @param filter
     * @return the specified version range
     */
    public VersionRange extractVersionRange ( String filter ) {

        try {
            Version equalsVersion = Version.parseVersion(this.extractSimpleFilterAttribute(filter, Constants.VERSION_ATTRIBUTE, FilterType.EQUALS));
            return new VersionRange(VersionRange.LEFT_CLOSED, equalsVersion, equalsVersion, VersionRange.RIGHT_CLOSED);
        }
        catch (
            FilterParserException |
            IllegalArgumentException e ) {
            log.trace("Cannot parse exact version:", e); //$NON-NLS-1$
        }

        Version minVersionIncl = tryParseMinVersionIncl(filter);
        Version minVersionExcl = tryParseMinVersionExcl(filter);
        Version maxVersionIncl = tryParseMaxVersionIncl(filter);
        Version maxVersionExcl = tryParseMaxVersionExcl(filter);

        return createVersionRange(minVersionIncl, maxVersionIncl, minVersionExcl, maxVersionExcl);
    }


    /**
     * @param filter
     * @return
     */
    private Version tryParseMaxVersionExcl ( String filter ) {
        Version maxVersionExcl = null;
        try {
            maxVersionExcl = Version.parseVersion(this.extractNegatedFilterAttribute(filter, Constants.VERSION_ATTRIBUTE, FilterType.GREATER_EQUALS));
        }
        catch (
            FilterParserException |
            IllegalArgumentException e ) {
            log.trace("Cannot parse maxVersionExcl:", e); //$NON-NLS-1$
        }
        return maxVersionExcl;
    }


    /**
     * @param filter
     * @return
     */
    private Version tryParseMaxVersionIncl ( String filter ) {
        Version maxVersionIncl = null;
        try {
            maxVersionIncl = Version.parseVersion(this.extractSimpleFilterAttribute(filter, Constants.VERSION_ATTRIBUTE, FilterType.LESS_EQUALS));
        }
        catch (
            FilterParserException |
            IllegalArgumentException e ) {
            log.trace("Cannot parse maxVersionIncl:", e); //$NON-NLS-1$
        }
        return maxVersionIncl;
    }


    /**
     * @param filter
     * @return
     */
    private Version tryParseMinVersionExcl ( String filter ) {
        Version minVersionExcl = null;
        try {
            minVersionExcl = Version.parseVersion(this.extractNegatedFilterAttribute(filter, Constants.VERSION_ATTRIBUTE, FilterType.LESS_EQUALS));
        }
        catch (
            FilterParserException |
            IllegalArgumentException e ) {
            log.trace("Cannot parse minVersionExcl:", e); //$NON-NLS-1$
        }
        return minVersionExcl;
    }


    /**
     * @param filter
     * @return
     */
    private Version tryParseMinVersionIncl ( String filter ) {
        Version minVersionIncl = null;
        try {
            minVersionIncl = Version.parseVersion(this.extractSimpleFilterAttribute(filter, Constants.VERSION_ATTRIBUTE, FilterType.GREATER_EQUALS));
        }
        catch (
            FilterParserException |
            IllegalArgumentException e ) {
            log.trace("Cannot parse minVersionIncl:", e); //$NON-NLS-1$
        }
        return minVersionIncl;
    }


    /**
     * @param minVersionIncl
     * @param maxVersionIncl
     * @param minVersionExcl
     * @param maxVersionExcl
     * @return
     */
    private static VersionRange createVersionRange ( Version minVersionIncl, Version maxVersionIncl, Version minVersionExcl, Version maxVersionExcl ) {

        if ( minVersionIncl != null ) {
            return makeClosedLowerBoundVersionRange(minVersionIncl, maxVersionExcl);
        }

        if ( minVersionExcl != null ) {
            return makeOpenLowerBoundVersionRange(minVersionExcl, maxVersionExcl);
        }

        if ( maxVersionExcl != null ) {
            return makeCOVersionRange(Version.emptyVersion, maxVersionExcl);
        }

        return makeCCVersionRange(Version.emptyVersion, maxVersionIncl);
    }


    /**
     * @param minVersionExcl
     * @param maxVersionExcl
     * @return
     */
    private static VersionRange makeOpenLowerBoundVersionRange ( Version minVersionExcl, Version maxVersionExcl ) {
        if ( maxVersionExcl != null ) {
            return makeOCVersionRange(minVersionExcl, maxVersionExcl);
        }
        return makeOOVersionRange(minVersionExcl, null);
    }


    /**
     * @param minVersionIncl
     * @param maxVersionExcl
     * @return
     */
    private static VersionRange makeClosedLowerBoundVersionRange ( Version minVersionIncl, Version maxVersionExcl ) {
        if ( maxVersionExcl != null ) {
            return makeCOVersionRange(minVersionIncl, maxVersionExcl);
        }

        return makeCCVersionRange(minVersionIncl, null);
    }


    /**
     * @param min
     * @param max
     * @return () version range
     */
    private static VersionRange makeOOVersionRange ( Version min, Version max ) {
        return new VersionRange(VersionRange.LEFT_OPEN, min, max, VersionRange.RIGHT_OPEN);
    }


    /**
     * @param maxVersionIncl
     * @return [] version range
     */
    private static VersionRange makeCCVersionRange ( Version min, Version max ) {
        return new VersionRange(VersionRange.LEFT_CLOSED, min, max, VersionRange.RIGHT_CLOSED);
    }


    /**
     * @param min
     * @param max
     * @return (] version range
     */
    private static VersionRange makeOCVersionRange ( Version min, Version max ) {
        return new VersionRange(VersionRange.LEFT_OPEN, min, max, VersionRange.RIGHT_OPEN);
    }


    /**
     * @param min
     * @param max
     * @return [) version range
     */
    private static VersionRange makeCOVersionRange ( Version min, Version max ) {
        return new VersionRange(VersionRange.LEFT_CLOSED, min, max, VersionRange.RIGHT_OPEN);
    }
}
