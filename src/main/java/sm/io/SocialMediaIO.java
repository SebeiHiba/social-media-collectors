package sm.io;

import java.io.File;

public interface SocialMediaIO<T> {

    void write(T content, File file);

    T read(File file);
}
