package sm;

import sm.collector.Collector;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CollectorFactory {

    public static Collector create(String name) {
        switch (name) {
            case "facebook":
                return null;//return new FacebookCollector();
        }

        return null;
    }

    public static List<Collector> create(String[] names) {
        return Arrays.stream(names).map(CollectorFactory::create).collect(Collectors.toList());
    }
}
