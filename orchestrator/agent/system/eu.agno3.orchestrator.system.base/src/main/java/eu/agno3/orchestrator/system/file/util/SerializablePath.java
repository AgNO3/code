/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.file.util;


import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;


/**
 * @author mbechler
 *
 */
public class SerializablePath implements Path, Externalizable {

    private transient Path delegate;


    /**
     * 
     */
    public SerializablePath () {}


    /**
     * @param delegate
     * 
     */
    private SerializablePath ( Path delegate ) {
        this.delegate = delegate;
    }


    @Override
    public void forEach ( Consumer<? super Path> action ) {
        this.delegate.forEach(action);
    }


    @Override
    public Spliterator<Path> spliterator () {
        return this.delegate.spliterator();
    }


    @Override
    public FileSystem getFileSystem () {
        return this.delegate.getFileSystem();
    }


    @Override
    public boolean isAbsolute () {
        return this.delegate.isAbsolute();
    }


    @Override
    public Path getRoot () {
        return this.delegate.getRoot();
    }


    @Override
    public Path getFileName () {
        return this.delegate.getFileName();
    }


    @Override
    public Path getParent () {
        return this.delegate.getParent();
    }


    @Override
    public int getNameCount () {
        return this.delegate.getNameCount();
    }


    @Override
    public Path getName ( int index ) {
        return this.delegate.getName(index);
    }


    @Override
    public Path subpath ( int beginIndex, int endIndex ) {
        return this.delegate.subpath(beginIndex, endIndex);
    }


    @Override
    public boolean startsWith ( Path other ) {
        return this.delegate.startsWith(other);
    }


    @Override
    public boolean startsWith ( String other ) {
        return this.delegate.startsWith(other);
    }


    @Override
    public boolean endsWith ( Path other ) {
        return this.delegate.endsWith(other);
    }


    @Override
    public boolean endsWith ( String other ) {
        return this.delegate.endsWith(other);
    }


    @Override
    public Path normalize () {
        return this.delegate.normalize();
    }


    @Override
    public Path resolve ( Path other ) {
        return this.delegate.resolve(other);
    }


    @Override
    public Path resolve ( String other ) {
        return this.delegate.resolve(other);
    }


    @Override
    public Path resolveSibling ( Path other ) {
        return this.delegate.resolveSibling(other);
    }


    @Override
    public Path resolveSibling ( String other ) {
        return this.delegate.resolveSibling(other);
    }


    @Override
    public Path relativize ( Path other ) {
        return this.delegate.relativize(other);
    }


    @Override
    public URI toUri () {
        return this.delegate.toUri();
    }


    @Override
    public Path toAbsolutePath () {
        return this.delegate.toAbsolutePath();
    }


    @Override
    public Path toRealPath ( LinkOption... options ) throws IOException {
        return this.delegate.toRealPath(options);
    }


    @Override
    public File toFile () {
        return this.delegate.toFile();
    }


    @Override
    public WatchKey register ( WatchService watcher, Kind<?>[] events, Modifier... modifiers ) throws IOException {
        return this.delegate.register(watcher, events, modifiers);
    }


    @Override
    public WatchKey register ( WatchService watcher, Kind<?>... events ) throws IOException {
        return this.delegate.register(watcher, events);
    }


    @Override
    public Iterator<Path> iterator () {
        return this.delegate.iterator();
    }


    @Override
    public int compareTo ( Path other ) {
        return this.delegate.compareTo(other);
    }


    /**
     * @return the unwrapped path
     */
    public Path unwrap () {
        return this.delegate;
    }


    @Override
    public boolean equals ( Object other ) {
        if ( other instanceof SerializablePath ) {
            this.delegate.equals( ( (SerializablePath) other ).unwrap());
        }
        else if ( other instanceof Path ) {
            this.delegate.equals(other);
        }

        return this.delegate.equals(other);
    }


    @Override
    public int hashCode () {
        return this.delegate.hashCode();
    }


    @Override
    public String toString () {
        return this.delegate.toString();
    }


    /**
     * @param p
     * @return a wrapped path
     */
    public static final SerializablePath wrap ( Path p ) {
        if ( p == null ) {
            return null;
        }
        return new SerializablePath(p);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    @Override
    public void writeExternal ( ObjectOutput out ) throws IOException {
        out.writeObject(this.delegate.toUri());
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    @Override
    public void readExternal ( ObjectInput in ) throws IOException, ClassNotFoundException {
        URI u = (URI) in.readObject();
        this.delegate = Paths.get(u);
    }
}
