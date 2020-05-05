# Gitlet Design
**Name** : Yulin Li

# Classes and Data Structures 
## CommitTree

This class represents the current commit tree, keeping track of all commits made in this repository. 
**Fields**  

1. `HashMap<String, String> branches`: A map with the branch name as the `key` and commit ID as the `value`. 
2. `String head`: A pointer to the current commit (its unique ID). 
## Commit 

Defines and records the characteristics and references of the commits. 
**Fields** 

1. `HashMap<String, String> currBlobs`: A map representing the references to files kept in a certain commit as `blobs`, with the name of the file as the `key` and the blob ID as the `value`. 
## Class Stage 

Defines and keeps track of the preparation stages. 
**Fields** 

1. `HashMap<String, String> addingStage`: A map representing the `blobs` that have been staged for adding, with the name of the file as the `key` and the blob ID as the `value`. (Note the similarity in structure allows easy copy onto each commit’s `currBlobs`.)
2. `HashMap<String, String> removingStage`: A map representing the `blobs` that have been staged for removing, with the name of the file as the `key` and the blob ID as the `value`. 
3. `HashMap<String, File> remotes`: A map representing the name of the remote files and the corresponding files. 
## Class Blob 

Defines and records the characteristics of the contents of the files tracked by the git system. 

----------
# Algorithms 
## CommitTree Class 
1. `init()`: Method that initializes a gitlet repository. Creates the main `stage` and the initial commit which is empty and has the initial branch `master` point to this initial commit. 
2. `makeDir()`: Method that creates the `.gitlet` directory and the `commits`, `blobs`, and `stage` directories inside to keep corresponding objects. 
3. `add(String fileName)`: Add the file of the name of `fileName` in the Current Working Directory to the `addingStage` in the current `stage` as a `blob`. 
4. `commit(String msg)`: Make a new `commit`, modifying or adding the files that have been added to the `stage`. Have this `commit` as the new `head` and add this `commit` as a new node to the current `CommitTree`. Clears the staging areas after a commit is successfully made. 
5. `remove(String fileName)`: If the file is currently in the `addingStage` , remove it. If the file is in the most recent `commit`, add it to the `removingStage` and deletes it from the Current Working Directory. 
6. `find(String msg)`: Prints out the commit ID who have `msg` as their log message. Start every one with a new line if there is more than one. 
7. `reset(String id)`: Resets to the commit with the given id. 
8. `checkout(String branchName)`: checks out to the given branch name, deleting everything tracked in the current working directory but not in the checked out commit. 
9. `merge (String branchName)`: merges the current branch into the given branch. 


## Commit Class
1. `Commit (String msg, String parent, HashMap<String, String> toAdd, HashMap<String, String> toRemove)`: Constructor for the `Commit` class, takes in `msg` from the client as its log message and String `parent` representing its parent commit. `toAdd` and `toRemove` are HashMaps from the `stage`, this constructor adds and removes `blobs` from its `currBlobs` according to these HashMaps. 
2. `Commit (String msg, String parent)`: A simpler constructor meant for the initial constructor, defaulting the `date` to 1/1/1970. 
3. `String logString()`: returns the string in the log format. 
----------
# Persistence 

In order to persist the state of the main `CommitTree`, we need to save the `CommitTree` after each call to the program. To do this, 

1. Write the current `CommitTree` to disk after it is first created with `init()` and every time a command is executed in the `Main` class. 
2. Write the current `Commit` to disk every time it is created. 
3. Write the tracked `blobs` to disk once they have been converted from Files. 
4. Write the current `Stage` to disk every time a change has been made to the current stage. 
    *All of the above is done with* `*Utils.writeObject(file, serializable object)*`*, the file names are their unique* `*SHA-1 ID*` *and kept under their respective sub directories under* `*.gitlet*`*.*

In order to retrieve our state, every time such objects are called on after the initial one, we need to search for them using `Utils.readObject(file, class)` with the appropriate parameters. 


