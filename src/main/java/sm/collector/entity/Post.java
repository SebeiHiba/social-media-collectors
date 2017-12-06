package sm.collector.entity;

import groovy.json.JsonBuilder;

import java.util.Objects;

//TODO unify the post content
public class Post extends Content {

    public final Object content;

    public Post(Type type, Object content, String id) {
        super(type,id);
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Post post = (Post) o;
        return Objects.equals(content, post.content);
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
