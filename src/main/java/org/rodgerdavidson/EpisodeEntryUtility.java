package org.rodgerdavidson;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.rodgerdavidson.xml.model.Episodedetails;


public class EpisodeEntryUtility {
    private final EdgeDriver edge;
    private final JavascriptExecutor js;
    private final TrackingFileUtils fileUtils;
    private String name;
    private Episodedetails details;


    public EpisodeEntryUtility(EdgeDriver edge) {
        this.edge = edge;
        js = (JavascriptExecutor) edge;
        fileUtils = new TrackingFileUtils();
    }


    public void updateEpisodeDetails(String name, Episodedetails details) {
        this.name = name;
        this.details = details;
        System.out.println("Attempting to update episode details for: " + name);

        if (openMediaEdit()) {
            addDetailsForEpisode();
        }
    }


    private void addDetailsForEpisode() {
        System.out.println("Keying details for " + name);
        enterVideoInfo();
        enterPoster();
        submit();
        System.out.println("completed episode " + name);
    }


    private void enterPoster() {
        edge.findElement(By.xpath(".//*[text()='Poster']")).click();
        edge.findElement(By.xpath(".//*[text()='Enter URL']")).click();
        sendKeys(By.name("url"), details.getThumb(), "Thumbnail URL", true);
    }


    private void enterVideoInfo() {
        edge.findElement(By.xpath(".//*[text()='Video Info']")).click();
        sendKeys(By.name("title"), details.getShowtitle(), "TV Show Title", true);
        sendKeys(By.name("tagline"), details.getTitle(), "Episode Title", true);
        sendKeys(By.name("season"), String.valueOf(details.getSeason()), "Season", true);
        sendKeys(By.name("episode"), String.valueOf(details.getEpisode()), "Episode", true);
        sendKeys(By.name("certificate"), details.getMpaa(), "Classification (aka MPAA Rating)", true);
        details.getGenre().forEach(genre -> sendKeys(By.xpath("//*[@name=\"genre\"][last()]"), genre + Keys.RETURN, "Genre", false));
        details.getActor().forEach(actor -> sendKeys(By.xpath("//*[@name=\"actor\"][last()]"), actor.getName() + Keys.RETURN, "Cast", false));
        if (details.getCredits() != null && details.getCredits().length() > 0) {
            for (String name : details.getCredits().split(", ")) {
                sendKeys(By.xpath("//*[@name=\"writer\"][last()]"), name + Keys.RETURN, "Writer", false);
            }
        }
        if (details.getDirector() != null && details.getDirector().length() > 0) {
            for (String name : details.getDirector().split(", ")) {
                sendKeys(By.xpath("//*[@name=\"director\"][last()]"), name + Keys.RETURN, "Writer", false);
            }
        }
        sendKeys(By.name("summary"), details.getPlot(), "Summary", true);

        enterDate(By.name("tvshow_original_available"), details.getPremiered(), "TV Series Air Date");
        enterDate(By.name("original_available"), details.getAired(), "Release Date (Episode)");
    }


    private void enterDate(By selector, String date, String logName) {
        if (date == null || date.equals("") || date.length() < 10) return;
        System.out.println("Updating date '" + logName + "' to " + date + " using " + selector.toString());
        js.executeScript("arguments[0].scrollIntoView();", edge.findElement(selector));
        js.executeScript("arguments[0].value='" + date + "';", edge.findElement(selector));
        edge.findElement(selector).click();
        edge.findElement(By.xpath(".//*[text()='Edit video info']")).click();
    }


    private void sendKeys(By elementSelector, String data, String logTitle, boolean clearField) {
        if (data == null || data.equals("") || data.length() < 1) return;
        try {
            String tagName = edge.findElement(elementSelector).getTagName();
            if (tagName.equalsIgnoreCase("input") || tagName.equalsIgnoreCase("textArea")) {
                js.executeScript("arguments[0].scrollIntoView();", edge.findElement(elementSelector));
                edge.findElement(elementSelector).click();
                if (clearField) edge.findElement(elementSelector).clear();
                edge.findElement(elementSelector).sendKeys(data);
            } else {
                System.out.println("Element '" + elementSelector.toString() + "' is not INPUT type");
            }
        } catch (Exception e) {
            System.out.println("Unable to enter info for " + logTitle);
            e.printStackTrace();
        }
    }


    private void submit() {
        if (validateEntries()) {
            System.out.println("............. submitting form");
            edge.findElement(By.xpath(".//*[text()='  OK  ']")).click();
            new WebDriverWait(edge, 2).until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class, 'overwrite-policy-dialog')]")));
            edge.findElement(By.xpath("//label[text()='Replace']")).click();
            edge.findElement(By.xpath("//div[contains(@class, 'overwrite-policy-dialog')]//button[text()='  OK  ']")).click();
            fileUtils.writeEpisodeToSuccessFile(name);
        }
    }


    private boolean validateEntries() {
        edge.findElement(By.xpath(".//*[text()='Video Info']")).click();
        System.out.println("Validating entered data for: " + name);
        if (edge.findElement(By.name("title")).getAttribute("value").equalsIgnoreCase(details.getShowtitle()) &&
                edge.findElement(By.name("tagline")).getAttribute("value").equalsIgnoreCase(details.getTitle()) &&
                edge.findElement(By.name("season")).getAttribute("value").equalsIgnoreCase(String.valueOf(details.getSeason())) &&
                edge.findElement(By.name("episode")).getAttribute("value").equalsIgnoreCase(String.valueOf(details.getEpisode())) &&
                edge.findElement(By.name("certificate")).getAttribute("value").equalsIgnoreCase(details.getMpaa()) &&
                edge.findElement(By.name("summary")).getAttribute("value").equalsIgnoreCase(details.getPlot()) &&
                matchDate(By.name("tvshow_original_available"), details.getPremiered()) &&
                matchDate(By.name("original_available"), details.getAired())
        ) {
            System.out.println("MATCHES!!!");
            return true;
        }
        cancelEdit();
        fileUtils.writeEpisodeToFailFile(name);
        return false;
    }


    private boolean matchDate(By selector, String date) {
        String input = edge.findElement(selector).getAttribute("value");
        String[] parse = input.split("/");
        String[] matchParse = date.split("-");

        if (parse[0].equalsIgnoreCase(matchParse[1]) &&
                parse[1].equalsIgnoreCase(matchParse[2]) &&
                parse[2].equalsIgnoreCase(matchParse[0])
        ) {
            return true;
        }
        return false;
    }


    private boolean openMediaEdit() {
        try {
            edge.findElement(By.className("background-poster"))
                    .findElements(By.xpath("//*[@aria-label='Action']"))
                    .get(0).click();
            ;
            try {
                edge.findElements(By.xpath(".//*[text()='Edit video info']")).get(0).click();
                SeleniumRunner.waitForAvailable(By.className("edit-meta-dialog"), edge);
                return true;
            } catch (Exception e) {
                System.out.println("Failed to open EDIT view   !!!!!!!!!");
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("Failed to open OPTIONS menu  !!!!!!");
            e.printStackTrace();
            fileUtils.writeEpisodeToFailFile(name);
        }

        return false;
    }


    public void cancelEdit() {
        edge.findElement(By.xpath("//button[text()='Cancel']")).click();
        new WebDriverWait(edge, 2).until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//button[text()='Yes']")));
        edge.findElement(By.xpath("//button[text()='Yes']")).click();
        new WebDriverWait(edge, 2).until(ExpectedConditions.invisibilityOfElementLocated(
                By.className("edit-meta-dialog")));
    }
}
