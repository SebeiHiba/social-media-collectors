package sm;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import sm.collector.Collector;
import sm.collector.entity.Post;
import sm.collector.entity.Profile;
import sm.io.IO;

import java.util.List;

public class Runner {


    public static void main(String[] args) {
        String version = Runner.class.getPackage().getImplementationVersion();

        MainCommand main = new MainCommand();
        JCommander jc = new JCommander(main);
        jc.setProgramName(String.format("Social Media Downloader", version));
        DownloadCommand downloadCommand = new DownloadCommand();
        jc.addCommand("download", downloadCommand);

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

        for (String word : keywords) {

            if (profile) {
                for (Collector collector : collectors) {
                    List<Profile> profiles = collector.collectProfiles(word);
                    profiles.forEach(io::save);
                }
            }

            if (post) {
                for (Collector collector : collectors) {
                    List<Post> posts = collector.collectPosts(word);
                    posts.forEach(io::save);
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
}

