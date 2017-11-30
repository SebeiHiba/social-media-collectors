package sm.collector.entity;

import groovy.json.JsonBuilder;

import java.util.Objects;

//TODO unify the profiles content
public class Profile extends Content {

    public final Object content;

    public Profile(Type type, Object content) {
        super(type);
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Profile profile = (Profile) o;
        return Objects.equals(content, profile.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), content);
    }

    @Override
    public String toString() {
        return new JsonBuilder(content).toPrettyString();
    }
}
