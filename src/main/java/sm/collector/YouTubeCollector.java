package main.java.sm.collector;
import java.io.*;
import java.util.*;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;

public class YouTubeCollector {
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
   private static YouTube.Search.List search ;
    public static void main (String args[])throws IOException{

   String queryterm =getInputQuery();

  searchVideo(queryterm);
   //searchChannel( queryterm);
        //searchByKeyword (queryterm);

    }

    /**
     * Initialize a YouTube object to search for videos on YouTube. Then display
     * the name and thumbnail image of each video in the result set.
     */
    public static  YouTube.Search.List intialize (String queryTerm, String type)  {

        // Read the developer key from the properties file.
        Properties properties = new Properties();
        try {
            InputStream in = YouTubeCollector.class.getResourceAsStream("/"
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
             // Restrict the search results to only include videos. See:
             // https://developers.google.com/youtube/v3/docs/search/list#type
            search.setType(type);
        }
            catch (GoogleJsonResponseException e) {
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

    //Search Videos by Keywords
    // To increase efficiency, only retrieve the fields that the
    // application uses.
    // search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
    public static void searchVideo(String queryTerm)throws IOException{

       search  = intialize (queryTerm, "video");
       search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

       // Call the API and print results.
        SearchListResponse searchResponse = search.execute();
        List<SearchResult> searchResultList = searchResponse.getItems();
        if (searchResultList != null) {
            prettyPrint(searchResultList.iterator(), queryTerm,"YoutubeVideo");
            }
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

    //Search the Type channel by a keyword
    public static void searchChannel(String queryTerm) throws IOException{
              search  = intialize (queryTerm,"channel");

             // Call the API and print results.
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();
            for (int i= 0; i<searchResultList.size(); i++){
                System.out.println("******** The "+ i +" channel***************");
                System.out.println("channel  " +searchResultList.get(i).toPrettyString());
                System.out.println("\r\n");
         /* if (searchResultList != null) {
                prettyPrint(searchResultList.iterator(), queryTerm, "YouTubeChannel");
            }*/
        }
    }

//Return videos, channel and playlist
public static void searchByKeyword (String queryTerm)throws IOException{
    search  = intialize (queryTerm,"");

    // Call the API and print results.
    SearchListResponse searchResponse = search.execute();
    List<SearchResult> searchResultList = searchResponse.getItems();
    for (int i= 0; i<searchResultList.size(); i++){
        System.out.println("******** The "+ i +" result***************");
        System.out.println ("*********** Result Kind ********************" + searchResultList.get(i).getKind());
        System.out.println("\r\n");
        System.out.println(searchResultList.get(i).toPrettyString());
        System.out.println("\r\n");
          /*  if (searchResultList != null) {
                prettyPrint(searchResultList.iterator(), queryTerm);
            }*/
    }
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
    /*
        * Prints out all results in the Iterator. For each result, print the title,
        * video ID, and thumbnail.
        *
        * @param iteratorSearchResults Iterator of SearchResults to print
        *
        * @param query Search query (String)
        */
public static void prettyPrint (

            Iterator<SearchResult> iteratorSearchResults, String query, String fileName)throws IOException {
        List<CommentThread> videoComments  =  new ArrayList<CommentThread>();
        new File(System.getProperty("user.home")+"/Data").mkdir();
        FileWriter  file = new FileWriter( System.getProperty("user.home")+"/Data"+"/"+fileName+".json"
        );
        System.out
                .println("\n=============================================================");
        System.out.println("   First " + NUMBER_OF_VIDEOS_RETURNED
                + " videos for search on \"" + query + "\".");
        System.out
                .println("=============================================================\n");

        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }

        while (iteratorSearchResults.hasNext()) {

            SearchResult singleVideo = iteratorSearchResults.next();

            ResourceId rId = singleVideo.getId();

            // Confirm that the result represents a video. Otherwise, the
            // item will not contain a video ID.
            if (rId.getKind().equals("youtube#video")) {

                Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails()
                        .getDefault();
                System.out.println(" Video Id: " + rId.getVideoId());
                System.out.println(" Title: "
                        + singleVideo.getSnippet().getTitle());
                System.out.println(" Thumbnail: " + thumbnail.getUrl());
                System.out
                        .println("\n-------------------------------------------------------------\n");


                try{

                    file.write("\r\n");
                    file.write("************Video************");
                    file.write ( singleVideo.toPrettyString());
                    file.write("\r\n");

                    videoComments = getComment(rId
                            .getVideoId());

                    for (int j = 0; j < videoComments.size(); j++) {
                        file.write("************Comment************");
                        file.write("\r\n");
                        //get the comments of each video
                        //test video before
                        file.write(videoComments.get(j).toPrettyString());
                        file.write("\r\n");
                    }

                }  catch (IOException e){
                    e.printStackTrace();
                }

            }
        }
        try{
            file.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

}
