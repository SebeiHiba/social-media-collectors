package sm.collector;


import sm.collector.entity.Content;
import sm.collector.entity.Post;
import sm.collector.entity.Profile;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;


import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


public class TwitterCollector extends Collector{
    //	How many tweets to retrieve in every call to Twitter. 100 is the maximum allowed in the API
    private static final int TWEETS_PER_QUERY		=3;

    //	This controls how many queries, maximum, we will make of Twitter before cutting off the results.
    //	You will retrieve up to MAX_QUERIES*TWEETS_PER_QUERY tweets.
    //
    //  If you set MAX_QUERIES high enough (e.g., over 450), you will undoubtedly hit your rate limits
    //  and you an see the program sleep until the rate limits reset
    private static final int MAX_QUERIES			= 2;

    //Twitter authentification
    private Twitter getAccessToken() {

        ConfigurationBuilder cb = new ConfigurationBuilder();
       /* cb.setJSONStoreEnabled(true).setDebugEnabled(true).setOAuthConsumerKey("yy8Y0vZ4UhH6czHiD1UpF9tjo")
                .setOAuthConsumerSecret("3CHGnY5sTC6eNNhQa3Pe67aNoNtLs9hi4WzKif7X1Sv9s5iJCv")
                .setOAuthAccessToken("4220969668-qtPDjGf6ZNDMURnOCMw4KjlhwyYrNsEVDhE7qAf")
                .setOAuthAccessTokenSecret("JsGrmatGOJE0QgwXB0SDSRr66O32cRsDDmUloBVWG0kIP");*/

        cb.setJSONStoreEnabled(true)
                .setDebugEnabled(true)
                .setOAuthConsumerKey("JCAVXzFH9efguu0RVgcvlDOH5")
                .setOAuthConsumerSecret(
                        "uNowcNPRh78o0HdcpZweJoaqnG75ZuW5O3u5SLmvPtbSPpHTDQ")
                .setOAuthAccessToken(
                        "863880911187738625-nIDOACROQSsqoxLDEa1ceIuelfLYhQq")
                .setOAuthAccessTokenSecret(
                        "b5DJ7mctINtpcmgMv6byCPOuIj4B51XiAvAN7ESe9U3vg");
cb.setHttpProxyHost("10.30.0.11");
     cb.setHttpProxyPassword("rana05376263");
       cb.setHttpProxyPort(Integer.parseInt("8080"));
cb.setHttpProxyUser("05376263");
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
        return twitter;
    }
    //collect tweets
    public List<Post> collectPosts(String keyword)  {
        int	totalTweets = 0;
        long maxID = -1;
        boolean testrate=false;
        QueryResult result;

        List<Post> posts = new LinkedList<>();
        Twitter twitter = getAccessToken();

        for (int queryNumber=0;queryNumber < MAX_QUERIES; queryNumber++)
        {
            System.out.printf("\n\n!!! Starting loop %d\n\n", queryNumber);

            //	Do we need to delay because we've already hit our rate limits?

            try {
                Query query = new Query(keyword);
                query.setCount(TWEETS_PER_QUERY);// How many tweets, max, to retrieve
                if (maxID != -1)
                {
                    query.setMaxId(maxID - 1);
                }

                do {
                    result = twitter.search(query);
                    if (result.getRateLimitStatus().getRemaining() == 0)
                    {
                        //	Yes we do, unfortunately ...
                        System.out.printf("!!! Sleeping for %d seconds due to rate limits\n", result.getRateLimitStatus().getSecondsUntilReset());

                        try {
                            Thread.sleep((result.getRateLimitStatus().getSecondsUntilReset()+2) * 1000l);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    List<Status> tweets = result.getTweets();


                    System.out.println(" Limit: " +  result.getRateLimitStatus().getLimit());
                    System.out.println(" Remaining: " +  result.getRateLimitStatus().getRemaining());
                    System.out.println(" SecondsUntilReset: " +  result.getRateLimitStatus().getSecondsUntilReset());
                    JSONObject JSON_complete = null;
                    for (Status tweet : tweets) {

                        System.out.println ("*************tweet********** "+ tweet);
                        //	Increment our count of tweets retrieved
                        totalTweets++;
                        System.out.println(" totalTweets "+ totalTweets);

                        //	Keep track of the lowest tweet ID.  If you do not do this, you cannot retrieve multiple
                        //	blocks of tweets...
                        if (maxID == -1 || tweet.getId() < maxID)
                        {
                            maxID = tweet.getId();
                        }
                        //Status To JSON String&
                        String statusJson = DataObjectFactory.getRawJSON(tweet);
                        //JSON String to JSONObject
                        try {
                             JSON_complete = new JSONObject(statusJson);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        System.out.println ("*******JSON Object**********" + JSON_complete);
                        posts.add(new Post(Content.Type.TWITTER, Objects.toString(tweet.getId()),JSON_complete));
                      //  System.out.println("tweet "+ TwitterObjectFactory.getRawJSON(tweet));
                        //KeyedMessage<String, String> message = new KeyedMessage<String, String>(topic, TwitterObjectFactory.getRawJSON(tweet));
                        //  producer.send(message);
                    }

                } while ((query = result.nextQuery()) != null);
            } catch (TwitterException te) {
                te.printStackTrace();
                System.out.println("Failed to search tweets: " + te.getMessage());
                System.exit(-1);
            }
        }
        return posts;

    }

    //Collect profiles
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
            profiles.add(new Profile(Content.Type.TWITTER,Objects.toString(user.getId()),user));

        }
        return profiles;
    }
    public static void main(String[] args) throws InterruptedException, TwitterException {
        TwitterCollector tp= new TwitterCollector();
        tp.collectPosts("Samsung");

    }

}