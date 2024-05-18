package jgit.command;

import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "commit",
        description = "Commit the currently staged files"
)
public class Commit implements Callable<Integer> {

    @CommandLine.Parameters(
            arity = "1",
            description = "Commit message",
            paramLabel = "<message>"
    )
    private String message;

    @Override
    public Integer call() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WriteTree.writeTree(baos);
        String treeHash = baos.toString().trim();
        String parentCommitHash = null;

        final String masterRef = ".git/refs/heads/master";
        if (Files.exists(Path.of(masterRef))) {
            parentCommitHash = Files.readString(Path.of(masterRef)).trim();
        }

        CommitTree.CommitTree(treeHash, parentCommitHash, this.message);
        return 0;
    }
}
