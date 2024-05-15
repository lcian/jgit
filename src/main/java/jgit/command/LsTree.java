package jgit.command;

import jgit.Common;
import jgit.GitTree;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;


@Command(name = "ls-tree",
        description = "List the contents of a tree object"
)
public class LsTree implements Callable<Integer> {

    @Parameters(
            arity = "1",
            description = "Hash of the tree",
            paramLabel = "<tree>"
    )
    private String hash;

    @CommandLine.Option(
            names = "-r",
            description = "Traverse subtrees recursively"
    )
    private boolean isRecursive;

    @Override
    public Integer call() throws IOException {
        final String hashPrefix = hash.substring(0, 2);
        final String hashSuffix = hash.substring(2);
        final String objectPath = String.format(".git/objects/%s/%s", hashPrefix, hashSuffix);

        final byte[] compressedObject = Files.readAllBytes(Path.of(objectPath));
        final byte[] object = Common.inflate(new ByteArrayInputStream(compressedObject));
        final GitTree tree = GitTree.deserializeUncompressed(object);
        System.out.println(tree.toString(this.isRecursive));
        return 0;
    }
}