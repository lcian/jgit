package jgit;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitCommit implements GitObject {

    private String parentHash;
    private String treeHash;
    private String authorName;
    private String authorEmail;
    private Instant creationTimestamp;
    private String committerName;
    private String committerEmail;
    private Instant commitmentTimestamp;
    private String message;

    public GitCommit(String parentHash, String treeHash, String authorName, String authorEmail, Instant creationTimestamp, String committerName, String committerEmail, Instant commitmentTimestamp, String message) {
        this.parentHash = parentHash;
        this.treeHash = treeHash;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.creationTimestamp = creationTimestamp;
        this.committerName = committerName;
        this.committerEmail = committerEmail;
        this.commitmentTimestamp = commitmentTimestamp;
        this.message = message;
    }

    public GitCommit(String treeHash, String authorName, String authorEmail, Instant creationTimestamp, String committerName, String committerEmail, Instant commitmentTimestamp, String message) {
        this(null, treeHash, authorName, authorEmail, creationTimestamp, committerName, committerEmail, commitmentTimestamp, message);
    }

    public GitCommit(String treeHash, String authorName, String authorEmail, Instant creationTimestamp, String message) {
        this(null, treeHash, authorName, authorEmail, creationTimestamp, authorName, authorEmail, creationTimestamp, message);
    }

    public GitCommit(String parentHash, String treeHash, String authorName, String authorEmail, Instant creationTimestamp, String message) {
        this(parentHash, treeHash, authorName, authorEmail, creationTimestamp, authorName, authorEmail, creationTimestamp, message);
    }

    private static final Pattern AUTHOR_PATTERN = Pattern.compile("author (.+) <(.+)> (\\d+) ([+-]\\d+)");
    private static final Pattern COMMITTER_PATTERN = Pattern.compile("committer (.+) <(.+)> (\\d+) ([+-]\\d+)");

    private static Instant parseGitTimestamp(String epochSeconds, String offset) {
        long seconds = Long.parseLong(epochSeconds);
        return Instant.ofEpochSecond(seconds).atOffset(ZoneOffset.of(offset)).toInstant();
    }

    public static GitCommit deserializeUncompressed(byte[] in) {
        if (in.length < 6 || !new String(Arrays.copyOfRange(in, 0, 6)).equals("commit")) {
            throw new RuntimeException("Not a commit object");
        }
        int i = 0;
        while (in[i] != (byte) '\0') {
            i++;
        }
        i++;
        String[] lines = new String(Arrays.copyOfRange(in, i, in.length)).split("\n");

        String parentHash = null;
        String treeHash = null;
        String authorName = null;
        String authorEmail = null;
        String committerName = null;
        String committerEmail = null;
        String message = null;
        Instant creationTimestamp = null;
        Instant commitmentTimestamp = null;
        for (i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.startsWith("tree ")) {
                treeHash = line.substring(5);
            } else if (line.startsWith("parent ")) {
                parentHash = line.substring(7);
            } else if (line.startsWith("author ")) {
                Matcher matcher = AUTHOR_PATTERN.matcher(line);
                if (matcher.find()) {
                    authorName = matcher.group(1);
                    authorEmail = matcher.group(2);
                    creationTimestamp = parseGitTimestamp(matcher.group(3), matcher.group(4));
                }
            } else if (line.startsWith("committer ")) {
                Matcher matcher = COMMITTER_PATTERN.matcher(line);
                if (matcher.find()) {
                    committerName = matcher.group(1);
                    committerEmail = matcher.group(2);
                    commitmentTimestamp = parseGitTimestamp(matcher.group(3), matcher.group(4));
                }
            } else if (line.isEmpty()) {
                // The commit message starts after an empty line
                message = String.join("\n", Arrays.copyOfRange(lines, i + 1, lines.length));
                break;
            }
        }
        if (parentHash != null) {
            return new GitCommit(parentHash, treeHash, authorName, authorEmail, creationTimestamp, committerName, committerEmail, commitmentTimestamp, message);
        } else {
            return new GitCommit(treeHash, authorName, authorEmail, creationTimestamp, committerName, committerEmail, commitmentTimestamp, message);
        }
    }

    public String getParentHash() {
        return parentHash;
    }

    @Override
    public String getType() {
        return "commit";
    }

    @Override
    public int getSize() {
        byte[] object = this.getUncompressedSerialization();
        int i = 0;
        while (object[i] != (byte) '\0') {
            i++;
        }
        i++;
        return object.length - i;
    }

    @Override
    public byte[] getHash() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.serialize(baos);
        byte[] serialized = baos.toByteArray();
        return DigestUtils.sha1(serialized);
    }

    public String getHashHex() {
        return Hex.encodeHexString(this.getHash());
    }

    @Override
    public byte[] getUncompressedSerialization() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(String.format("tree %s%n", this.treeHash).getBytes());
            if (this.parentHash != null) {
                baos.write(String.format("parent %s%n", this.parentHash).getBytes());
            }
            baos.write(String.format("author %s <%s> %s +0200%n", this.authorName, this.authorEmail, this.creationTimestamp.getEpochSecond()).getBytes());
            baos.write(String.format("committer %s <%s> %s +0200%n", this.committerName, this.committerEmail, this.commitmentTimestamp.getEpochSecond()).getBytes());
            baos.write(String.format("%n%s%n", this.message).getBytes());
            byte[] body = baos.toByteArray();
            out.write(String.format("commit %d\0", body.length).getBytes());
            out.write(body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }

    public String toString(boolean withCommit) {
        return String.format("commit %s%n", this.getHashHex()) + this.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("tree %s%n", this.treeHash));
        if (this.parentHash != null) {
            ;
            sb.append(String.format("parent %s%n", this.parentHash));
        }
        sb.append(String.format("author %s <%s> %s +0200%n", this.authorName, this.authorEmail, this.creationTimestamp.getEpochSecond()));
        sb.append(String.format("committer %s <%s> %s +0200%n", this.committerName, this.committerEmail, this.commitmentTimestamp.getEpochSecond()));
        sb.append(String.format("%n%s%n", this.message));
        return sb.toString();
    }
}
