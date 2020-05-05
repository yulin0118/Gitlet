package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.System.getProperty;

/** Class representing the current commit tree.
 *  @author Yulin Li
 */

public class CommitTree implements Serializable {
    /** The map representing the branches ever made and the corresponding
     * head commit's ID for that branch.*/
    private HashMap<String, String> branches;
    /** The list of all the commits ever made, stored by their ID. */
    private ArrayList<String> allCommits;
    /** The unique ID of the head commit.*/
    private String head;
    /** The name of the current Branch. */
    private String currBranch;
    /** The initial commit shared by every branch in the repository. */
    private Commit initialCommit;
    /** The array list of all the commits in the given branch for merge.*/
    private ArrayList<String> allCommitInOther = new ArrayList<>();
    /** The found split commit. */
    private Commit foundSplit;

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
    /** The branches file. */
    private static File branchesFile = new File(
            MAINFILE.toString() + "/" + "branches");

    /** The constructor of the CommitTree class.*/
    public CommitTree() {
    }

    /**
     * Creates a gitlet repository and set up the initial commit.
     */
    public void init() {
        if (MAINFILE.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            return;
        }
        makeDir();
        Stage mainStage = new Stage();
        mainStage.writeStage();
        Commit initial = new Commit("initial commit", null);
        initialCommit = initial;
        allCommits = new ArrayList<>();
        allCommits.add(initial.getID());
        branches = new HashMap<String, String>();
        branches.put("master", initial.getID());
        currBranch = "master";
        head = initial.getID();
        Utils.writeObject(branchesFile, branches);
    }

    /**
     * Make all of the directories necessary for this git repository.
     */
    public void makeDir() {
        MAINFILE.mkdir();
        COMMITFILE.mkdir();
        BLOBFILE.mkdir();
        STAGEFILE.mkdir();
    }

    /** Adds a copy of a certain file to the staging area to be added.
     * @param fileName the name of the file being added */
    public void add(String fileName) {
        Stage mainStage = Stage.readStage("mainStage");
        mainStage.getRemovingStage().remove(fileName);
        File fileToAdd = new File(fileName);
        if (!fileToAdd.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        Blob thisBlob = new Blob(
                fileName, Utils.readContentsAsString(fileToAdd));
        Commit currCommit = getCurrCommit();
        mainStage.getRemovingStage().remove(fileName);
        mainStage.writeStage();
        if (currCommit.getBlobs().containsValue(thisBlob.getID())) {
            if (mainStage.getAddingStage().containsValue(thisBlob.getID())) {
                mainStage.getAddingStage().remove(fileName);
                mainStage.getRemovingStage().remove(fileName);
                mainStage.writeStage();
            }
            return;
        }
        thisBlob.writeBlob();
        mainStage.addToAddingStage(fileName, thisBlob.getID());
        mainStage.writeStage();
    }

    /**
     * Returns the current commit.
     * @return the current commit that the head is pointing to.
     */
    public Commit getCurrCommit() {
        return Utils.readObject(new File(COMMITFILE.toString()
                + "/" + head), Commit.class);
    }

    /** Make a new commit.
     * @param msg the commit message passed in by the user
     * create a new commit
     * clone the its parent
     * if already in */
    public void commit(String msg) {
        Stage mainStage = Stage.readStage("mainStage");
        HashMap<String, String> toAdd = mainStage.getAddingStage();
        HashMap<String, String> toRemove = mainStage.getRemovingStage();
        if (toAdd.isEmpty() && toRemove.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        if (msg.isEmpty()) {
            System.out.println("Please enter a commit message");
            return;
        }
        Commit currCommit = getCurrCommit();
        Commit newCommit = new Commit(msg, currCommit.getID(),
                mainStage.getAddingStage(), mainStage.getRemovingStage());
        allCommits.add(newCommit.getID());
        mainStage.getAddingStage().clear();
        mainStage.getRemovingStage().clear();
        branches.replace(currBranch, newCommit.getID());
        Utils.writeObject(branchesFile, branches);
        updateHead(currBranch);
        mainStage.writeStage();
    }

    /**
     * Updates HEAD to the latest commit ID of BRANCH.
     * @param branch the name of the current branch.
     */

    public void updateHead(String branch) {
        head = branches.get(branch);
    }
    /**
     *Stages a file for removal.
     * @param fileName the name of the file.
     */
    public void remove(String fileName) {
        Stage mainStage = Stage.readStage("mainStage");
        if (mainStage.getAddingStage().containsKey(fileName)) {
            mainStage.getAddingStage().remove(fileName);
            mainStage.writeStage();
        } else if (getCurrCommit().getBlobs().containsKey(fileName)) {
            mainStage.getRemovingStage().put(
                    fileName, getCurrCommit().getBlobs().get(fileName));
            mainStage.writeStage();
            new File(CWD.toString() + "/" + fileName).delete();
        } else {
            System.out.println("No reason to remove the file.");
        }
    }

    /**
     * Prints the history from the current head commit.
     * This is done recursively.
     */
    public void log() {
        File headFile = new File(MAINFILE.toString() + "/head.txt");
        if (headFile.exists()) {
            String commitStr = Utils.readContentsAsString(headFile);
            printLog(commitStr);
        } else {
            printLog(getCurrCommit());
        }
        printInitial();
    }

    /**
     * Prints the log for all of the commits ever made in this repository.
     * This is done through iterating everything in the allCommits arrayList.
     */
    public void globalLog() {
        for (String currCommitStr : allCommits) {
            Commit currCommit = Utils.readObject(new File(
                    COMMITFILE.toString() + "/" + currCommitStr), Commit.class);
            System.out.println(currCommit.logString());
        }
    }

    /**
     * print out the log as requested.
     * @param currCommit the current commit.
     */
    public void printLog(Commit currCommit) {
        while (currCommit.getParentCommit() != null) {
            System.out.println(currCommit.logString());
            currCommit = currCommit.getParentCommit();
        }
    }

    /**
     * print out the log as requested.
     * @param currCommitStr the current commit id.
     */
    public void printLog(String currCommitStr) {
        Commit currCommit = Utils.readObject(new File(
                COMMITFILE.toString()
                        + "/" + currCommitStr), Commit.class);
        while (currCommit.getParentCommit() != null) {
            System.out.println(currCommit.logString());
            currCommit = currCommit.getParentCommit();
        }
    }

    /**
     * Prints the log of the initial commit.
     */
    public void printInitial() {
        System.out.println(initialCommit.logString());
    }


    /**
     * Find and prints the IDs of the commits that have the commit MESSAGE msg.
     * @param msg the commit message to look for.
     */
    public void find(String msg) {
        if (msg.equals("initial commit")) {
            System.out.println(initialCommit.getID());
        } else {
            boolean found = false;
            for (String currCommitStr : allCommits) {
                Commit currCommit = Utils.readObject(new File(
                        COMMITFILE.toString()
                                + "/" + currCommitStr), Commit.class);
                if (currCommit.getMsg().equals(msg)) {
                    System.out.println(currCommit.getID());
                    found = true;
                }
            }
            if (!found) {
                System.out.println("Found no commit with that message.");
            }
        }
        System.out.println();
    }

    /**
     * prints the status of the current commit tree.
     */
    public void status() {
        printBranches();
        printAddingStaged();
        printRemovedStage();
        printModi();
        printUntracked();
    }

    /**
     * prints the branches.
     */
    public void printBranches() {
        System.out.println("=== Branches ===");
        ArrayList<String> branchNames =
                new ArrayList<String>(branches.keySet());
        branchNames = orderedList(branchNames);
        for (String str : branchNames) {
            if (str.equals(currBranch)) {
                System.out.println("*" + str);
            } else {
                System.out.println(str);
            }
        }
        System.out.println();
    }

    /**
     * prints the modified files.
     */
    public void printModi() {
        Stage mainStage = Stage.readStage("mainStage");
        for (String fileName : mainStage.getAddingStage().keySet()) {
            File thisFile = new File(CWD.toString()
                    + "/" + fileName);
            if (!thisFile.exists() && !fileName.equals(".gitlet")
                    && !fileName.equals("branches")) {
                deleted.add(fileName);
            } else {
                Blob thisBlob = Blob.readBlob(
                        mainStage.getAddingStage().get(fileName));
                if (!thisBlob.getContent().equals(
                        Utils.readContentsAsString(thisFile))) {
                    if (!fileName.equals(".gitlet")
                            && !fileName.equals("branches")) {
                        modified.add(fileName);
                    }
                }
            }
        }
        for (String fileName : getCurrCommit().getBlobs().keySet()) {
            if (!mainStage.getRemovingStage().containsKey(fileName)
                    && !fileName.equals(".gitlet")
                    && !new File(CWD.toString()
                    + "/" + fileName).exists()) {
                deleted.add(fileName);
            } else {
                if (!mainStage.getAddingStage().containsKey(fileName)
                        && !fileName.equals(".gitlet")) {
                    File thisFile = new File(CWD.toString()
                            + "/" + fileName);
                    Blob thisBlob = Blob.readBlob(
                            getCurrCommit().getBlobs().get(fileName));
                    if (thisFile.exists() && !thisBlob.getContent().equals(
                            Utils.readContentsAsString(thisFile))) {
                        modified.add(fileName);
                    }
                }
            }
        }
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String str : deleted) {
            System.out.println(str + " (deleted)\n");
        }
        for (String str : modified) {
            System.out.println(str + " (modified)\n");
        }
        if (deleted.isEmpty() && modified.isEmpty()) {
            System.out.println();
        }
        deleted.clear();
        modified.clear();
    }

    /**The array list of all the deleted files for the modified section
     * of status. */
    private ArrayList<String> deleted = new ArrayList<>();

    /**The array list of all the deleted files for the modified section
     * of status. */
    private ArrayList<String> modified = new ArrayList<>();

    /**
     * prints the untracked files.
     */
    public void printUntracked() {
        String[] allFileNames = CWD.list();
        ArrayList<String> untracked = new ArrayList<>();
        Stage mainStage = Stage.readStage("mainStage");
        for (String fileName : allFileNames) {
            if (!getCurrCommit().getBlobs().containsKey(fileName)
                    && !mainStage.getAddingStage().containsKey(fileName)) {
                if (!fileName.equals(".gitlet")
                        && !fileName.equals("branches")) {
                    untracked.add(fileName);
                }
            }
        }
        System.out.println("=== Untracked Files ===");
        for (String fileName : untracked) {
            System.out.println(fileName + "\n");
        }
        if (untracked.isEmpty()) {
            System.out.println();
        }
        untracked.clear();
    }

    /**
     * prints the adding stage.
     */
    public void printAddingStaged() {
        System.out.println("=== Staged Files ===");
        Stage mainStage = Stage.readStage("mainStage");
        ArrayList<String> fileNames = new ArrayList<String>(
                mainStage.getAddingStage().keySet());
        mainStage.writeStage();
        printOrderedList(fileNames);
    }

    /**
     * prints the removing stage.
     */
    public void printRemovedStage() {
        System.out.println("=== Removed Files ===");
        Stage mainStage = Stage.readStage("mainStage");
        ArrayList<String> fileNames = new ArrayList<String>(
                mainStage.getRemovingStage().keySet());
        mainStage.writeStage();
        printOrderedList(fileNames);
    }

    /**
     * print the list in order.
     * @param lst the given list.
     */
    public void printOrderedList(ArrayList<String> lst) {
        lst = orderedList(lst);
        for (String str : lst) {
            System.out.println(str);
        }
        System.out.println();
    }

    /**
     * Get the list ordered.
     * @param lst the given list.
     * @return the ordered list.
     */
    public ArrayList<String> orderedList(ArrayList<String> lst) {
        ArrayList<String> ordered = new ArrayList<String>();
        for (int i = 0; i < lst.size(); i++) {
            String str = lst.get(i);
            for (int j = 1; j < lst.size(); j++) {
                if (str.compareTo(lst.get(j)) > 0) {
                    str = lst.get(j);
                }
            }
            i = -1;
            ordered.add(str);
            lst.remove(str);
        }
        return ordered;
    }

    /**
     * Checkout a commit by the file name.
     * @param fileName the name of the file to be checked out.
     */
    public void checkoutFileName(String fileName) {
        Commit currCommit = getCurrCommit();
        writeInCWD(fileName, currCommit);
    }

    /**
     * Write the file with FILENAME in the COMMIT currCommit back in the CWD.
     * @param fileName name of the file.
     * @param currCommit the COMMIT referred to.
     */
    private void writeInCWD(String fileName, Commit currCommit) {
        String blobID = currCommit.getBlobs().get(fileName);
        if (blobID == null) {
            System.out.println("File does not exist in that commit.");
        } else {
            Blob thisBlob = Blob.readBlob(blobID);
            File targetFile = new File(CWD.toString()
                    + "/" + fileName);
            Utils.writeContents(targetFile, thisBlob.getContent());
        }
    }

    /**
     * Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory, overwriting the versions
     * of the files that are already there if they exist. Also, at the
     * end of this command, the given branch will now be considered the
     * current branch (HEAD). Any files that are tracked in the current
     * branch but are not present in the checked-out branch are deleted.
     * The staging area is cleared, unless the checked-out branch is the
     * current branch
     * @param branchName the name of the desired BRANCH.
     */
    public void checkoutBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
        } else if (branchName.equals(currBranch)) {
            System.out.println("No need to checkout the current branch.");
        } else {
            String ckBranch = branches.get(branchName);
            Commit ckCommit = Utils.readObject(new File(COMMITFILE.toString()
                    + "/" + ckBranch), Commit.class);
            Commit currCommit = getCurrCommit();
            if (!checkOverRide(ckBranch)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
            } else {
                overrideCurrCommit(ckCommit, currCommit);
                currBranch = branchName;
                branches.replace(branchName, ckCommit.getID());
                Utils.writeObject(branchesFile, branches);
                head = ckCommit.getID();
            }
        }
    }

    /**
     * check if COMMIT ck includes tracks anything not in current commit
     * but in current working directory.
     * @param ck name of the checkout branch.
     * @return if this is true.
     */
    public boolean checkOverRide(String ck) {
        Commit ckCommit = Utils.readObject(new File(COMMITFILE.toString()
                + "/" + ck), Commit.class);
        Commit currCommit = getCurrCommit();
        for (String fileNameCk : ckCommit.getBlobs().keySet()) {
            if (!currCommit.getBlobs().containsKey(fileNameCk)
                    && new File(CWD.toString()
                    + "/" + fileNameCk).exists()) {
                return false;
            }
        }
        return true;
    }

    /**
     * check if COMMIT ck includes tracks anything not in current commit
     * but in current working directory.
     * @param ckCommit the commit of to check out.
     * @return if this is true.
     */
    public boolean checkOverRideCm(Commit ckCommit) {
        Commit currCommit = getCurrCommit();
        for (String fileNameCk : ckCommit.getBlobs().keySet()) {
            if (!currCommit.getBlobs().containsKey(fileNameCk)
                    && new File(CWD.toString() + "/"
                    + fileNameCk).exists()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check out a specific file in a commit.
     * @param cmID the ID for the COMMIT that we should search from.
     * @param fileName the name of the FILE we are trying to recover.
     */
    public void ckCommitFile(String cmID, String fileName) {
        Commit foundCommit = null;
        for (String branch : branches.keySet()) {
            Commit currCommit = Utils.readObject(new File(COMMITFILE.toString()
                    + "/" + branches.get(branch)), Commit.class);
            while (currCommit.getParentCommit() != null) {
                if (currCommit.getID().equals(cmID)
                        || currCommit.getID().startsWith(cmID)) {
                    foundCommit = currCommit;
                    break;
                }
                currCommit = currCommit.getParentCommit();
            }
        }
        if (foundCommit == null) {
            System.out.println("No commit with that id exists.");
        } else {
            writeInCWD(fileName, foundCommit);
        }
    }

    /**
     * Creates a new branch.
     * @param branchName The name of the new branch to be added.
     */
    public void branch(String branchName) {
        if (branches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
        } else {
            Commit currCommit = getCurrCommit();
            branches.put(branchName, currCommit.getID());
            Utils.writeObject(branchesFile, branches);
        }
    }

    /**
     * Removes a branch.
     * @param branchName the name of the branch to be removed.
     */
    public void rmBranch(String branchName) {
        if (branchName.equals(currBranch)) {
            System.out.println("Cannot remove the current branch.");
        } else if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
        } else {
            branches.remove(branchName);
        }
    }

    /**
     * Resets to a branch.
     * @param commitID the ID of the commit to reset to.
     */
    public void reset(String commitID) {
        Commit ckCommit = null;
        boolean found = false;
        for (String id : allCommits) {
            if (id.startsWith(commitID)
                        || id.equals(commitID)) {
                ckCommit = Utils.readObject(new File(
                        COMMITFILE.toString() + "/" + id), Commit.class);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No commit with that id exists.");
        } else {
            if (!checkOverRideCm(ckCommit)) {
                System.out.println("There is an untracked file "
                        + "in the way; delete it, or add and commit it first.");
            } else {
                Commit currCommit = getCurrCommit();
                overrideCurrCommit(ckCommit, currCommit);
                branches.put(currBranch, ckCommit.getID());
                Utils.writeObject(branchesFile, branches);
                head = ckCommit.getID();
            }
        }
    }

    /**
     * Deletes the Blobs of the currCommit and write the Blobs of the ckCommit
     * into the Current Working Directory.
     * @param ckCommit the commit being checked out.
     * @param currCommit the current commit.
     */
    private void overrideCurrCommit(Commit ckCommit, Commit currCommit) {
        for (String fileName : currCommit.getBlobs().keySet()) {
            new File(CWD.toString() + "/" + fileName).delete();
        }
        for (String fileName : ckCommit.getBlobs().keySet()) {
            String blobID = ckCommit.getBlobs().get(fileName);
            Blob thisBlob = Blob.readBlob(blobID);
            File targetFile = new File(CWD.toString()
                    + "/" + fileName);
            Utils.writeContents(targetFile, thisBlob.getContent());
        }
        Stage mainStage = Stage.readStage("mainStage");
        mainStage.getAddingStage().clear();
        mainStage.getRemovingStage().clear();
        mainStage.writeStage();
    }

    /** True if this is a conflict merge.*/
    private boolean isConflict = false;

    /**
     * Check if this branch is legit for merge.
     * @param branchName the name of the given branch.
     * @return true only if this is a legit merge to carry on with.
     */
    public boolean isLegitBranch(String branchName) {
        Stage mainStage = Stage.readStage("mainStage");
        if (!mainStage.getAddingStage().isEmpty()
                || !mainStage.getRemovingStage().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return false;
        } else if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return false;
        } else if (branchName.equals(currBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check is this merge is legit.
     * @param current the current commit.
     * @param given the given commit.
     * @param split the split commit.
     * @param givenBranchName the name of the given branch.
     * @return if this is a legit merge.
     */
    private boolean isLegitCommit(Commit current, Commit given,
                                  Commit split, String givenBranchName) {
        if (split.getID().equals(given.getID())) {
            System.out.println("Given branch is an "
                    + "ancestor of the current branch.");
            return false;
        } else if (split.equals(current)) {
            checkoutBranch(givenBranchName);
            System.out.println("Current branch fast-forwarded.");
            return false;
        } else if (!checkOverRideCm(split)) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Merges the current branch with the given branch.
     * @param givenBranchName the name of the branch to be merged with.
     */
    public void merge(String givenBranchName) {
        Stage mainStage = Stage.readStage("mainStage");
        if (!isLegitBranch(givenBranchName)) {
            return;
        }
        Commit current = getCurrCommit();
        Commit given =  Utils.readObject(new File(COMMITFILE.toString()
                + "/" + branches.get(givenBranchName)), Commit.class);
        if (!checkOverRideCm(given)) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            return;
        }
        Commit split = findSplitRecur(current, given);
        if (!isLegitCommit(current, given, split, givenBranchName)) {
            return;
        }
        for (String currFileName : current.getBlobs().keySet()) {
            String currFile = current.getBlobs().get(currFileName);
            String givenFile = given.getBlobs().get(currFileName);
            String splitFile = split.getBlobs().get(currFileName);
            if (currFile.equals(splitFile)) {
                if (givenFile == null) {
                    File target = new File(CWD.toString()
                            + "/" + currFileName);
                    target.delete();
                    mainStage.getRemovingStage().put(currFileName, currFile);
                } else if (!givenFile.equals(splitFile)) {
                    ckCommitFile(given.getID(), currFileName);
                    mainStage.addToAddingStage(currFileName, givenFile);
                }
            } else if (!currFile.equals(givenFile) && splitFile != null) {
                String givencontent = "";
                if (givenFile != null) {
                    givencontent = Blob.readBlob(givenFile).getContent();
                }
                String content = conGiven(currFile, givenFile, givencontent);
                isConflict = makeCon(mainStage, currFileName, content);
            }
        }
        for (String givenFileName : given.getBlobs().keySet()) {
            String splitFile = split.getBlobs().get(givenFileName);
            String currFile = current.getBlobs().get(givenFileName);
            String givenFile = given.getBlobs().get(givenFileName);
            if (split.getBlobs().get(givenFileName) == null
                    && current.getBlobs().get(givenFileName) == null) {
                ckCommitFile(given.getID(), givenFileName);
                mainStage.addToAddingStage(givenFileName,
                        given.getBlobs().get(givenFileName));
            } else if (currFile != null && !currFile.equals(givenFile)
                    && splitFile != null) {
                String content = makeContent(currFile, givenFile);
                isConflict = makeCon(mainStage, givenFileName, content);
            }
        }
        String msg = "Merged " + givenBranchName + " into " + currBranch + ".";
        makeNewCommit(msg, getCurrCommit().getID(),
                mainStage.getAddingStage(), mainStage.getRemovingStage(),
                currBranch, given, isConflict, mainStage);
    }

    /**
     * make content for conflicted merges.
     * @param curr the current commit file.
     * @param given the given commit file.
     * @return the content for the merge file.
     */
    public String makeContent(String curr, String given) {
        String result = "<<<<<<< HEAD\n"
                + Blob.readBlob(curr).getContent() + "=======\n"
                + Blob.readBlob(given).getContent() + ">>>>>>>\n";
        return result;
    }

    /**
     * make content for conflicted merges with given content.
     * @param curr current file.
     * @param given given file.
     * @param content given content.
     * @return content of the merge file.
     */
    public String conGiven(String curr, String given, String content) {
        String result = "<<<<<<< HEAD\n"
                + Blob.readBlob(curr).getContent()
                + "=======\n" + content + ">>>>>>>\n";
        return result;
    }

    /**
     * makes a new commit for the merge method.
     * @param msg the message.
     * @param parent the parent.
     * @param add the adding stage.
     * @param remove the removing stage.
     * @param branch the current branch name.
     * @param given the given branch.
     * @param con is boolean for if it is a conflict file.
     * @param main the stage.
     */
    public void makeNewCommit(String msg, String parent,
                              HashMap<String, String> add,
                              HashMap<String, String> remove,
                              String branch, Commit given, boolean con,
                              Stage main) {
        Commit newCommit = new Commit(msg, parent, add, remove);
        finishCommit(newCommit, given, branch);
        if (con) {
            System.out.println("Encountered a merge conflict.");
        }
        finishStage(main);
    }

    /**
     * finish and write the stage.
     * @param stage the given stage.
     */
    public void finishStage(Stage stage) {
        stage.getAddingStage().clear();
        stage.getRemovingStage().clear();
        stage.writeStage();
    }

    /**
     * Finish the commit.
     * @param cm the commit.
     * @param given the given one in merge.
     * @param branch the current branch.
     */
    public void finishCommit(Commit cm, Commit given, String branch) {
        cm.addParent(given.getID());
        cm.writeCommit();
        allCommits.add(cm.getID());
        branches.replace(branch, cm.getID());
        Utils.writeObject(branchesFile, branches);
        head = cm.getID();
    }

    /**
     * make the conflicted file.
     * @param mainStage the stage.
     * @param currFileName the current file name.
     * @param content the content.
     * @return if it is a conflicted file.
     */
    private boolean makeCon(Stage mainStage,
                            String currFileName, String content) {
        File conflictFile = new File(
                CWD.toString() + "/" + currFileName);
        Utils.writeContents(conflictFile, content);
        Blob thisBlob = new Blob(currFileName,
                Utils.readContentsAsString(conflictFile));
        thisBlob.writeBlob();
        mainStage.addToAddingStage(currFileName, thisBlob.getID());
        isConflict = true;
        return true;
    }

    /**
     * Finds the last common ancestor for the two commits.
     * @param current the current commit.
     * @param given the given commit.
     * @return the found commit.
     */
    public Commit findSplit(Commit current, Commit given) {
        while (current.getParentCommit() != null) {
            Commit givenPointer = given;
            while (givenPointer.getParentCommit() != null) {
                if (current.getID().equals(givenPointer.getID())) {
                    return current;
                }
                givenPointer = givenPointer.getParentCommit();
            }
            current = current.getParentCommit();
        }
        return null;
    }

    /**
     * Setting up the array list of all the commits in the given branch.
     * @param given the head pointer of the given branch.
     */
    public void getAllCommits(Commit given) {
        Commit givenPointer = given;
        while (givenPointer.getParentCommit() != null) {
            allCommitInOther.add(givenPointer.getID());
            if (givenPointer.getOtherParent() != null) {
                getAllCommits(givenPointer.getOtherParent());
            }
            givenPointer = givenPointer.getParentCommit();
        }
    }

    /**
     * A recursive function to find the given commit. Considering
     * both the first and second parents.
     * @param current the current commit.
     * @param given the given commit.
     * @return the found commit.
     */
    public Commit findSplitRecur(Commit current, Commit given) {
        getAllCommits(given);
        allCommitInOther.add(initialCommit.getID());
        findSplitHelper(current, 0);
        return foundSplit;
    }

    /**
     * The tree-recursive function that helps find the split commit.
     * @param curr the current commit.
     * @param n the length of the path.
     * @return the length of the path.
     */
    public int findSplitHelper(Commit curr, Integer n) {
        if (curr == null) {
            return n;
        }
        for (String str : allCommitInOther) {
            if (curr.getID().equals(str)) {
                foundSplit = curr;
                return n;
            }
        }
        return Math.min(findSplitHelper(curr.getParentCommit(), n + 1),
                findSplitHelper(curr.getOtherParent(), n + 1));
    }

    /**
     * The HashMap mapping the remote names to their path.
     */
    private HashMap<String, File> remotes = new HashMap<>();

    /**
     * Adds a remote repository.
     * @param remoteName the name of the remote.
     * @param remoteDir the string for the path.
     */
    public void addRemote(String remoteName, String remoteDir) {
        if (remotes.containsKey(remoteName)) {
            System.out.println("A remote with that name already exists.");
            return;
        }
        String pathName = remoteDir.replace("/", java.io.File.separator);
        remotes.put(remoteName, new File(pathName));
    }

    /**
     * Removes a remote repository.
     * @param remoteName the name of the repository.
     */
    public void rmRemote(String remoteName) {
        if (!remotes.containsKey(remoteName)) {
            System.out.println("A remote with that name does not exist.");
        } else {
            remotes.remove(remoteName);
        }
    }

    /**
     * pushes the local commits to the given branch of the given remote.
     * @param remoteName the name of the remote repo.
     * @param remoteBranch the name of the remote branch.
     */
    public void push(String remoteName, String remoteBranch) {
        File remoteFile = remotes.get(remoteName);
        if (!remoteFile.exists()) {
            System.out.println("Remote directory not found.");
            return;
        }
        HashMap<String, String> something = new HashMap<String, String>();
        HashMap remoteBranches =
                Utils.readObject(new File(remoteFile.toString() + "/branches"),
                        something.getClass());
        Commit currPointer = getCurrCommit();
        String remoteHeadStr = (String) remoteBranches.get(remoteBranch);
        Commit remoteHead = Utils.readObject(new File(
                remoteFile.toString()
                        + "/.commits/" + remoteHeadStr), Commit.class);
        boolean found = false;
        while (currPointer.getParentCommit() != null) {
            if (currPointer.getID().equals(remoteHead.getID())) {
                found = true;
                break;
            } else {
                currPointer = currPointer.getParentCommit();
            }
        }
        if (found) {
            appendCommits(remoteHead, remoteFile);
            File headFile = new File(remoteFile.toString() + "/head.txt");
            Utils.writeContents(headFile, getCurrCommit().getID());
        } else {
            System.out.println("Please pull down "
                    + "remote changes before pushing.");
        }
    }

    /**
     * Brings down commits from the remote Gitlet repository
     * into the local Gitlet repository.
     * @param remoteName the name of the remote.
     * @param remoteBranch the name of the remote branch.
     */
    public void fetch(String remoteName, String remoteBranch) {
        File remoteFile = remotes.get(remoteName);
        if (!remoteFile.exists()) {
            System.out.println("Remote directory not found.");
            return;
        }
        HashMap remoteBranches =
                Utils.readObject(new File(remoteFile.toString() + "/branches"),
                        branches.getClass());
        if (remoteBranches.get(remoteBranch) == null) {
            System.out.println("That remote does not have that branch.");
        } else {
            String remoteHeadStr = (String) remoteBranches.get(remoteBranch);
            Commit remoteHead = Utils.readObject(new File(
                    remoteFile.toString()
                            + "/.commits/" + remoteHeadStr), Commit.class);
            String newBranch = remoteName + "/" + remoteBranch;
            Commit remotePointer = remoteHead;
            while (remotePointer.getParentCommit() != null) {
                if (!allCommits.contains(remotePointer.getID())) {
                    allCommits.add(remotePointer.getID());
                    Utils.writeObject(new File(
                                    COMMITFILE.toString() + "/"
                                            + remotePointer.getID()),
                            remotePointer);
                    for (String id : remotePointer.getCurrBlobs().keySet()) {
                        Blob thisBlob = Utils.readObject(new File(
                                remoteFile.toString() + "/.blobs/"
                                        + remotePointer.getCurrBlobs()
                                        .get(id)), Blob.class);
                        Utils.writeObject(
                                new File(BLOBFILE.toString() + "/"
                                        + thisBlob.getID()),
                                thisBlob);
                    }
                }
                remotePointer = remotePointer.getParentCommit();
            }
            branches.put(newBranch, remoteHead.getID());
            Utils.writeObject(branchesFile, branches);
        }
    }

    /**
     * Fetches branch as for the fetch command, and then
     * merges that fetch into the current branch.
     * @param remoteName the name of the remote.
     * @param remoteBranch the name of the remote branch.
     */
    public void pull(String remoteName, String remoteBranch) {
        fetch(remoteName, remoteBranch);
        String newBranch = remoteName + "/" + remoteBranch;
        merge(newBranch);
    }

    /**
     * Appends commits to the head at the given file.
     * @param remoteHead the commit pointed to by the remote head.
     * @param remoteFile the path of the remote repository.
     */
    public void appendCommits(Commit remoteHead, File remoteFile) {
        Commit currPointer = this.getCurrCommit();
        while (currPointer.getParentCommit() != null
                && !currPointer.equals(remoteHead)) {
            Utils.writeObject(new File(
                            remoteFile.toString()
                                    + "/.commits/" + currPointer.getID()),
                    currPointer);
            for (String id : currPointer.getCurrBlobs().keySet()) {
                Blob thisBlob = Utils.readObject(new File(
                        BLOBFILE.toString() + "/"
                                + currPointer.getCurrBlobs()
                                .get(id)), Blob.class);
                Utils.writeObject(
                        new File(remoteFile.toString()
                                + "/.blobs/" + thisBlob.getID()),
                        thisBlob);
            }
            currPointer = currPointer.getParentCommit();
        }
    }

    /**
     * write the commit tree to disk.
     */
    public void writeCommitTree() {
        Utils.writeObject(new File(MAINFILE.toString()
                + "/" + "mainTree"), this);
    }

    /**
     * Read out the commit tree.
     * @param fileName the name of the tree.
     * @return the commit tree related to the name.
     */
    public static CommitTree readCommitTree(String fileName) {
        return Utils.readObject(new File(MAINFILE.toString()
                + "/" + fileName), CommitTree.class);
    }
}
