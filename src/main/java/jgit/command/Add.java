package jgit.command;

import jgit.GitIndex;
import picocli.CommandLine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "add",
        description = "Add a file to the staging area"
)
public class Add implements Callable<Integer> {

    @CommandLine.Parameters(
            arity = "1",
            description = "File to add",
            paramLabel = "<file>"
    )
    private String file;

    @Override
    public Integer call() throws FileNotFoundException {
        GitIndex index = new GitIndex();
        if (Files.exists(Path.of(".git/index"))) {
            index = GitIndex.deserialize(new FileInputStream(".git/index"));
        }
        index.add(this.file);
        index.serialize(new FileOutputStream(".git/index"));
        return 0;
    }
}
