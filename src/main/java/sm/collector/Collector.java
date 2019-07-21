package sm.collector;

import sm.collector.entity.Post;
import sm.collector.entity.Profile;

import java.util.List;

public abstract class Collector {

    public List<Post> collectPosts(String keyword) {
        return null;
    }

    public abstract List<Profile> collectProfiles(String keyword);
}
