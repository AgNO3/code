/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.01.2014 by mbechler
 */
package eu.agno3.runtime.db.schema.console;


import java.util.Set;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.changelog.visitor.ChangeSetVisitor;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;

import org.apache.karaf.shell.api.console.Session;
import org.fusesource.jansi.Ansi;


/**
 * @author mbechler
 * 
 */
public class ChangeSetConsoleOutputVisitor implements ChangeSetVisitor {

    /**
     * 
     */
    private final XMLChangeLogSerializer xmlSerializer;
    /**
     * 
     */
    private final Session ci;


    /**
     * @param xmlSerializer
     * @param ci
     */
    public ChangeSetConsoleOutputVisitor ( XMLChangeLogSerializer xmlSerializer, Session ci ) {
        this.xmlSerializer = xmlSerializer;
        this.ci = ci;
    }


    @Override
    public Direction getDirection () {
        return Direction.FORWARD;
    }


    @Override
    public void visit ( ChangeSet change, DatabaseChangeLog changeLog, Database db, Set<ChangeSetFilterResult> filterResults )
            throws LiquibaseException {
        Ansi out = Ansi.ansi();

        out.a("Change ").bold().a(change.getId()).boldOff(); //$NON-NLS-1$
        out.a(" from ").a(change.getFilePath()); //$NON-NLS-1$
        out.a(" Author: ").a(change.getAuthor()); //$NON-NLS-1$
        if ( change.getComments() != null ) {
            out.a(" - ").a(change.getComments()); //$NON-NLS-1$
        }
        out.newline();

        for ( Change c : change.getChanges() ) {

            out.a("  ").a(formatChangeLogXml(c)); //$NON-NLS-1$
        }

        out.newline();
        this.ci.getConsole().println(out.toString());
    }


    /**
     * @param c
     * @return
     */
    protected String formatChangeLogXml ( Change c ) {
        return this.xmlSerializer.serialize(c, true).replace(System.lineSeparator(), System.lineSeparator() + "  "); //$NON-NLS-1$
    }
}