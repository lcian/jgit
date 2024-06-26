package jgit;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class GitTree implements GitObject {

    private SortedSet<Child> children;

    public GitTree() {
        this.children = new TreeSet<>(Child.nameComparator);
    }

    public GitTree(Collection<Child> children) {
        this();
        this.children.addAll(children);
    }

    public void addChild(Child child) {
        this.children.add(child);
    }

    public static GitTree deserializeUncompressed(byte[] in) {
        if (in.length < 4 || !new String(Arrays.copyOfRange(in, 0, 4)).equals("tree")) {
            throw new RuntimeException("Not a tree object");
        }
        final GitTree tree = new GitTree();
        int i = 0;
        while (in[i] != (byte) '\0') {
            i++;
        }
        i++;
        final byte[] contents = Arrays.copyOfRange(in, i, in.length);
        StringBuilder sb = new StringBuilder();
        GitObjectFactory factory = new GitObjectFactory();
        while (i < in.length) {
            while ((char) in[i] != ' ') {
                sb.append((char) in[i]);
                i++;
            }
            i++;
            final int mode = Integer.parseInt(sb.toString());
            sb.setLength(0);

            while ((char) in[i] != '\0') {
                sb.append((char) in[i]);
                i++;
            }
            i++;
            final String name = sb.toString();
            sb.setLength(0);

            for (int j = i; j < i + 20; j++) {
                sb.append(String.format("%02x", in[j]));
            }
            i += 20;
            String hash = sb.toString();
            sb.setLength(0);

            final GitObject child = factory.createGitObject(
                    Common.getContents(hash)
            );
            tree.children.add(new Child(child, mode, name));
        }
        return tree;
    }

    public String getType() {
        return "tree";
    }

    public int getSize() {
        byte[] object = this.getUncompressedSerialization();
        int i = 0;
        while (object[i] != (byte) '\0') {
            i++;
        }
        i++;
        return object.length - i;
    }

    public byte[] getHash() {
        return DigestUtils.sha1(this.getUncompressedSerialization());
    }

    public String getHashHex() {
        return DigestUtils.sha1Hex(this.getUncompressedSerialization());
    }

    public String toString(boolean recursive) {
        return this.toString(recursive, "");
    }

    private String toString(boolean recursive, String prepend) {
        StringBuilder sb = new StringBuilder();
        for (Child child : children) {
            if (recursive && child.getObject().getType() == "tree") {
                GitTree childObject = (GitTree) child.getObject();
                sb.append(childObject.toString(true, prepend + child.getName() + "/"));
            } else {
                sb.append(
                        String.format("%06d %s %s\t%s",
                                child.getMode().mode,
                                child.getObject().getType(),
                                child.getObject().getHashHex(),
                                prepend + child.getName()
                        )
                );
            }
            if (!child.equals(children.getLast())) {
                sb.append(String.format("%n"));
            }
        }
        return sb.toString();
    }

    public String toString() {
        return this.toString(false);
    }

    public byte[] getUncompressedSerialization() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (Child child : this.children) {
            final byte[] entry = ArrayUtils.addAll(
                    String.format("%d %s\0", child.getMode().mode, child.getName()).getBytes(),
                    child.getObject().getHash()
            );
            try {
                baos.write(entry);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        final byte[] body = baos.toByteArray();
        return ArrayUtils.addAll(
                String.format("tree %d\0", body.length).getBytes(),
                body
        );
    }

    public enum Mode {
        NORMAL_FILE(100644),
        EXECUTABLE_FILE(100755),
        SYMBOLIC_LINK(120000),
        DIRECTORY(40000);

        public final int mode;

        private Mode(int mode) {
            this.mode = mode;
        }

        public static Mode valueOfMode(int mode) {
            for (Mode e : Mode.values()) {
                if (e.mode == mode) {
                    return e;
                }
            }
            return null;
        }
    }

    public static class Child {

        private static final Comparator<Child> nameComparator = Comparator.comparing(Child::getName);

        private GitObject object;
        private Mode mode;
        private String name;

        public Child(GitObject object, int mode, String name) {
            this.object = object;
            this.mode = Mode.valueOfMode(mode);
            this.name = name;
        }

        public GitObject getObject() {
            return this.object;
        }

        public Mode getMode() {
            return this.mode;
        }

        public String getName() {
            return this.name;
        }
    }

}
