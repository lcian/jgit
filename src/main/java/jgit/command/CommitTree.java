package jgit.command;

import jgit.GitCommit;
import picocli.CommandLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "commit-tree",
        description = "Create a new commit object"
)
public class CommitTree implements Callable<Integer> {

    @CommandLine.Parameters(
            arity = "1",
            description = "Hash of the tree",
            paramLabel = "<tree>"
    )
    private String treeHash;

    @CommandLine.Option(
            names = "-p",
            description = "Hash of the parent commit",
            paramLabel = "<parent>"
    )
    private String parentCommitHash;

    @CommandLine.Parameters(
            arity = "1",
            description = "Commit message",
            paramLabel = "<message>"
    )
    private String message;

    @Override
    public Integer call() throws IOException {
        final GitCommit commit = new GitCommit(
                this.parentCommitHash,
                this.treeHash,
                "Lorenzo Cian",
                "cian.lorenzo@gmail.com",
                Instant.now(),
                this.message
        );

        final String hash = commit.getHashHex();
        System.out.println(hash);
        final String hashPrefix = hash.substring(0, 2);
        final String hashSuffix = hash.substring(2);

        if (Files.notExists(Path.of(String.format(".git/objects/%s", hashPrefix)))) {
            new File(".git/objects", hashPrefix).mkdirs();
        }
        final String objectPath = String.format(".git/objects/%s/%s", hashPrefix, hashSuffix);
        commit.serialize(new FileOutputStream(objectPath));
        Files.write(Path.of(".git/refs/heads/master"), hash.getBytes());
        return 0;
    }

}
