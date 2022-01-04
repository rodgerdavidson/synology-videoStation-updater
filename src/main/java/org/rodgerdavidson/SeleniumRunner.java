package org.rodgerdavidson;


import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.rodgerdavidson.xml.model.Episodedetails;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SeleniumRunner {
    public static final String VIDEO_STATION_APP_URL = "synology-video-station-app-url";

    public static final String SEARCH_AND_FILTER_BOX_ID = "ext-comp-1022";
    public static final String FILTER_BUTTON_ID = "ext-comp-1079";
    private EdgeDriver edge;


    public SeleniumRunner() {
        WebDriverManager.edgedriver().setup();
        edge = new EdgeDriver();
        edge.manage().window().maximize();
    }


    public void openSynologyVideoStation(Properties props) {
        edge.get(props.getProperty(VIDEO_STATION_APP_URL));
        edge.findElementById("details-button").click();
        edge.findElementById("proceed-link").click();

        LoginUtility loginUtility = new LoginUtility(edge, props);
        loginUtility.login();

        filterMainPage("TESTING", false, edge);
    }


    public static void filterMainPage(String input, boolean submit, EdgeDriver edge) {
        waitForAvailable(By.id(SEARCH_AND_FILTER_BOX_ID), edge);
        WebElement search = edge.findElementById(SEARCH_AND_FILTER_BOX_ID);
        search.click();
        search.clear();
        search.sendKeys(input);
        if(submit){
            search.sendKeys(Keys.RETURN);
        }
    }


    public static void waitForAvailable(By elementLocator, EdgeDriver edge) {
        System.out.println("Waiting 10 seconds for " + elementLocator.toString() + "..................................");
        new WebDriverWait(edge, 10)
                .until(ExpectedConditions.presenceOfElementLocated(elementLocator));
    }


    public void processEpisodes(Map<String, Episodedetails> episodes) {
        List<String> lines = new ArrayList<>();
        try {
            File file = new File(TrackingFileUtils.COMPLETED_EPISODES_TXT);
            if(file.exists() && file.canRead()) lines = FileUtils.readLines(file, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        lines.forEach(ep -> episodes.remove(ep));

        episodes.forEach((name, episodedetails) -> {
            addDetailsToVideoStation(name, episodedetails);
        });
    }


    private void addDetailsToVideoStation(String name, Episodedetails details) {
        EpisodeFinder finder = new EpisodeFinder(edge);
        if (finder.searchAndSelectEpisode(name)) {
            EpisodeEntryUtility updater = new EpisodeEntryUtility(edge);
            updater.updateEpisodeDetails(name, details);
        }
    }
}
