package sm.collector;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.User;
import sm.collector.entity.Content;
import sm.collector.entity.Post;
import sm.collector.entity.Profile;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class FacebookCollector extends Collector {

    private FacebookClient getAccessToken() {
        String accessToken = "EAAZAxtjxvbg4BAB89RjYZCJxWVeCsin2nRmLGJFNrllqqIW3ZBJgDgfONC7QWrYTzQkxBO1ns57zwWvMxlFehWCNjqK2cCyetZB5pIR7RhJvU2EX8UlGXGEPRQhsQz2TUL9KgNiZAkZBatG7iTc1UvbYWHiYvQonH5JagQZCGXZARwZDZD";
        FacebookClient fbClient = new DefaultFacebookClient(accessToken);
        return fbClient;
    }

    @Override
    public List<Profile> collectProfiles(String keyword) {
        List<Profile> profiles = new LinkedList<>();

        FacebookClient fbClient = getAccessToken();
        Connection<User> publicSearch = fbClient
                .fetchConnection("search", User.class,
                        Parameter.with("q", "barak obama"),
                        Parameter.with("type", "user"),
                        Parameter.with("limit", 200),
                        Parameter.with("offset", 0),
                        Parameter.with("fields",
                                "email,first_name,last_name,gender,birthday,friends,link,name,age_range,website,locale"));

        for (User user : publicSearch.getData()) {
            profiles.add(new Profile(Content.Type.FACEBOOK, user.getId(), user));

        }

        return profiles;
    }

    @Override
    public List<Post> collectPosts(String keyword) {
        //TODO implement me...
        return null;
    }

    public static void main(String[] args) throws IOException {
        FacebookCollector collector = new FacebookCollector();
        List<Profile> profiles = collector.collectProfiles("obama");

        profiles.forEach(System.out::println);
    }
}
