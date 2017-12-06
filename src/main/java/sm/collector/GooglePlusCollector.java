package sm.collector;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Activity;
import com.google.api.services.plus.model.ActivityFeed;
import com.google.api.services.plus.model.PeopleFeed;
import com.google.api.services.plus.model.Person;
import sm.collector.entity.Content;
import sm.collector.entity.Post;
import sm.collector.entity.Profile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class GooglePlusCollector extends Collector {

    private Plus authentificate() throws IOException {
        Plus plusSvc;
        String CLIENT_ID = "554467993368-3v80utiauf4uhvg9c4r224gbergv8f7d.apps.googleusercontent.com";
        String CLIENT_SECRET = "OHnd1YwyjWJ_gzaHXIAYkKdA";
        String REDIRECT_URI = "https://localhost";
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();

        Collection<String> SCOPE = Arrays.asList(
                "https://www.googleapis.com/auth/plus.login",
                "https://www.googleapis.com/auth/plus.me",
                "https://www.googleapis.com/auth/userinfo.email",
                "https://www.googleapis.com/auth/userinfo.profile",
                "https://www.googleapis.com/auth/plus.profiles.read");

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, SCOPE)
                .setAccessType("online").setApprovalPrompt("auto").build();

        String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI)
                .build();
        System.out
                .println("Please open the following URL in your browser then type the authorization code:");
        System.out.println("  " + url);
        System.out.println("Enter authorization code:");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String code = br.readLine();

        GoogleTokenResponse response = flow.newTokenRequest(code)
                .setRedirectUri(REDIRECT_URI).execute();
        GoogleCredential credential = new GoogleCredential()
                .setFromTokenResponse(response);

        return plusSvc = new Plus.Builder(httpTransport, jsonFactory,
                credential).setApplicationName("SocialMediaExtractor").build();
    }

    @Override
    public List<Post> collectPosts(String keyword) {
        List<Post> posts = new LinkedList<>();
        Plus plusSvc = null;
        try {
            plusSvc = authentificate();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Plus.Activities.Search searchActivities = null;
        try {
            searchActivities = plusSvc.activities().search(keyword);
        } catch (IOException e) {
            e.printStackTrace();
        }
        searchActivities.setMaxResults(20L);
        ActivityFeed activityFeed = null;
        try {
            activityFeed = searchActivities.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Activity> activities = activityFeed.getItems();
        int pageNumber = 1;
        while (activities != null && pageNumber <= 2) {
            pageNumber++;
            for (Activity activity : activities) {
                posts.add(new Post(Content.Type.GOOGLE_PLUS, activity, activity.getId()));
            }
            if (activityFeed.getNextPageToken() == null) {
                break;
            }
            searchActivities.setPageToken(activityFeed.getNextPageToken());
            try {
                activityFeed = searchActivities.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            activities = activityFeed.getItems();
        }
        return posts;
    }

    @Override
    public List<Profile> collectProfiles(String keyword) {
        List<Profile> profiles = new LinkedList<>();
        Plus plusSvc = null;
        try {
            plusSvc = authentificate();
            Plus.People.Search searchPeople = plusSvc.people().search(keyword);
            searchPeople.setMaxResults(5L);
            PeopleFeed peopleFeed = searchPeople.execute();
            List<Person> people = peopleFeed.getItems();
            int pageNumber = 1;
            while (people != null && pageNumber <= 2) {
                pageNumber++;
                for (Person person : people) {
                    profiles.add(new Profile(Content.Type.GOOGLE_PLUS, person, person.getId()));
                }
                if (peopleFeed.getNextPageToken() == null) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return profiles;
    }

    public static void main(String[] args) throws IOException {
        GooglePlusCollector collector = new GooglePlusCollector();
        //List<Profile> profiles = collector.collectProfiles("trump");
        //profiles.forEach(System.out::println);

        List<Post> posts = collector.collectPosts("Donalds Trump");
        posts.forEach(System.out::println);
    }

}
