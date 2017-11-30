package sm.collector;

import groovy.json.JsonBuilder;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TwitterCollector {

    public TwitterCollector() {

    }

    static public Twitter getAccessToken() {

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true).setOAuthConsumerKey("A7UIKEpBjMlatRjuLwPQvyszL")
                .setOAuthConsumerSecret("mTHpPptB3wRzShahIgygvB7chJVtyWJ1eJqbjio0mCvCJxP0XQ")
                .setOAuthAccessToken("2803989151-eMg7JOf0RSmlLmoLbKYM7WEETQU7hIvmUtT29JM")
                .setOAuthAccessTokenSecret("WHMrp2cPIxSWK3RxdR5PaDtkJmaGIHdQphAISTcAY5eIa");
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
        return twitter;
    }

    static public void searchUsers(String keywords, Twitter twitter) throws TwitterException, InterruptedException, IOException {
        new File(System.getProperty("user.home")+"/Data").mkdir();
        FileWriter  file = new FileWriter( System.getProperty("user.home")+"/Data"+"/Tw_SearchedUsers.json"
        );
        file.write("\r\n");
        //users searching
        ResponseList<User> users = twitter.searchUsers(keywords, -1);
        file.write("*********************Retrieved Profiles**********************\n\n");
        System.out.println("--------------------------------Retrieved Profiles-------------------------------- \n");
        //users displaying
        int i = 1;
        for (User user : users) {
            //  profile information displaying
            displayData("User", i, user);
            //profile information storing
            storeData("User", i, file, user);
            i++;
        }
        System.out.println("-------------------------------End----------------------------");
        file.close();
    }

    static public void getTweetsByUser(String screenName, Twitter twitter) throws TwitterException, IOException {
        new File(System.getProperty("user.home")+"/Data").mkdir();
        FileWriter  file = new FileWriter( System.getProperty("user.home")+"/Data"+"/Tw_UserTimeline.json"
        );
        file.write("\r\n");
        //set screenName of the user to fetch his posts
        User user = twitter.showUser(screenName);
        ResponseList<Status> status = twitter.getUserTimeline(user.getId());
        file.write("*********************Retrieved Timeline of the User " + user.getName() + "**********************\n\n");
        System.out.println("----------------------------Tweets of " + user.getName() + "----------------------------");
        if (status != null) {
            int i = 1;
            for (Status stat : status) {
                //TimeLine information displaying
                displayData("Tweet", i, stat);
                //TimeLine information storing
                storeData("Tweet", i, file, stat);
                i++;
            }
        }
        System.out.println("-------------------------------------END----------------------");
        file.close();

    }

    static public void getTweetsByKeywords(String keywords, Twitter twitter) throws IOException {
        new File(System.getProperty("user.home")+"/Data").mkdir();
        FileWriter  file = new FileWriter( System.getProperty("user.home")+"/Data"+"/Tw_TweetsByKeywords.json"
        );
        file.write("\r\n");
        try {
            Query query = new Query(keywords);
            QueryResult result;
            int i = 1;
            do {
                result = twitter.search(query);
                List<Status> tweets = result.getTweets();
                file.write("*********************Retrieved Tweets for the following keywords (" + keywords + ") **********************\n\n");
                System.out.println("----------------------------Tweets related to the following keywords \" " + keywords + " \"----------------------------");
                for (Status tweet : tweets) {
                    // information displaying
                    displayData("Tweet", i, tweet);
                    //information storing
                    storeData("Tweet", i, file, tweet);
                    i++;
                }
                System.out.println("--------------------------------END---------------------------");

            } while ((query = result.nextQuery()) != null);
            System.exit(0);
            file.close();
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to search tweets: " + te.getMessage());
            System.exit(-1);
        }

    }

    public static void displayData(String entityName, int entityNumber, Object entity) {
        System.out.println("\n==========================================================================");
        System.out.println("                                  " + entityName + " " + entityNumber);
        System.out.println("==========================================================================\n");
        System.out.println(new JsonBuilder(entity).toPrettyString());
    }

    public static void storeData(String entityName, int entityNumber, FileWriter file, Object entity) throws IOException {
        file.write("***************************" + entityName + " " + entityNumber + "************************\n");
        file.write(new JsonBuilder(entity).toPrettyString());
        file.write("\r\n");
    }

    public static void main(String[] args) throws TwitterException, InterruptedException, IOException {
        Twitter twitter = getAccessToken();
        //searchUsers("barak obama", twitter);
        //getTweetsByUser("aminaamara1", twitter);
        getTweetsByKeywords("Warren,Donald,Trump", twitter);

    }
}

