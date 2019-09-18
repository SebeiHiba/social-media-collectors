package sm;

import sm.collector.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CollectorFactory {

    public static Collector create(String name) {
        switch (name) {
            case "twitter":
                return new TwitterCollector();
            
            case "youtube":
                return new YouTubeCollector();
        }

        return null;
    }

    public static List<Collector> create(String[] names) {
        return Arrays.stream(names).map(CollectorFactory::create).collect(Collectors.toList());
    }
}
