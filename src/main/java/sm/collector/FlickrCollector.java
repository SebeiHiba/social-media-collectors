package main.java.sm.collector;

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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FlickrCollector {
// private  final Transport transportAPI ;
public static void main (String [] args)throws FlickrException{

    String apiKey = "1d16520343c27f3f2b0f973b1d447e47";
    String sharedSecret = "91844af3aa9d6069";
    Flickr f = new Flickr(apiKey, sharedSecret, new REST());
    TestInterface testInterface = f.getTestInterface();
    java.util.Collection results = testInterface.echo(Collections.EMPTY_MAP);
    //initialize SearchParameter object, this object stores the search keyword
    SearchParameters searchParams=new SearchParameters();
    searchParams.setSort(SearchParameters.INTERESTINGNESS_DESC);
    //Create tag keyword array
    String[] tags=new String[]{"Dog","Doberman"};
    searchParams.setTags(tags);

    //Initialize PhotosInterface object
    PhotosInterface photosInterface=f.getPhotosInterface();

    //Execute search with entered tags
    // PhotoList photoList=null;
    PhotoList photoList=photosInterface.search(searchParams,20,1);
    System.out.println("here");
    //get search result and fetch the photo object and get small square imag's url
    if(photoList!=null){
        System.out.println("photoList "+ photoList.getPages());
        //Get search result and check the size of photo result
        for(int i = 0; i<photoList.size();i++){
         Photo photo=(Photo)photoList.get(i);
         System.out.println( "*********** The " +i + " photo ***************");
         System.out.println ("url: " + photo.getUrl());
         System.out.println(  "description: " +photo.getDescription());
         System.out.println(  "id: " +photo.getId());
         System.out.println( "title: " + photo.getTitle());
         System.out.println( "comment: " + photo.getComments());
         System.out.println( "owner: " + photo.getOwner());
        }

             }
     }
}

