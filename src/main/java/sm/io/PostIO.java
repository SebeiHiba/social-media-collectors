package sm.io;

import sm.collector.entity.Post;

import java.io.File;

public class PostIO implements SocialMediaIO<Post> {

    @Override
    public void write(Post content, File file) {
        //TODO implement me...
    }

    @Override
    public Post read(File file) {
        //TODO implement me...
        return null;
    }
}
