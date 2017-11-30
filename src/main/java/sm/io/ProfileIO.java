package sm.io;

import sm.collector.entity.Profile;

import java.io.File;

public class ProfileIO implements SocialMediaIO<Profile> {

    @Override
    public void write(Profile content, File file) {
        //TODO implement me...
    }

    @Override
    public Profile read(File file) {
        //TODO implement me...
        return null;
    }
}
