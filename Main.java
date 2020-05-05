package gitlet;

import java.io.File;

import static java.lang.System.*;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Yulin Li
 */
public class Main {

    /** The current working directory. */
    public static final File CWD = new File(getProperty("user.dir"));

    /**
     * check if the commit tree has already been set up.
     * @return if it has been set up.
     */
    public static boolean inited() {
        File mainFile = new File(CWD.toString() + "/.gitlet");
        return mainFile.exists();
    }

    /**
     * boolean to see if a command has been found valid.
     */
    private static boolean found = false;

    /**
     * the tree object for the new operation.
     */
    private static CommitTree tree = new CommitTree();

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     * @param args the commands.
     */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        } else if (!inited() && !args[0].equals("init")) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        switch (args[0]) {
        case "init":
            if (args.length != 1) {
                System.out.println("Incorrect operands.");
                return;
            }
            tree.init();
            writeTree(tree);
            found = true;
            break;
        case "log":
            if (args.length != 1) {
                System.out.println("Incorrect operands.");
                return;
            }
            tree = CommitTree.readCommitTree("mainTree");
            tree.log();
            writeTree(tree);
            found = true;
            break;
        case "global-log":
            if (args.length != 1) {
                System.out.println("Incorrect operands.");
                return;
            }
            tree = CommitTree.readCommitTree("mainTree");
            tree.globalLog();
            writeTree(tree);
            break;
        case "find":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                return;
            }
            tree = CommitTree.readCommitTree("mainTree");
            tree.find(args[1]);
            writeTree(tree);
            found = true;
            break;
        default:
            otherMain(args);
        }
    }

    /**
     * The main function for remote commands only.
     * @param args the commands.
     */
    public static void remoteMain(String... args) {
        switch (args[0]) {
        case "add-remote":
            if (args.length != 3) {
                System.out.println("Incorrect operands.");
                return;
            }
            tree = CommitTree.readCommitTree("mainTree");
            tree.addRemote(args[1], args[2]);
            writeTree(tree);
            found = true;
            break;
        case "rm-remote":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                return;
            }
            tree = CommitTree.readCommitTree("mainTree");
            tree.rmRemote(args[1]);
            found = true;
            writeTree(tree);
            break;
        case "fetch":
            if (args.length != 3) {
                System.out.println("Incorrect operands.");
                return;
            }
            tree = CommitTree.readCommitTree("mainTree");
            tree.fetch(args[1], args[2]);
            writeTree(tree);
            found = true;
            break;
        case "pull":
            if (args.length != 3) {
                System.out.println("Incorrect operands.");
                return;
            }
            tree = CommitTree.readCommitTree("mainTree");
            tree.pull(args[1], args[2]);
            writeTree(tree);
            found = true;
            break;
        case"push":
            if (args.length != 3) {
                System.out.println("Incorrect operands.");
                return;
            }
            tree = CommitTree.readCommitTree("mainTree");
            tree.push(args[1], args[2]);
            writeTree(tree);
            found = true;
            break;
        default:
            thirdMain(args);
        }
    }

    /**
     * the third main.
     * @param args commands.
     */

    public static void thirdMain(String... args) {
        switch (args[0]) {
        case "status":
            if (args.length != 1) {
                System.out.println("Incorrect operands.");
                return;
            }
            tree = CommitTree.readCommitTree("mainTree");
            tree.status();
            writeTree(tree);
            found = true;
            break;
        case "branch":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                return;
            }
            tree = CommitTree.readCommitTree("mainTree");
            tree.branch(args[1]);
            writeTree(tree);
            found = true;
            break;
        default:
            lastMain(args);
        }
    }

    /**
     * last main.
     * @param args commands.
     */
    public static void lastMain(String... args) {
        switch (args[0]) {
        case "merge":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                return;
            }
            tree = CommitTree.readCommitTree("mainTree");
            tree.merge(args[1]);
            writeTree(tree);
            found = true;
            break;
        case "add":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                return;
            }
            tree = CommitTree.readCommitTree("mainTree");
            tree.add(args[1]);
            writeTree(tree);
            found = true;
            break;
        case "commit":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                return;
            }
            tree = CommitTree.readCommitTree("mainTree");
            tree.commit(args[1]);
            writeTree(tree);
            found = true;
            break;
        case "rm":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                return;
            }
            tree = CommitTree.readCommitTree("mainTree");
            tree.remove(args[1]);
            writeTree(tree);
            found = true;
            break;
        default:
        }
        if (!found) {
            System.out.println("No command with that name exists.");
        }
    }

    /**
     * the other main.
     * @param args commands.
     */
    public static void otherMain(String... args) {
        switch (args[0]) {
        case "rm-branch":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                return;
            }
            tree = CommitTree.readCommitTree("mainTree");
            tree.rmBranch(args[1]);
            writeTree(tree);
            found = true;
            break;
        case "reset":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                return;
            }
            tree = CommitTree.readCommitTree("mainTree");
            tree.reset(args[1]);
            writeTree(tree);
            found = true;
            break;
        case "checkout":
            if (args.length == 2) {
                tree = CommitTree.readCommitTree("mainTree");
                tree.checkoutBranch(args[1]);
                writeTree(tree);
                found = true;
            } else if (args[1].equals("--")) {
                tree = CommitTree.readCommitTree("mainTree");
                tree.checkoutFileName(args[2]);
                writeTree(tree);
                found = true;
            } else if (args[2].equals("--")) {
                tree = CommitTree.readCommitTree("mainTree");
                tree.ckCommitFile(args[1], args[3]);
                writeTree(tree);
                found = true;
            } else {
                System.out.println("Incorrect operands.");
                return;
            }
            break;
        default:
            remoteMain(args);
        }
    }

    /**
     * write the tree.
     * @param thisTree this tree.
     */
    public static void writeTree(CommitTree thisTree) {
        thisTree.writeCommitTree();
        found = true;
    }
}


