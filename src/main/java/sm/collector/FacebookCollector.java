package sm.collector;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.User;
import groovy.json.JsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FacebookCollector {

    public FacebookCollector() {

    }

    static public FacebookClient getAccessToken() {
        String accessToken = "EAAZAxtjxvbg4BAB89RjYZCJxWVeCsin2nRmLGJFNrllqqIW3ZBJgDgfONC7QWrYTzQkxBO1ns57zwWvMxlFehWCNjqK2cCyetZB5pIR7RhJvU2EX8UlGXGEPRQhsQz2TUL9KgNiZAkZBatG7iTc1UvbYWHiYvQonH5JagQZCGXZARwZDZD";
        FacebookClient fbClient = new DefaultFacebookClient(accessToken);
        return fbClient;
    }

    static public void searchPeople(String keyword) throws IOException {
        new File(System.getProperty("user.home")+"/Data").mkdir();
        FileWriter  file = new FileWriter( System.getProperty("user.home")+"/Data"+"/FB_Profiles.json"
        );
        file.write("\r\n");
        FacebookClient fbClient = getAccessToken();
        Connection<User> publicSearch = fbClient
                .fetchConnection(
                        "search",
                        User.class,
                        Parameter.with("q", "barak obama"),
                        Parameter.with("type", "user"),
                        Parameter.with("limit", 200),
                        Parameter.with("offset", 0),
                        Parameter
                                .with("fields",
                                        "email,first_name,last_name,gender,birthday,friends,link,name,age_range,website,locale"));
        System.out.println("--------------------------------Retrieved Profiles-------------------------------- \n");
        int i = 1;
        for (User user : publicSearch.getData()) {
            //  profile information displaying
            displayData("User", i, user);
            //profile information storing
            storeData("User", i, file, user);
            i++;
            }
        System.out.println("--------------------------------End--------------------------------");
        file.write("*******************************************************************************\n");
        file.close();
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

    public static void main(String[] args) throws IOException {
        FacebookClient fbClient = getAccessToken();
        searchPeople("obama");
    }
}
