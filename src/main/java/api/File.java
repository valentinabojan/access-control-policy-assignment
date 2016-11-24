package api;

import java.io.Serializable;

public class File implements Serializable {

    private String name;
    private String value;
    private FileType type;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public FileType getType() {
        return type;
    }

    public static class FileBuilder {
        private File file;

        private FileBuilder() {
            file = new File();
        }

        public static FileBuilder file() {
            return new FileBuilder();
        }

        public FileBuilder withType(FileType type) {
            file.type = type;
            return this;
        }

        public FileBuilder withName(String name) {
            file.name = name;
            return this;
        }

        public FileBuilder withValue(String value) {
            file.value = value;
            return this;
        }

        public File build() {
            return file;
        }
    }
}