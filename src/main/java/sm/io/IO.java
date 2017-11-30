package sm.io;

import sm.collector.entity.Content;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class IO {

    private String basedir;

    public IO(String basedir) {
        this.basedir = basedir;
        for (Content.Type type : Content.Type.values()) {
            prepareDir(type);
        }
    }

    private void prepareDir(Content.Type type) {
        File targetDir = Paths.get(basedir, type.toString().toLowerCase()).toFile();
        if (!targetDir.exists()) {
            targetDir.mkdirs();
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
