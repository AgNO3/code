/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.packagekit.internal;


import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.freedesktop.PackageKit;
import org.freedesktop.PackageKit.Transaction;
import org.freedesktop.PackageKit.Transaction.Category;
import org.freedesktop.PackageKit.Transaction.Destroy;
import org.freedesktop.PackageKit.Transaction.Details;
import org.freedesktop.PackageKit.Transaction.DistroUpgrade;
import org.freedesktop.PackageKit.Transaction.ErrorCode;
import org.freedesktop.PackageKit.Transaction.EulaRequired;
import org.freedesktop.PackageKit.Transaction.Finished;
import org.freedesktop.PackageKit.Transaction.ItemProgress;
import org.freedesktop.PackageKit.Transaction.MediaChangeRequired;
import org.freedesktop.PackageKit.Transaction.RepoDetail;
import org.freedesktop.PackageKit.Transaction.RepoSignatureRequired;
import org.freedesktop.PackageKit.Transaction.RequireRestart;
import org.freedesktop.PackageKit.Transaction.UpdateDetail;
import org.freedesktop.dbus.DBus;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.types.UInt32;

import eu.agno3.orchestrator.system.dbus.SystemDBusClient;
import eu.agno3.orchestrator.system.packagekit.PackageKitException;
import eu.agno3.orchestrator.system.packagekit.PackageKitListener;
import eu.agno3.orchestrator.system.packagekit.PackageKitProgressListener;


/**
 * @author mbechler
 *
 */
public class SignalHandlers implements AutoCloseable {

    private static final Logger log = Logger.getLogger(SignalHandlers.class);

    private Map<Class<? extends DBusSignal>, DBusSigHandler<?>> registered = new HashMap<>();
    private SystemDBusClient connection;
    private Transaction tx;

    private Object waitLock = new Object();
    private volatile boolean completed = false;

    private PackageKitException error;

    private PackageKitListener listener;

    private int lastStatus = -1;
    private Integer lastPercent;

    private int progressScale = 100;
    private int progressOffset = 0;

    private PackageKitProgressListener progressListener;


    /**
     * @param connection
     * @param pk
     * @param progressListener
     * @param listener
     * @param handlers
     * @throws DBusException
     * 
     */
    public SignalHandlers ( SystemDBusClient connection, PackageKit pk, PackageKitProgressListener progressListener, PackageKitListener listener,
            Class<?>... handlers ) throws DBusException {
        this.connection = connection;
        this.progressListener = progressListener;
        this.tx = (Transaction) pk.CreateTransaction();
        this.listener = listener;
        Set<Class<?>> handlerTypes = new HashSet<>();
        handlerTypes.addAll(Arrays.asList(handlers));
        handlerTypes.add(ErrorCode.class);
        handlerTypes.add(Destroy.class);
        handlerTypes.add(Finished.class);
        registerHandlers(this.connection, this.tx, handlerTypes);
    }


    /**
     * 
     * @param scale
     * @param offset
     * @return this
     */
    public SignalHandlers scaleProgress ( int scale, int offset ) {
        this.progressScale = scale;
        this.progressOffset = offset;
        if ( this.progressListener != null ) {
            this.progressListener.haveProgress(0, offset);
        }
        return this;
    }


    /**
     * @return the transaction
     */
    public Transaction getTransaction () {
        return this.tx;
    }


    /**
     * @return the listener
     */
    public PackageKitListener getListener () {
        return this.listener;
    }


    /**
     * @param prop
     * @return the property value
     */
    public Object getProperty ( String prop ) {
        return ( (DBus.Properties) this.tx ).Get("org.freedesktop.PackageKit.Transaction", prop); //$NON-NLS-1$
    }


    /**
     * 
     * @return current transaction status
     */
    public int getStatus () {
        return ( (UInt32) getProperty("Status") ).intValue(); //$NON-NLS-1$
    }


    /**
     * 
     * @return current transaction percentage
     */
    public Integer getPercentage () {
        Integer val = ( (UInt32) getProperty("Percentage") ).intValue(); //$NON-NLS-1$
        if ( val == 101 ) {
            return null;
        }
        return val;
    }


    void updateProgress () {
        int status = getStatus();
        Integer percent = getPercentage();
        if ( status != this.lastStatus || ( percent != null && this.lastPercent != percent ) ) {
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Status is %d Percent is %s", status, percent)); //$NON-NLS-1$
            }

            if ( percent != null && this.progressListener != null ) {
                float scaled = percent * ( this.progressScale / 100.0f ) + this.progressOffset;
                if ( this.progressListener != null ) {
                    this.progressListener.haveProgress(status, scaled);
                }
            }
            this.lastPercent = percent;
            this.lastStatus = status;
        }
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * @throws InterruptedException
     * @throws PackageKitException
     * 
     */
    public void waitForCompletion () throws InterruptedException, PackageKitException {
        synchronized ( this.waitLock ) {
            this.waitLock.wait();
        }
        check();
    }


    /**
     * @param timeout
     * @throws InterruptedException
     * @throws PackageKitException
     * 
     */
    public void waitForCompletion ( long timeout ) throws InterruptedException, PackageKitException {
        synchronized ( this.waitLock ) {
            this.waitLock.wait(timeout);
        }
        check();
    }


    void finished () {
        this.completed = true;
        synchronized ( this.waitLock ) {
            this.waitLock.notify();
        }

    }


    /**
     * @param error
     *            the error to set
     */
    void setError ( PackageKitException error ) {
        if ( this.error == null ) {
            this.error = error;
        }
    }


    void error ( PackageKitException e ) {
        this.error = e;
        synchronized ( this.waitLock ) {
            this.waitLock.notify();
        }

    }


    /**
     * @throws PackageKitException
     */
    public void check () throws PackageKitException {

        if ( !this.completed ) {
            try {
                this.tx.Cancel();
            }
            catch ( DBusExecutionException ex ) {
                log.debug("Failed to cancel transaction", ex); //$NON-NLS-1$
            }
        }

        if ( this.error != null ) {
            throw new PackageKitException("Error in call", this.error); //$NON-NLS-1$
        }

        if ( !this.completed ) {
            throw new PackageKitException("Did not complete request"); //$NON-NLS-1$
        }
    }


    /**
     * @param dbus
     * @param transaction
     * @throws DBusException
     */
    private void registerHandlers ( SystemDBusClient dbus, Transaction transaction, Set<Class<?>> handlers ) throws DBusException {
        registerSignalHandler(handlers, ErrorCode.class, new PackageKitErrorHandler());
        registerSignalHandler(handlers, Destroy.class, new PackageKitDestroyHandler());
        registerSignalHandler(handlers, Finished.class, new PackageKitFinishedHandler());
        registerSignalHandler(handlers, org.freedesktop.PackageKit.Transaction.Package.class, new PackageKitPackageHandler());
        registerSignalHandler(handlers, Details.class, new PackageKitDetailsHandler());
        registerSignalHandler(handlers, Category.class, new PackageKitCategoryHandler());
        registerSignalHandler(handlers, RepoDetail.class, new PackageKitRepoDetailHandler());
        registerSignalHandler(handlers, RepoSignatureRequired.class, new PackageKitRepoSignatureRequiredHandler());
        registerSignalHandler(handlers, EulaRequired.class, new PackageKitEulaRequiredHandler());
        registerSignalHandler(handlers, MediaChangeRequired.class, new PackageKitMediaChangeRequiredHandler());
        registerSignalHandler(handlers, RequireRestart.class, new PackageKitRequireRestartHandler());
        registerSignalHandler(handlers, UpdateDetail.class, new PackageKitUpdateDetailHandler());
        registerSignalHandler(handlers, DistroUpgrade.class, new PackageKitDistroUpgradeHandler());
        registerSignalHandler(handlers, ItemProgress.class, new PackageKitItemProgressHandler());
    }


    /**
     * @param signal
     * @param handler
     * @throws DBusException
     */
    private <T extends DBusSignal> void registerSignalHandler ( Set<Class<?>> handlers, Class<T> signal, DBusSigHandler<T> handler )
            throws DBusException {
        if ( !handlers.contains(signal) ) {
            return;
        }
        if ( this.registered.put(signal, handler) != null ) {
            throw new IllegalArgumentException("Duplicate handler registration for " + signal.getName()); //$NON-NLS-1$
        }
        this.connection.addSigHandler(signal, this.tx, handler);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @SuppressWarnings ( {
        "rawtypes", "unchecked"
    } )
    @Override
    public void close () throws DBusException {
        for ( Entry<Class<? extends DBusSignal>, DBusSigHandler<?>> e : this.registered.entrySet() ) {
            try {
                this.connection.removeSigHandler(e.getKey(), this.tx, (DBusSigHandler) e.getValue());
            }
            catch ( Exception ex ) {
                log.warn("Failed to remove signal handler", ex); //$NON-NLS-1$
            }
        }

    }

    private final class PackageKitDestroyHandler implements DBusSigHandler<Destroy> {

        /**
         * 
         */
        public PackageKitDestroyHandler () {}


        /**
         * {@inheritDoc}
         *
         * @see org.freedesktop.dbus.DBusSigHandler#handle(org.freedesktop.dbus.DBusSignal)
         */
        @Override
        public void handle ( Destroy d ) {
            getLog().debug("Destroy"); //$NON-NLS-1$
            finished();
        }

    }

    private final class PackageKitFinishedHandler implements DBusSigHandler<Finished> {

        /**
         * 
         */
        public PackageKitFinishedHandler () {}


        /**
         * {@inheritDoc}
         *
         * @see org.freedesktop.dbus.DBusSigHandler#handle(org.freedesktop.dbus.DBusSignal)
         */
        @Override
        public void handle ( Finished fin ) {

            getLog().debug("Finished " + fin.exit); //$NON-NLS-1$
            if ( fin.exit.intValue() != 1 ) {
                // only set error, there still might be an ErrorCode signal coming
                setError(new PackageKitException(fin.exit.longValue(), "Finished with non successful code " + fin.exit.intValue())); //$NON-NLS-1$
            }
            else {
                // Wait for destroy, there may be still signals coming
            }
        }
    }

    /**
     * @author mbechler
     *
     */
    private final class PackageKitErrorHandler implements DBusSigHandler<ErrorCode> {

        /**
         * 
         */
        public PackageKitErrorHandler () {}


        @Override
        public void handle ( ErrorCode err ) {
            updateProgress();
            if ( getLog().isDebugEnabled() ) {
                getLog().debug(String.format("ErrorCode %d: %s", err.code.intValue(), err.details)); //$NON-NLS-1$
            }

            error(new PackageKitException(err.code.longValue(), err.details));

        }
    }

    private final class PackageKitPackageHandler implements DBusSigHandler<org.freedesktop.PackageKit.Transaction.Package> {

        /**
         * 
         */
        public PackageKitPackageHandler () {}


        /**
         * {@inheritDoc}
         *
         * @see org.freedesktop.dbus.DBusSigHandler#handle(org.freedesktop.dbus.DBusSignal)
         */
        @Override
        public void handle ( org.freedesktop.PackageKit.Transaction.Package p ) {
            updateProgress();
            try {
                getListener().havePackage(p);
            }
            catch ( PackageKitException e ) {
                error(e);
            }

        }

    }

    private final class PackageKitDetailsHandler implements DBusSigHandler<Details> {

        /**
         * 
         */
        public PackageKitDetailsHandler () {}


        /**
         * {@inheritDoc}
         *
         * @see org.freedesktop.dbus.DBusSigHandler#handle(org.freedesktop.dbus.DBusSignal)
         */
        @Override
        public void handle ( Details details ) {
            updateProgress();
            try {
                getListener().havePackageDetails(details);
            }
            catch ( PackageKitException e ) {
                error(e);
            }
        }

    }

    private final class PackageKitCategoryHandler implements DBusSigHandler<Category> {

        /**
         * 
         */
        public PackageKitCategoryHandler () {}


        /**
         * {@inheritDoc}
         *
         * @see org.freedesktop.dbus.DBusSigHandler#handle(org.freedesktop.dbus.DBusSignal)
         */
        @Override
        public void handle ( Category category ) {
            updateProgress();
            try {
                getListener().haveCategory(category);
            }
            catch ( PackageKitException e ) {
                error(e);
            }
        }

    }

    private final class PackageKitRepoDetailHandler implements DBusSigHandler<RepoDetail> {

        /**
         * 
         */
        public PackageKitRepoDetailHandler () {}


        /**
         * {@inheritDoc}
         *
         * @see org.freedesktop.dbus.DBusSigHandler#handle(org.freedesktop.dbus.DBusSignal)
         */
        @Override
        public void handle ( RepoDetail detail ) {
            updateProgress();
            try {
                getListener().haveRepoDetail(detail);
            }
            catch ( PackageKitException e ) {
                error(e);
            }
        }
    }

    private final class PackageKitRepoSignatureRequiredHandler implements DBusSigHandler<RepoSignatureRequired> {

        /**
         * 
         */
        public PackageKitRepoSignatureRequiredHandler () {}


        /**
         * {@inheritDoc}
         *
         * @see org.freedesktop.dbus.DBusSigHandler#handle(org.freedesktop.dbus.DBusSignal)
         */
        @Override
        public void handle ( RepoSignatureRequired repoSigReq ) {
            updateProgress();
            try {
                getListener().needRepoSignature(repoSigReq);
            }
            catch ( PackageKitException e ) {
                error(e);
            }
        }
    }

    private final class PackageKitEulaRequiredHandler implements DBusSigHandler<EulaRequired> {

        /**
         * 
         */
        public PackageKitEulaRequiredHandler () {}


        /**
         * {@inheritDoc}
         *
         * @see org.freedesktop.dbus.DBusSigHandler#handle(org.freedesktop.dbus.DBusSignal)
         */
        @Override
        public void handle ( EulaRequired eulaReq ) {
            updateProgress();
            try {
                getListener().needEULA(eulaReq);
            }
            catch ( PackageKitException e ) {
                error(e);
            }
        }
    }

    private final class PackageKitMediaChangeRequiredHandler implements DBusSigHandler<MediaChangeRequired> {

        /**
         * 
         */
        public PackageKitMediaChangeRequiredHandler () {}


        /**
         * {@inheritDoc}
         *
         * @see org.freedesktop.dbus.DBusSigHandler#handle(org.freedesktop.dbus.DBusSignal)
         */
        @Override
        public void handle ( MediaChangeRequired mediaChangeReq ) {
            updateProgress();
            try {
                getListener().needMediaChange(mediaChangeReq);
            }
            catch ( PackageKitException e ) {
                error(e);
            }
        }

    }

    private final class PackageKitRequireRestartHandler implements DBusSigHandler<RequireRestart> {

        /**
         * 
         */
        public PackageKitRequireRestartHandler () {}


        /**
         * {@inheritDoc}
         *
         * @see org.freedesktop.dbus.DBusSigHandler#handle(org.freedesktop.dbus.DBusSignal)
         */
        @Override
        public void handle ( RequireRestart restartReq ) {
            updateProgress();
            try {
                getListener().needRestart(restartReq);
            }
            catch ( PackageKitException e ) {
                error(e);
            }
        }
    }

    private final class PackageKitUpdateDetailHandler implements DBusSigHandler<UpdateDetail> {

        /**
         * 
         */
        public PackageKitUpdateDetailHandler () {}


        /**
         * {@inheritDoc}
         *
         * @see org.freedesktop.dbus.DBusSigHandler#handle(org.freedesktop.dbus.DBusSignal)
         */
        @Override
        public void handle ( UpdateDetail updateDetail ) {
            updateProgress();
            try {
                getListener().haveUpdateDetail(updateDetail);
            }
            catch ( PackageKitException e ) {
                error(e);
            }
        }
    }

    private final class PackageKitDistroUpgradeHandler implements DBusSigHandler<DistroUpgrade> {

        /**
         * 
         */
        public PackageKitDistroUpgradeHandler () {}


        /**
         * {@inheritDoc}
         *
         * @see org.freedesktop.dbus.DBusSigHandler#handle(org.freedesktop.dbus.DBusSignal)
         */
        @Override
        public void handle ( DistroUpgrade distroUpdate ) {
            updateProgress();
            try {
                getListener().haveDistroUpdate(distroUpdate);
            }
            catch ( PackageKitException e ) {
                error(e);
            }
        }
    }

    private final class PackageKitItemProgressHandler implements DBusSigHandler<ItemProgress> {

        /**
         * 
         */
        public PackageKitItemProgressHandler () {}


        /**
         * {@inheritDoc}
         *
         * @see org.freedesktop.dbus.DBusSigHandler#handle(org.freedesktop.dbus.DBusSignal)
         */
        @Override
        public void handle ( ItemProgress itemProgress ) {
            updateProgress();
            try {
                getListener().haveItemProgress(itemProgress);
            }
            catch ( PackageKitException e ) {
                error(e);
            }
        }

    }

}
