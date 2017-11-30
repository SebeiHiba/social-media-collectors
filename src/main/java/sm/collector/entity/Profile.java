package sm.collector.entity;

import groovy.json.JsonBuilder;

//TODO unify the profiles content
public class Profile extends Content {

    public final Object content;

    public Profile(Type type, Object content) {
        super(type);
        this.content = content;
    }

    @Override
    public String toString() {
        return new JsonBuilder(content).toPrettyString();
    }
}
