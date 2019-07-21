package sm;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import sm.collector.*;
import sm.collector.entity.*;
import sm.io.IO;
import org.apache.kafka.clients.producer.Producer;


import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Runner {

    public static void main(String[] args) throws IOException {
        String version = Runner.class.getPackage().getImplementationVersion();

        MainCommand main = new MainCommand();
        JCommander jc = new JCommander(main);
        jc.setProgramName(String.format("Social Media Downloader", version));
        DownloadCommand downloadCommand = new DownloadCommand();
        jc.addCommand("download", downloadCommand);


        downloadCommand.socialMedias = "youtube";
        downloadCommand.dir = "/home/admin/coll";
        downloadCommand.type = "post";
        downloadCommand.keywords = "obama";

        try {
            jc.parse(args);
        } catch (ParameterException e) {
            e.printStackTrace();
            jc.usage();
        }
        if (jc.getParsedCommand() == null) {
            jc.usage();
        }

        String lt = downloadCommand.type.toLowerCase();
        boolean profile = lt.equals("profile");
        boolean post = lt.equals("post");

        if (lt.equals("both")) {
            profile = post = true;
        }

        if (!(profile || post)) {
            throw new Error("type is invalid.");
        }

        IO io = new IO(downloadCommand.dir);

        String[] socialMedias = downloadCommand.socialMedias.split("-");
        String[] keywords = downloadCommand.keywords.split("-");
        List<Collector> collectors = CollectorFactory.create(socialMedias);
        Set<String> ids;
        for (String word : keywords) {
            if (profile) {
                for (Collector collector : collectors) {
                    List<Profile> profiles = collector.collectProfiles(word);

                    try {
                        List<Content> newIDs = null;
                        List<Content> contents = new ArrayList<Content>();
                        contents.addAll(profiles);
                        newIDs = io.verifyEntries(contents, "Profile");
                        newIDs.forEach(io::save);
                        if (newIDs.size() != 0)
                            io.saveIDs(newIDs.get(0).type, "Profile", newIDs);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (post) {
                for (Collector collector : collectors) {
                    List<Post> posts = collector.collectPosts(word);
                    posts.forEach(System.out::println);
                    List<Content> newIDs = null;

                    try {

                        List<Content> contents = new ArrayList<Content>();
                        contents.addAll(posts);
                        newIDs = io.verifyEntries(contents, "Post");
                        newIDs.forEach(io::save);
                        if (newIDs.size() != 0)
                            io.saveIDs(newIDs.get(0).type, "Post", newIDs);
                        int length = collector.getClass().getName()
                                .toLowerCase().length();
                        String sub = collector.getClass().getName()
                                .toLowerCase().substring(13, length - 9);
                        System.out.println("sub" + sub);
                        // producer subscription on kafka broker
                        KafKaBroker  broker = new KafKaBroker();
                        Producer<String, String> producer = broker
                                .producerSubcription();
                        // send new posts to kafka broker
                        do {
                            broker.sendMessage(producer, sub + "Post", newIDs
                                    .iterator().next().toString());
                        } while (newIDs.iterator().hasNext());


                        // consumer subscription
						/*		KafkaConsumer<String, String> consumer = broker
								.consumerSubcription();
						// consume messages from broker
						ConsumerRecords<String, String> records = broker
								.consumeMessage(consumer, sub + "Post");
						Wrapper wrapper = new Wrapper();
				for (ConsumerRecord<String, String> record : records) {
							// print the offset,key and value for the consumer
							// records.
							System.out.printf(
									"offset = %d, key = %s, value = %s\n",
									record.offset(), record.key(),
									record.value());
							try {
								wrapper.mapp(record.value(), sub, "Post");
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}*/

                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    @Parameters(commandDescription = "See commands below.")
    private static final class MainCommand {
        @Parameter(names = "--help", help = true, description = "You know this...")
        boolean help;
    }

    @Parameters(commandDescription = "Downloading social media data.", separators = "=")
    private static class DownloadCommand {
        @Parameter(names = {"-sm", "--social-medias"}, required = true, description = "A list of social medias separated by - (dash) from which the data will be downloaded.", order = 0)
        String socialMedias;

        @Parameter(names = {"-d", "--directory"}, required = true, description = "A directory in which the data will be saved.", order = 1)
        String dir;

        @Parameter(names = {"-t", "--type"}, required = true, description = "Types can be 'post', 'profile' or 'both'.", order = 2)
        String type;

        @Parameter(names = {"-k", "--keywords"}, required = true, description = "A list of keywords separated by - (dash).", order = 3)
        String keywords;
    }

    /*public static void main(String[] args) throws IOException {

        String version = Runner.class.getPackage().getImplementationVersion();

        MainCommand main = new MainCommand();
        JCommander jc = new JCommander(main);
        jc.setProgramName(String.format("Social Media Downloader", version));
        DownloadCommand downloadCommand = new DownloadCommand();
        jc.addCommand("download", downloadCommand);

        downloadCommand.socialMedias = "twitter";
        downloadCommand.dir = "/home/admin/coll";
        downloadCommand.type = "post";
        downloadCommand.keywords = "trump";

        try {
            jc.parse(args);
        } catch (ParameterException e) {
            e.printStackTrace();
            jc.usage();
        }
        if (jc.getParsedCommand() == null) {
            jc.usage();
        }

        String lt = downloadCommand.type.toLowerCase();
        boolean profile = lt.equals("profile");
        boolean post = lt.equals("post");

        if (lt.equals("both")) {
            profile = post = true;
        }

        if (!(profile || post)) {
            throw new Error("type is invalid.");
        }

        IO io = new IO(downloadCommand.dir);

        String[] socialMedias = downloadCommand.socialMedias.split("-");
        String[] keywords = downloadCommand.keywords.split("-");
        List<Collector> collectors = CollectorFactory.create(socialMedias);

        List <Consumer> consumers= ConsumerFactory.create(socialMedias);


        KafkaProducer kp=new   KafkaProducer();
        Set<String> ids;
        for (String word : keywords) {

            if (profile) {
                boolean test=false;
                for (Collector collector : collectors) {
                    List<Profile> profiles = collector.collectProfiles(word);

                    try {
                        List<Content> newIDs = null;
                        List<Content> contents = new ArrayList<Content>();
                        contents.addAll(profiles);
                        newIDs = io.verifyEntries(contents, "Profile");
                      //  newIDs.forEach(io::save);
                        //send posts
                        for (Content content:newIDs){
                            kp.send(content.type,content);
                        }
                        int length=collector.getClass().getName().toLowerCase().length();
                        String sub=collector.getClass().getName().toLowerCase().substring(0,length-8);
                           //Consum profiles
                        Consumer consumer=consumers.get(0);
                        do {
                            if (consumer.getClass().getName().toLowerCase().startsWith(sub)){
                                consumer.consumProfiles();
                                test=true;
                            }
                            else
                                consumer=consumers.iterator().next();

                        }while (!test);
                        if (newIDs.size() != 0)
                            io.saveIDs(newIDs.get(0).type, "Profile", newIDs);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (post) {
            for (Collector collector : collectors) {
                boolean test=false;
              List<Post> posts = collector.collectPosts(word);
                    try {
                        List<Content> newIDs = null;
                        List<Content> contents = new ArrayList<Content>();
                        contents.addAll(posts);
                        System.out.println ("contents "+ contents.get(0).toString());
                        newIDs = io.verifyEntries(contents, "Post");
                        //send verified posts to kafka
                       //  newIDs.forEach(kp::send);
                            for (Content content:newIDs){
                                kp.send(content.type,content);
                            }
                            int length=collector.getClass().getName().toLowerCase().length();
                            String sub=collector.getClass().getName().toLowerCase().substring(13,length-9);
                        System.out.println("consumer "+ sub);
                        //Consum posts
                        Consumer consumer=consumers.get(0);


                      do {
                            System.out.println("test consumer");
                            if (consumer.getClass().getName().toLowerCase().contains(sub)){
                                consumer.consumPosts();
                                test=true;
                             }
                            else
                                consumer=consumers.iterator().next();

                        }while (!test);
                     //     newIDs.forEach(io::save);
                        if (newIDs.size() != 0)
                            io.saveIDs(newIDs.get(0).type, "Post", newIDs);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }


        }
    }


    @Parameters(commandDescription = "See commands below.")
    private static final class MainCommand {
        @Parameter(names = "--help", help = true, description = "You know this...")
        boolean help;
    }

    @Parameters(commandDescription = "Downloading social media data.", separators = "=")
    private static class DownloadCommand {
        @Parameter(names = {"-sm", "--social-medias"}, required = true, description = "A list of social medias separated by - (dash) from which the data will be downloaded.", order = 0)
        String socialMedias;

        @Parameter(names = {"-d", "--directory"}, required = true, description = "A directory in which the data will be saved.", order = 1)
        String dir;

        @Parameter(names = {"-t", "--type"}, required = true, description = "Types can be 'post', 'profile' or 'both'.", order = 2)
        String type;

        @Parameter(names = {"-k", "--keywords"}, required = true, description = "A list of keywords separated by - (dash).", order = 3)
        String keywords;
    }*/
}

