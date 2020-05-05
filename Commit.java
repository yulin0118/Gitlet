package gitlet;
import java.io.File;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;

import static java.lang.System.getProperty;

/** Class of commits to store the commits made.
 *  @author Yulin Li
 */

public class Commit implements Serializable, Cloneable {

    /** The message of the commit. */
    private String _message;
    /** The date of the commit. */
    private String _date;
    /** The parent's id of the commit. */
    private String _parent;
    /** The id of the commit. */
    private String _id;
    /** The map representing its blobs. */
    private HashMap<String, String> currBlobs = new HashMap<>();
    /** Second parent after merge. */
    private String _secondParent = null;

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
     * Constructor of the commit class.
     * @param msg the message of the commit.
     * @param parent the parent of the commit class.
     */
    public Commit(String msg, String parent) {
        this._message = msg;
        this._parent = parent;
        this._date = "Wed Dec 31 16:00:00 1969 -0800";
        String idtext = msg + _parent + currBlobs.toString() + _date;
        _id = Utils.sha1(idtext);
        writeCommit();
    }

    /**
     * Constructor of the commit class.
     * @param msg the message of the commit.
     * @param parent the parent of the commit.
     * @param toAdd the HashMap of of the collection of the blobs to add.
     * @param toRemove the HashMap of of the collection of the blobs to remove.
     */
    public Commit(String msg, String parent,
                   HashMap<String, String> toAdd,
                   HashMap<String, String> toRemove) {
        this._message = msg;
        this._parent = parent;
        ZonedDateTime date = ZonedDateTime.now();
        this._date = date.format(DateTimeFormatter.ofPattern(
                "EEE MMM d HH:mm:ss yyyy Z", Locale.US));
        currBlobs = getParentCommit().getCurrBlobs();
        if (toAdd != null) {
            for (String key : toAdd.keySet()) {
                if (currBlobs.containsKey(key)) {
                    currBlobs.put(key, toAdd.get(key));
                } else {
                    currBlobs.put(key, toAdd.get(key));
                }
            }
        }
        if (toRemove != null) {
            for (String key : toRemove.keySet()) {
                currBlobs.remove(key);
            }
        }
        String idtext = msg + _parent + currBlobs.toString() + _date;
        _id = Utils.sha1(idtext);
        writeCommit();
    }

    /**
     * Produces the string representing the commit in the log.
     * @return the string to be printed in the log.
     */
    public String logString() {
        String result =
                "==="
                + System.lineSeparator() + "commit "
                        + this.getID() + System.lineSeparator()
                        + "Date: " + this._date
                        + System.lineSeparator()
                        + this._message + System.lineSeparator();
        return result;
    }

    /** Write the commit to disk. */
    public void writeCommit() {
        Utils.writeObject(new File(COMMITFILE.toString()
                + "/" + this.getID()), this);
    }

    /**
     * Returns the commit file as requested.
     * @param id the id of the desired commit.
     * @return the requested commit object.
     */
    public Commit readCommit(String id) {
        return Utils.readObject(new File(
                COMMITFILE.toString() + "/" + id), this.getClass());
    }

    /**
     * returns the message of the commit.
     * @return the message.
     */
    public String getMsg() {
        return this._message;
    }

    /**
     * get the other parent.
     * @return the second parent as a commit.
     */
    public Commit getOtherParent() {
        if (_secondParent == null) {
            return null;
        } else {
            return Utils.readObject(new File(COMMITFILE.toString()
                    + "/" + _secondParent), Commit.class);
        }
    }

    /**
     * get the parent commit.
     * @return the parent commit.
     */
    public Commit getParentCommit() {
        if (_parent == null) {
            return null;
        } else {
            return Utils.readObject(new File(COMMITFILE.toString()
                    + "/" + _parent), Commit.class);
        }
    }

    /**
     * Return the ID of this commit.
     * @return the ID.
     */
    public String getID() {
        return _id;
    }

    /**
     * Getter method of thr blobs tracked by this commit.
     * @return the hashmap of the blobs.
     */
    public HashMap<String, String> getCurrBlobs() {
        return currBlobs;
    }

    /**
     * Getter method of thr blobs tracked by this commit.
     * @return the hashmap of the blobs.
     */
    public HashMap<String, String> getBlobs() {
        return currBlobs;
    }

    /**
     * add the second parent for the commit.
     * @param id the id of the second commit.
     */
    public void addParent(String id) {
        _secondParent = id;
    }

    /**
     * Sets the parent of this commit to id.
     * @param id the id of the parent commit.
     */
    public void setParent(String id) {
        _parent = id;
    }
}
