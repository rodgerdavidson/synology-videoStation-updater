package org.rodgerdavidson;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class TrackingFileUtils {
    public static final String FAILED_EPISODES_TXT = "failed-episodes.txt";
    public static final String COMPLETED_EPISODES_TXT = "completed-episodes.txt";


    public void writeEpisodeToSuccessFile(String name) {
        File succeeded = new File(COMPLETED_EPISODES_TXT);
        writeTrackingFile(name, succeeded, "SUCCESS");
    }


    public void writeEpisodeToFailFile(String name) {
        File failed = new File(FAILED_EPISODES_TXT);
        writeTrackingFile(name, failed, "FAILED");
    }


    private void writeTrackingFile(String name, File out, String statusText) {
        try {
            FileUtils.write(out, name + "\r\n", Charset.defaultCharset(), true);
        } catch (IOException e) {
            System.out.println("Failed to write '" + name + "' to " + statusText +
                    " LIST    !!!!!!!!!!!!!!!!!");
            e.printStackTrace();
        }
    }
}
