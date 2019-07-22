package sm.collector;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;

import sm.collector.Collector;
import sm.collector.YouTubeAuthentification;
import sm.collector.YouTubeCollector;
import sm.collector.entity.Post;
import sm.collector.entity.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class TestYouTube{


    /**
     * Define a global variable that identifies the name of a file that contains
     * the developer's API key.
     */
    private static final String PROPERTIES_FILENAME = "youtube.properties";
    private static final long NUMBER_OF_VIDEOS_RETURNED = 50;
    /**
     * Define a global instance of a Youtube object, which will be used to make
     * YouTube Data API requests.
     */
    private static YouTube youtube;
    private static YouTube.Search.List search;
    static SearchListResponse searchResponse ;


    //Collect Profiles
    public List<Profile> collectProfiles(String queryTerm) {

        List<Profile> profiles = new LinkedList<>();
        search = intialize(queryTerm, "channel");
        // Call the API and print results.
        try {
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();
            for (SearchResult result:searchResultList) {
                profiles.add(new Profile(Content.Type.YOUTUBE,result.getId().getChannelId(), result));
            }
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : "
                    + e.getMessage());
        }
        return profiles;
    }
    //Collect Published Video
    public static List<Post> collectPosts(String queryTerm) {

        int i=0;
        boolean res;
        int searchquota = 20;
        List<Post> posts = new LinkedList<>();


        search = intialize(queryTerm, "video");
        do {
            i++;
            System.out.println("loop "+ i);
            if (searchquota == 0) {
                try {
                    System.out.printf("!!! Sleeping for seconds due to rate limits\n");
                    Thread.sleep(5*1000);
                    searchquota =20;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // Call the API and print results.
            try {
                System.out.printf("search");

                searchResponse = search.execute();
                System.out.println("searchresponse " + searchResponse.isEmpty());
                searchquota=searchquota-1;
                System.out.printf("searchquota "+searchquota+"\n");
                List<SearchResult> searchResultList = searchResponse.getItems();
                for (SearchResult result : searchResultList) {
                    System.out.println ("result "+ result.toString());
                    posts.add(new Post(Content.Type.YOUTUBE, result.getId().getVideoId(), result));
                }
            } catch (IOException e) {
                System.err.println("There was an IO error: " + e.getCause() + " : "
                        + e.getMessage());
            }


        }while ((searchResponse.isEmpty()));
        return posts;
    }



    // Search Comments By Video ID
    public static List<CommentThread> searchCommentsByVideo(

            Iterator<SearchResult> iteratorSearchResults, String query) throws IOException {
        List<CommentThread> videoComments = new ArrayList<CommentThread>();
        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }
        while (iteratorSearchResults.hasNext()) {

            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();
            // Confirm that the result represents a video. Otherwise, the
            // item will not contain a video ID.
            if (rId.getKind().equals("youtube#video")) {
                try {
                    videoComments = getComment(rId
                            .getVideoId());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        return videoComments;

    }

    // Get Video Comments
    public static List<CommentThread> getComment(String idVideo)
            throws IOException {

        CommentThreadListResponse videoCommentsListResponse = youtube
                .commentThreads().list("snippet")
                .setKey("AIzaSyCh4qbhnIBT2VjwlyA28DPOP9Z73e2rWqU")
                .setVideoId(idVideo).setTextFormat("plainText")
                .setMaxResults(100L).execute();
        List<CommentThread> videoComments = videoCommentsListResponse
                .getItems();
        if (videoComments.isEmpty()) {
            System.out.println("Can't get video comments.");
        } else {
            // Print information from the API response.
            System.out
                    .println("\n================== Returned Video Comments ==================\n");
            for (CommentThread videoComment : videoComments) {
                CommentSnippet snippet = videoComment.getSnippet()
                        .getTopLevelComment().getSnippet();
                System.out.println("  - Author: "
                        + snippet.getAuthorDisplayName());
                System.out.println("  - Comment: " + snippet.getTextDisplay());
                System.out
                        .println("\n-------------------------------------------------------------\n");
            }

        }
        return videoComments;
    }


    /**
     * Initialize a YouTube object to search for videos on YouTube. Then display
     * the name and thumbnail image of each video in the result set.
     */
    public static YouTube.Search.List intialize(String queryTerm, String type) {


        // Read the developer key from the properties file.
        Properties properties = new Properties();
        try {
            InputStream in = sm.collector.YouTubeCollector.class.getResourceAsStream("/"
                    + PROPERTIES_FILENAME);
            properties.load(in);

        } catch (IOException e) {
            System.err.println("There was an error reading "
                    + PROPERTIES_FILENAME + ": " + e.getCause() + " : "
                    + e.getMessage());
            System.exit(1);
        }
        // This object is used to make YouTube Data API requests. The last
        // argument is required, but since we don't need anything
        // initialized when the HttpRequest is initialized, we override
        // the interface and provide a no-op function.
        try {
            youtube = new YouTube.Builder(YouTubeAuthentification.HTTP_TRANSPORT,
                    YouTubeAuthentification.JSON_FACTORY, new HttpRequestInitializer() {
                public void initialize(HttpRequest request)
                        throws IOException {
                }
            }).setApplicationName("YouTubeSearch").build();

            // Define the API request for retrieving search results.
            search = youtube.search().list("id,snippet");

            // Set your developer key from the Google API Console for
            // non-authenticated requests. See:
            // https://console.developers.google.com/
            String apiKey = properties.getProperty("youtube.apikey");
            search.setKey(apiKey);
            search.setQ(queryTerm);
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
            // Restrict the search results to only include videos. See:
            // https://developers.google.com/youtube/v3/docs/search/list#type
            search.setType(type);
        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: "
                    + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : "
                    + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return (search);
    }

    /*
     * Prompt the user to enter a query term and return the user-specified term.
     */
    public static String getInputQuery() throws IOException {
        String inputQuery = "";
        System.out.print("Please enter a search term: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(
                System.in));
        inputQuery = bReader.readLine();
        if (inputQuery.length() < 1) {
            // Use the string "YouTube Developers Live" as a default.
            inputQuery = "YouTube Developers Live";
        }
        return inputQuery;
    }

    public static void main (String [] args){
        collectPosts("obama");
    }
}


