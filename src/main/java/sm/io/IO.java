package sm.io;

import sm.collector.entity.Content;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IO {

    private String basedir;

    public IO(String basedir) throws IOException {
        this.basedir = basedir;
        for (Content.Type type : Content.Type.values()) {
            prepareDir(type);
            prepareFile(type, "ids_Post_");
            prepareFile(type, "ids_Profile_");
        }
    }

    private void prepareDir(Content.Type type) {
        File targetDir = Paths.get(basedir, type.toString().toLowerCase()).toFile();
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
    }

    private void prepareFile(Content.Type type, String fileName) throws IOException {
        File targetDir = Paths.get(basedir + "\\" + type, fileName + type.toString().toLowerCase()).toFile();
        if (!targetDir.exists())
            targetDir.createNewFile();
    }

    public List<Content> verifyEntries(List<Content> contents, String entityName) throws IOException, URISyntaxException {
        Set<String> ids;
        List<Content> newIDs = new ArrayList<>();
        if (contents.size() != 0) {
            ids = loadIDs(contents.get(0).type, entityName);
            if (ids != null) {
                contents.forEach(s -> {
                    if (!(ids.contains(s.id))) {
                        newIDs.add(s);
                    }
                });
            } else newIDs.addAll(contents);
        }
        return newIDs;
    }

    public Set<String> loadIDs(Content.Type type, String entityName) throws URISyntaxException, IOException {
        Set<String> ids = new HashSet<>();
        Path targetDir = Paths.get(basedir + "\\" + type, "ids_" + entityName + "_" + type.toString().toLowerCase());
        File writer = new File(targetDir.toString());
        if (writer.length() != 0 && writer.exists()) {
            Files.lines(targetDir).forEach(ids::add);
        }
        return ids;
    }

    public void saveIDs(Content.Type type, String entityName, List<Content> newIDs) {

        try {
            Path targetDir = Paths.get(basedir + "\\" + type, "ids_" + entityName + "_" + type.toString().toLowerCase());
            FileWriter writer = new FileWriter(targetDir.toString(), true);
            newIDs.forEach(s -> {
                try {
                    writer.append(s.id + "\r\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void save(Content content) {
        File targetFile = Paths.get(basedir, content.type.toString().toLowerCase(), content.hashCode() + "").toFile();
        try {
            FileWriter writer = new FileWriter(targetFile);
            writer.write(content.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
