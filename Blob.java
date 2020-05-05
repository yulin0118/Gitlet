package gitlet;

import java.io.File;
import java.io.Serializable;

import static java.lang.System.getProperty;

/** Class of the blobs tracked by this gitlet.
 *  @author Yulin Li
 */
public class Blob implements Serializable {

    /** The name of the blob. */
    private String _name;
    /** The id of the blob. */
    private String _id;
    /** The content of the blob as a string. */
    private String _content;

    /** The CWD file. */
    public static final File CWD = new File(getProperty("user.dir"));
    /** The main file. */
    public static final File MAINFILE = new File(
            CWD.toString() + "/.gitlet");
    /** The commit file. */
    public static final File COMMITFILE = new File(
            MAINFILE.toString() + "/.commits");
    /** The blob file. */
    public static final File BLOBFILE = new File(
            MAINFILE.toString() + "/.blobs");
    /** The stage file. */
    public static final File STAGEFILE = new File(
            MAINFILE.toString() + "/.stage");


    /**
     * Constructor for a blob.
     * @param name the name of the file stored in this blob.
     * @param content the content of the blob taken as a string.
     */
    public Blob(String name, String content) {
        _name = name;
        _content = content;
        _id = Utils.sha1(name + content);
    }

    /**
     * Write a blob in disk.
     */
    public void writeBlob() {
        Utils.writeObject(new File(
                BLOBFILE.toString() + "/" + this._id), this);
    }

    /**
     * read the blob back into file, using its id as an identifier.
     * @param id the Id of the blob to be read.
     * @return the file containing the blob requested.
     */
    public static Blob readBlob(String id) {
        return Utils.readObject(new File(
                BLOBFILE.toString() + "/" + id), Blob.class);
    }

    /**
     * getter method for the blob's id.
     * @return the id.
     */
    public String getID() {
        return _id;
    }

    /**
     * getter method for the blob's content.
     * @return the the content.
     */
    public String getContent() {
        return _content;
    }
}
