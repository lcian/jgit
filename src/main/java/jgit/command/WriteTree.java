package jgit.command;

import jgit.Common;
import jgit.GitBlob;
import jgit.GitIndex;
import jgit.GitTree;
import picocli.CommandLine.Command;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.Callable;


@Command(name = "write-tree",
        description = "Create a tree object from the current index"
)
public class WriteTree implements Callable<Integer> {

    private GitTree createTreeRecursively(Vector<String> blobPaths, String prefix) {
        final Vector<GitTree.Child> children = new Vector<>();
        final HashMap<String, Vector<String>> indirectChildren = new HashMap<>();
        for (String path : blobPaths) {
            if (!path.equals(prefix)) {
                final String pathPrefix = path.split("/")[0];
                if (!indirectChildren.containsKey(prefix)) {
                    indirectChildren.put(pathPrefix, new Vector<>());
                }
                indirectChildren.get(pathPrefix).addLast(path);
            } else {
                System.out.printf("Processing path %s%n", path);
                final HashObject hasher = new HashObject(path, true);
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    hasher.call(baos);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                final String hash = baos.toString();
                byte[] compressedContents = Common.getContents(hash);
                byte[] contents = Common.inflate(new ByteArrayInputStream(compressedContents));
                GitBlob blob = GitBlob.deserializeUncompressed(contents);
                String name = Arrays.stream(path.split("/")).toList().getLast();
                int mode = 100644; // FIXME
                children.add(new GitTree.Child(blob, mode, path));
            }
        }
        for (String key : indirectChildren.keySet()) {
            return this.createTreeRecursively(indirectChildren.get(key), key);
        }
        return new GitTree(children);
    }

    @Override
    public Integer call() throws IOException {
        final FileInputStream fis;
        try {
            fis = new FileInputStream(".git/index");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        final GitIndex index = GitIndex.deserialize(fis);
        System.out.println(index.toString());

        final Vector<String> blobPaths = new Vector<>();
        for (Iterator<String> it = index.getObjectRelativePaths(); it.hasNext(); ) {
            String blobPath = it.next();
            blobPaths.addLast(blobPath);
            System.out.format("Blob path %s%n", blobPath);
        }
        final GitTree tree = this.createTreeRecursively(blobPaths, "");
        final String hash = tree.getHashHex();
        System.out.println(hash);
        final String hashPrefix = hash.substring(0, 2);
        final String hashSuffix = hash.substring(2);

        if (Files.notExists(Path.of(String.format(".git/objects/%s", hashPrefix)))) {
            new File(".git/objects", hashPrefix).mkdirs();
        }
        final String objectPath = String.format(".git/objects/%s/%s", hashPrefix, hashSuffix);
        final FileOutputStream fos = new FileOutputStream(objectPath);
        tree.serialize(fos);
        fos.close();
        return 0;
    }
}