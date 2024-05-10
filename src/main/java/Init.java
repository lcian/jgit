import picocli.CommandLine.Command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.Callable;


@Command(name = "init",
        description = "Initialize an empty Git repository"
)
public class Init implements Callable<Integer> {

    @Override
    public Integer call() throws IOException {
        final File root = new File(".git");
        new File(root, "objects").mkdirs();
        new File(root, "refs").mkdirs();
        final File head = new File(root, "HEAD");
        head.createNewFile();
        FileWriter writer = new FileWriter(head);
        writer.write("ref: refs/heads/master\n");
        writer.close();
        System.out.printf("Initialized empty Git repository in %s%n", Paths.get("").toAbsolutePath());
        return 0;
    }
}