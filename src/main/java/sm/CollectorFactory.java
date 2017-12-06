package sm;

import sm.collector.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CollectorFactory {

    public static Collector create(String name) {
        switch (name) {
            case "facebook":
                return new FacebookCollector();
            case "twitter":
                return new TwitterCollector();
            case "google_plus":
                return new GooglePlusCollector();
            case "youtube":
                return new YouTubeCollector();
            case "flickr":
                return new FlickrCollector();
        }

        return null;
    }

    public static List<Collector> create(String[] names) {
        return Arrays.stream(names).map(CollectorFactory::create).collect(Collectors.toList());
    }
}
