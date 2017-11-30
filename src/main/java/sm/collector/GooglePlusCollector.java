package main.java.sm.collector;
import java.io.IOException;
import java.util.List;

import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Activity;
import com.google.api.services.plus.model.ActivityFeed;


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
import groovy.json.JsonBuilder;
import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class GooglePlusCollector {
    private static Plus plusSvc;
    private static String CLIENT_ID = "246775426922-8sskbhfg6h3usqindumvs52libg0okr6.apps.googleusercontent.com";
    private static String CLIENT_SECRET = "MrseVLH6WwmxIx9rk1wMtLLR";
    private static String REDIRECT_URI = "https://localhost";

    public GooglePlusCollector() {
    }

    public static Plus authentificate() throws IOException {
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
                credential).setApplicationName("SocialMediaDataCollector").build();
    }

    public static void searchPeople(String keywords, Plus plusSvc) throws IOException {
        new File(System.getProperty("user.home")+"/Data").mkdir();
        FileWriter  file = new FileWriter( System.getProperty("user.home")+"/Data"+"/GP_Profiles.json"
        );
        file.write("\r\n");
        Plus.People.Search searchPeople = plusSvc.people().search(keywords);
        searchPeople.setMaxResults(5L);
        PeopleFeed peopleFeed = searchPeople.execute();
        List<Person> people = peopleFeed.getItems();

        // Loop through until we arrive at an empty page, or the second page
        file.write("**************************Retrieved Profiles**********************************\n");
        System.out.println("--------------------------------Retrieved Profiles-------------------------------- \n");
        int pageNumber = 1;
        int i = 1;
        while (people != null && pageNumber <= 2) {
            pageNumber++;
            for (Person person : people) {
                //  profile information displaying
                displayData("User", i, person);
                //profile information storing
                storeData("User", i, file, person);
                i++;
            }

            // We will know we are on the last page when the next page token is null.
            // If this is the case, break.
            if (peopleFeed.getNextPageToken() == null) {
                break;
            }
        }
        System.out.println("--------------------------------------------------------------------------- \n");
        file.write("*******************************************************************************\n");
        file.close();
    }

    public static void getActivityByKeyword(String keywords, Plus plusSvc) throws IOException {
        new File(System.getProperty("user.home")+"/Data").mkdir();
        FileWriter  file = new FileWriter( System.getProperty("user.home")+"/Data"+"/GP_ActivitiesByKeywords.json"
        );
        file.write("\r\n");
        Plus.Activities.Search searchActivities = null;
        //search activities relative to this "keyword"
        try {
            searchActivities = plusSvc.activities().search(keywords);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //20L is maximum number of activities to include in the response, which is used for paging
        searchActivities.setMaxResults(20L);
        ActivityFeed activityFeed = null;
        try {
            activityFeed = searchActivities.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // get activities in the current page of results.
        List<Activity> activities = activityFeed.getItems();
        // Loop through until we arrive at an empty page
        file.write("**************************Retrieved Activities**********************************\n");
        System.out.println("--------------------------------Retrieved Activities-------------------------------- \n");
        int pageNumber = 1;
        int i = 1;
        while (activities != null && pageNumber <= 2) {
            pageNumber++;
            for (Activity activity : activities) {
                //information displaying
                displayData("Activity", i, activity);
                //information storing
                storeData("Activity", i, file, activity);
                i++;
            }
            // We will know we are on the last page when the next page token is null.
            // If this is the case, break.
            if (activityFeed.getNextPageToken() == null) {
                break;
            }
            // Prepare to request the next page of activities
            searchActivities.setPageToken(activityFeed.getNextPageToken());
            // Execute and process the next page request
            try {
                activityFeed = searchActivities.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            activities = activityFeed.getItems();
        }
        System.out.println("--------------------------------------------------------------------------- \n");
        file.write("*******************************************************************************\n");
        file.close();
    }

    public static void getUserActivities(String idProfile, Plus plusSvc) throws IOException {
        new File(System.getProperty("user.home")+"/Data").mkdir();
        FileWriter  file = new FileWriter( System.getProperty("user.home")+"/Data"+"/GP_UserTimeline.json"
        );
        file.write("\r\n");
        //get the list of activities of a given user using his profile ID
        Plus.Activities.List listActivities = plusSvc.activities().list(idProfile, "public");
        listActivities.setMaxResults(5L);
        // Execute the request for the first page
        ActivityFeed activityFeed = listActivities.execute();
        // Unwrap the request and extract the pieces we want
        List<Activity> activities = activityFeed.getItems();
        file.write("**************************Retrieved User Timeline Activities**********************************\n");
        System.out.println("--------------------------------Retrieved User Timeline Activities-------------------------------- \n");
        // Loop through until we arrive at an empty page
        int i=1;
        while (activities != null) {
            for (Activity activity : activities) {
                //  profile information displaying
                displayData("Activity", i, activity);
                //profile information storing
                storeData("Activity", i, file, activity);
                i++;
            }

            // We will know we are on the last page when the next page token is null.
            // If this is the case, break.
            if (activityFeed.getNextPageToken() == null) {
                break;
            }

            // Prepare to request the next page of activities
            listActivities.setPageToken(activityFeed.getNextPageToken());

            // Execute and process the next page request
            activityFeed = listActivities.execute();
            activities = activityFeed.getItems();
        }
        System.out.println("--------------------------------------------------------------------------- \n");
        file.write("*******************************************************************************\n");
        file.close();
    }
    public static void displayData(String entityName, int entityNumber, Object entity) {
        System.out.println("\n==========================================================================");
        System.out.println("                                  "+entityName+" " + entityNumber);
        System.out.println("==========================================================================\n");
        System.out.println(new JsonBuilder(entity).toPrettyString());
    }

    public static void storeData(String entityName, int entityNumber, FileWriter file, Object entity) throws IOException {
        file.write("***************************" + entityName + " " + entityNumber + "************************\n");
        file.write(new JsonBuilder(entity).toPrettyString());
        file.write("\r\n");
    }
    public static void main(String[] args) throws IOException {
        Plus plusSvc = authentificate();
        //searchPeople("trump", plusSvc);
        //getActivityByKeyword("wold cup 2018", plusSvc);
        getUserActivities("+Trumpia", plusSvc);
    }
}
