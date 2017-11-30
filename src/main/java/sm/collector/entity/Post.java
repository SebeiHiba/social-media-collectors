package sm.collector.entity;

//TODO unify the post content
public class Post extends Content {

    public final Object content;

    public Post(Type type, Object content) {
        super(type);
        this.content = content;
    }
}
