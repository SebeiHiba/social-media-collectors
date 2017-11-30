package sm.collector;

import sm.collector.entity.Post;
import sm.collector.entity.Profile;

import java.util.List;

public abstract class Collector {

    public abstract List<Post> collectPosts(String keyword);

    public abstract List<Profile> collectProfiles(String keyword);
}
