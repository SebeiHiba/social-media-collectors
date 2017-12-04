package sm.collector;

import com.flickr4java.flickr.*;

import com.flickr4java.flickr.collections.Collection;
import com.flickr4java.flickr.people.PeopleInterface;
import com.flickr4java.flickr.people.User;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photos.SearchParameters;
import com.flickr4java.flickr.test.TestInterface;
import com.flickr4java.flickr.util.XMLUtilities;
import groovy.json.JsonBuilder;
import org.w3c.dom.Element;
import sm.collector.entity.Content;
import sm.collector.entity.Post;
import sm.collector.entity.Profile;

import java.util.*;

public class FlickrCollector extends Collector {
    // private  final Transport transportAPI ;
    public List<Profile> collectProfiles(String keyword) {
        return null;
    }

    public List<Post> collectPosts(String queryTerm) {
        List<Post> posts = new LinkedList<>();
        Flickr flickr = authentification();
        TestInterface testInterface = flickr.getTestInterface();
        try {
            java.util.Collection results = testInterface.echo(Collections.EMPTY_MAP);
        } catch (FlickrException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : "
                    + e.getMessage());
        }
        //initialize SearchParameter object, this object stores the search keyword
        SearchParameters searchParams = new SearchParameters();
        searchParams.setSort(SearchParameters.INTERESTINGNESS_DESC);
        //Create tag keyword array
        String[] tags = new String[]{queryTerm};
        searchParams.setTags(tags);
        //Initialize PhotosInterface object
        PhotosInterface photosInterface = flickr.getPhotosInterface();
        //Execute search with entered tags
        // PhotoList photoList=null;
        try {
            PhotoList photoList = photosInterface.search(searchParams, 20, 1);
            for (int i = 0; i < photoList.size(); i++) {
                posts.add(new Post(Content.Type.FLICKR, (Photo) photoList.get(i)));
            }
        } catch (FlickrException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : "
                    + e.getMessage());
        }


        return posts;
    }

    public Flickr authentification() {
        String apiKey = "1d16520343c27f3f2b0f973b1d447e47";
        String sharedSecret = "91844af3aa9d6069";
        Flickr flickr = new Flickr(apiKey, sharedSecret, new REST());
        return flickr;
    }

}

