package gitlet;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import static java.lang.System.getProperty;

/** Class the staging areas.
 *  @author Yulin Li
 */
public class Stage implements Serializable {
    /**HashMap representing the Blobs to be added.
     * key: name of the file
     * value: the id of the blob
     */
    private HashMap<String, String> addingStage = new HashMap<>();
    /**HashMap representing the Blobs to be removed.
     * key: name of the file
     * value: the id of the blob
     */
    private HashMap<String, String> removingStage = new HashMap<>();

    /** The current working directory. */
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
     * add the blob to the adding stage.
     * @param name the name of the file.
     * @param blobID the id of the blob.
     */
    public void addToAddingStage(String name, String blobID) {
        addingStage.put(name, blobID);
    }

    /**
     * getter method for the adding stage.
     * @return the adding stage.
     */
    public HashMap<String, String> getAddingStage() {
        return addingStage;
    }

    /**
     * getter method for the adding stage.
     * @return the removing stage.
     */
    public HashMap<String, String> getRemovingStage() {
        return removingStage;
    }

    /** Write the stage in disk. */
    public void writeStage() {
        Utils.writeObject(new File(STAGEFILE.toString()
                + "/" + "mainStage"), this);
    }

    /**
     * Reads the stage from disk.
     * @param fileName the name of the stage.
     * @return the stage object requested.
     */
    public static Stage readStage(String fileName) {
        return Utils.readObject(new File(STAGEFILE.toString()
                + "/" + fileName), Stage.class);
    }
}
