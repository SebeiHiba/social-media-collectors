package sm.io;

import java.io.File;

public class IO {

    public IO(File basedir) {
        if(!basedir.exists()) {
            basedir.mkdirs();
        }


    }
}
