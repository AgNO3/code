/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.02.2015 by mbechler
 */
package eu.agno3.fileshare.model.tokens;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public class AnonymousGrantToken extends SessionIntentToken {

    /**
     * 
     */
    private static final long serialVersionUID = -8513177078151997391L;

    private byte[] nonce;

    private String password;


    /**
     * @param nonce
     * @param withIntentSessionId
     * 
     */
    public AnonymousGrantToken ( byte[] nonce, Serializable withIntentSessionId ) {
        super(withIntentSessionId);
        this.nonce = nonce;
    }


    /**
     * @param nonce
     * @param withIntentSessionId
     * @param password
     * 
     */
    public AnonymousGrantToken ( byte[] nonce, Serializable withIntentSessionId, String password ) {
        super(withIntentSessionId);
        this.nonce = nonce;
        this.password = password;
    }


    /**
     * 
     */
    public AnonymousGrantToken () {}


    /**
     * @return the nonce
     */
    public byte[] getNonce () {
        return this.nonce;
    }


    /**
     * @return the password
     */
    public String getPassword () {
        return this.password;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.tokens.SessionIntentToken#writeExternal(java.io.ObjectOutput)
     */
    @Override
    public void writeExternal ( ObjectOutput oos ) throws IOException {
        super.writeExternal(oos);
        if ( this.nonce != null ) {
            oos.writeByte(this.nonce.length);
            oos.write(this.nonce);
        }
        else {
            oos.writeByte(0);
        }

        oos.writeBoolean(this.password != null);
        if ( this.password != null ) {
            oos.writeUTF(this.password);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.tokens.SessionIntentToken#readExternal(java.io.ObjectInput)
     */
    @Override
    public void readExternal ( ObjectInput ois ) throws ClassNotFoundException, IOException {
        super.readExternal(ois);
        byte len = ois.readByte();
        if ( len > 0 ) {
            byte n[] = new byte[len];
            ois.readFully(n);
            this.nonce = n;
        }
        if ( ois.readBoolean() ) {
            this.password = ois.readUTF();
        }
    }

}
