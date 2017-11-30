package sm;

import sm.collector.Collector;
import sm.collector.FacebookCollector;

public class CollectorFactory {

    public static Collector create(String name) {
        switch (name) {
            case "facebook":
                return null;//return new FacebookCollector();
        }

        return null;
    }
}
