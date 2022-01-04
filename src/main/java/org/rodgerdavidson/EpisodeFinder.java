package org.rodgerdavidson;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;

public class EpisodeFinder {
    private final EdgeDriver edge;
    private TrackingFileUtils fileUtils;


    public EpisodeFinder(EdgeDriver edge) {
        this.edge = edge;
        fileUtils = new TrackingFileUtils();
    }


    public boolean searchAndSelectEpisode(String name) {
        if (selectTvHome()) {
            if (filterByEpisodeName(name)) {
                if (selectUnknownEpisode(name)) {
                    return true;
                }
            }
        }
        return false;
    }


    public boolean selectUnknownEpisode(String name) {
        System.out.println("Wait for Series page to load:" + name);
        SeleniumRunner.waitForAvailable(By.id("ext-comp-1253"), edge);

        String title = null;
        int count = 0;
        do {
            try {
                Thread.sleep(500);
                title = edge.findElementById("ext-comp-1253")
                        .findElement(By.className("title"))
                        .getText();
            } catch (Exception e) {
                count++;
                e.printStackTrace();
                System.out.println("Waiting for series title");
            }
        } while (title == null || title.equalsIgnoreCase("") || title.trim().length() < 1);


        if (
                title.equalsIgnoreCase(name)
        ) {
            System.out.println("Series page found and loaded");
            String containerId = "ext-gen428";

            try {
                findTitleWithinContainer(name, containerId);
                return true;
            } catch (Exception w) {
                System.out.println("Unable to locate episode by expected title: " + name);
                w.printStackTrace();
                try {
                    findTitleWithinContainer("Unknown", containerId);
                    return true;
                } catch (Exception e) {
                    System.out.println("Unable to locate 'Unknown' episode title");
                    e.printStackTrace();
                    fileUtils.writeEpisodeToFailFile(name);
                }
            }
        } else {
            System.out.println("Unable to confirm series matches episode title");
            fileUtils.writeEpisodeToFailFile(name);
        }
        return false;
    }


    public boolean filterByEpisodeName(String name) {
        SeleniumRunner.filterMainPage(name, true, edge);
        try {
            findTitleWithinContainer(name, "ext-comp-1109");
            return true;
        } catch (Exception e) {
            System.out.println("!!!!   Could not click TV Show: " + name +
                    ".......................................  !!!!!!!!");
            e.printStackTrace();
        }
        fileUtils.writeEpisodeToFailFile(name);
        return false;
    }


    private void findTitleWithinContainer(String name, String containerId) throws Exception {
        long waitFor = 500;
        int count = 0;

        WebElement container = null;
        do {
            try {
                container = edge.findElementById(containerId);   // get results container
                System.out.println(container.getAttribute("role") + ": " + container.getAttribute("class")
                        + ": " + container.getAttribute("id"));
            } catch (Exception e) {
                count++;
                System.out.println("Failed to find container.............. attempt: " + count);
                e.printStackTrace();
                Thread.sleep(waitFor);
            }
        } while (count < 3 && null == container);

        count = 0;

        WebElement episodeTitle = null;
        do {
            try {
                episodeTitle = container.findElement(By.xpath(".//*[text()='" + name + "']"));   // get title element with show name
                System.out.println(episodeTitle.getAttribute("ext:qtip"));
            } catch (Exception e) {
                count++;
                System.out.println("Failed to find episode title.............. attempt: " + count);
                e.printStackTrace();
                Thread.sleep(waitFor);
            }
        } while (count < 3 && null == episodeTitle);

        count = 0;
        WebElement poster = null;
        do {
            try {
                poster = episodeTitle.findElement(By.xpath("./.."));     // get Parent
                System.out.println(poster.getAttribute("class"));
            } catch (Exception e) {
                count++;
                System.out.println("Failed to find episode poster.............. attempt: " + count);
                e.printStackTrace();
                Thread.sleep(waitFor);
            }
        } while (count < 3 && null == poster);

        poster.click();

        System.out.println("Selected show: " + name);
    }


    public boolean selectTvHome() {
        try {
            WebElement tvShowButton = edge.findElement(By.xpath("//div[@class='text vs-ellipsis' and text()='TV Show']"));
            System.out.println(tvShowButton.getAttribute("ext:qtip"));

            tvShowButton.click();
            System.out.println("Selected TV Show home......................................");

            SeleniumRunner.waitForAvailable(By.id("ext-gen123"), edge);
            return true;
        } catch (Exception e) {
            System.out.println("!!!!   Could not click TV Show home.......................................  !!!!!!!!");
            e.printStackTrace();
        }
        return false;
    }
}
