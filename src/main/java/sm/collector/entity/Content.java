package sm.collector.entity;

public abstract class Content {

    public enum Type {
        FACEBOOK, FLICKR, GOOGLE_PLUS, LINKEDIN, TWITTER, YOUTUBE
    }

    public final Type type;

    public Content(Type type) {
        this.type = type;
    }
}
