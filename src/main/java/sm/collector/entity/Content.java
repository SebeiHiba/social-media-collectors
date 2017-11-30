package sm.collector.entity;

import java.util.Objects;

public abstract class Content {

    public enum Type {
        FACEBOOK, FLICKR, GOOGLE_PLUS, LINKEDIN, TWITTER, YOUTUBE
    }

    public final Type type;

    public Content(Type type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Content content = (Content) o;
        return type == content.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
