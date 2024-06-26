package jgit;

import jgit.command.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;


@Command(name = "jgit",
        description = "jgit version 1.0.0",
        subcommands = {
                CatFile.class, HashObject.class, LsTree.class, WriteTree.class, CommitTree.class,
                Init.class, Add.class, Commit.class, Log.class
        }
)
public class Main implements Callable<Integer> {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        CommandLine.usage(this, System.out);
        return 0;
    }
}
