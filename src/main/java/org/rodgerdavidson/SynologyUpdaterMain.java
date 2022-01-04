package org.rodgerdavidson;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.rodgerdavidson.xml.model.Episodedetails;

public class SynologyUpdaterMain {
    public static final String RUNNER_PROPERTIES = "runner.properties";


    public static void main(String[] args) {
        SynologyUpdaterMain main = new SynologyUpdaterMain();
        Properties props = main.getProperties();

        EpisodeImporter episodeImporter = new EpisodeImporter(props.getProperty("search-folder-path"));
        Map<String, Episodedetails> episodes = episodeImporter.importAll();
        System.out.println("loaded: " + episodes.size());

        SeleniumRunner runner = new SeleniumRunner();
        runner.openSynologyVideoStation(props);
        runner.processEpisodes(episodes);
    }


    private Properties getProperties() {
        Properties props = new Properties();
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(RUNNER_PROPERTIES);
            props.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }
}
