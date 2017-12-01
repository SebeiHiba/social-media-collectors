package sm.collector;
import sm.collector.entity.Content;
import sm.collector.entity.Post;
import sm.collector.entity.Profile;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TwitterCollector extends Collector {

    private Twitter getAccessToken() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true).setOAuthConsumerKey("A7UIKEpBjMlatRjuLwPQvyszL")
                .setOAuthConsumerSecret("mTHpPptB3wRzShahIgygvB7chJVtyWJ1eJqbjio0mCvCJxP0XQ")
                .setOAuthAccessToken("2803989151-eMg7JOf0RSmlLmoLbKYM7WEETQU7hIvmUtT29JM")
                .setOAuthAccessTokenSecret("WHMrp2cPIxSWK3RxdR5PaDtkJmaGIHdQphAISTcAY5eIa");
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
        return twitter;
    }

    @Override
    public List<Post> collectPosts(String keyword) {
        List<Post> posts = new LinkedList<>();
        Twitter twitter = getAccessToken();
        try {
            Query query = new Query(keyword);
            QueryResult result;
            do {
                result = twitter.search(query);
                List<Status> tweets = result.getTweets();
                for (Status tweet : tweets) {
                    posts.add(new Post(Content.Type.TWITTER,tweet));
                }

            } while ((query = result.nextQuery()) != null);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to search tweets: " + te.getMessage());
            System.exit(-1);
        }
        return posts;
    }

    @Override
    public List<Profile> collectProfiles(String keyword) {
        List<Profile> profiles = new LinkedList<>();
        Twitter twitter = getAccessToken();
        ResponseList<User> users = null;
        try {
            users = twitter.searchUsers(keyword, -1);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        for (User user : users) {
            profiles.add(new Profile(Content.Type.TWITTER, user));
        }
        return profiles;
    }
    public static void main(String[] args) throws TwitterException, InterruptedException, IOException {
        TwitterCollector collector = new TwitterCollector();
        //List<Profile> profiles = collector.collectProfiles("obama");
      List<Post> posts = collector.collectPosts("obama");
        //profiles.forEach(System.out::println);
       posts.forEach(System.out::println);
    }

}
