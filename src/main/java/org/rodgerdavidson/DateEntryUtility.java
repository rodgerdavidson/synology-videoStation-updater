package org.rodgerdavidson;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.DateFormatSymbols;
import java.time.Month;
import java.util.Locale;

public class DateEntryUtility {
    private EdgeDriver edge;
    private JavascriptExecutor js;

    @Deprecated
    //  Was playing with this, but there is a much easier way
    public DateEntryUtility(EdgeDriver edge, JavascriptExecutor js) {
        this.edge = edge;
        this.js = js;
    }

    @Deprecated
    public void enterDate(By selector, String dateString, String logDescription) {
        System.out.println("Updating date field '" + logDescription + "' using " + selector.toString() + " and " + dateString);
        String year;
        String month;
        String day;

        if (dateString.charAt(4) == '-' && dateString.charAt(7) == '-') {
            String[] parsed = dateString.split("-");
            year = parsed[0];
            month = parsed[1];
            day = parsed[2];
        } else {
            System.out.println("!!!!  Unexpected Date Format  !!!!");
            System.out.println("Format should be 'yyyy-mm-dd'; received: " + dateString);
            return;
        }

        selectAndOpenDateFieldOnScreen(selector);
        searchAndSelectYear(year);
        searchAndSelectMonth(month);
        selectDay(year, month, day);
        System.out.println("Done date.... " + edge.findElement(selector).getAttribute("value"));
    }

    private void selectDay(String year, String monthNum, String day) {
        String month = DateFormatSymbols.getInstance().getMonths()[Integer.parseInt(monthNum) - -1];
        String formatted = year + " " + month + " " + day.replaceFirst("^0+(?!$)", "");
        new WebDriverWait(edge, 3)
                .until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//td[@class='x-date-active' and @aria-label='" + formatted + "']")));

        edge.findElement(By.xpath("//td[@class='x-date-active' and @aria-label='" + formatted + "']")).click();
    }

    private void searchAndSelectMonth(String monthNumTxt) {
        Month month = Month.of(Integer.parseInt(monthNumTxt));
        edge.findElement(By.className("x-date-picker")).findElement(By.className("month-btn-ct")).click();

        String shortMonth = DateFormatSymbols.getInstance().getShortMonths()[month.getValue() - 1];

        if (!edge.findElement(By.xpath("//a [div/span = '" + shortMonth + "' and @class='x-menu-list-item']")).isDisplayed()) {
            Month start = Month.valueOf(edge.findElement(By.className("x-date-picker"))
                    .findElement(By.className("month-btn-ct")).getText().toUpperCase(Locale.ROOT));
            int startNum = start.getValue();
            int modifier = 0;

            if (startNum == month.getValue()) {
                return;
            } else if (startNum < month.getValue()) {
                modifier = 2;
            } else {
                modifier = -2;
            }

            Actions actions = new Actions(edge);

            do {
                startNum += modifier;
                String next = DateFormatSymbols.getInstance().getShortMonths()[startNum - 1];
                edge.findElement(By.className("x-date-picker")).findElement(By.className("month-btn-ct")).click();
                new WebDriverWait(edge, 3)
                        .until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//a [div/span = '" + next + "' and @class='x-menu-list-item']")));

                actions.sendKeys(
                                edge.findElement(By.xpath("//a [div/span = '" + next + "' and @class='x-menu-list-item']")),
                                Keys.ARROW_UP)
                        .perform();

                new WebDriverWait(edge, 2)
                        .until(ExpectedConditions.invisibilityOfElementLocated(
                                By.xpath("//div[contains(@class,'syno-vs2-datefield-month-menu')]")));
            } while (Math.abs(startNum - month.getValue()) < 3);
        }

        edge.findElement(By.className("x-date-picker")).findElement(By.className("month-btn-ct")).click();
        edge.findElement(By.xpath("//a [div/span = '" + shortMonth + "' and @class='x-menu-list-item']")).click();
    }


    private void searchAndSelectYear(String year) {
        int startYr = Integer.parseInt(edge.findElement(By.className("x-date-picker")).findElement(By.className("year-btn-ct")).getText());
        int targetYr = Integer.parseInt(year);

        int modifier = 0;

        if (startYr == targetYr) {
            return;
        } else if (startYr < targetYr) {
            modifier = 3;
        } else {
            modifier = -3;
        }

        Actions actions = new Actions(edge);

        do {
            startYr += modifier;
            edge.findElement(By.className("x-date-picker")).findElement(By.className("year-btn-ct")).click();
            System.out.println("currently looking for: " + startYr + ", main selection: " +
                    edge.findElement(By.className("x-date-picker")).findElement(By.className("year-btn-ct")).getText());
            new WebDriverWait(edge, 3)
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//span[@class='x-menu-item-text' and text()= '" + startYr + "']/../..")));

            actions.sendKeys(
                            edge.findElement(By.className("syno-vs2-datefield-year-menu"))
                                    .findElement(By.xpath(".//*[text()='" + startYr + "']")), Keys.ARROW_UP)
                    .perform();

            new WebDriverWait(edge, 2)
                    .until(ExpectedConditions.invisibilityOfElementLocated(
                            By.className("syno-vs2-datefield-year-menu")
                    ));
        } while (Math.abs(startYr - targetYr) > 3);

        edge.findElement(By.className("x-date-picker")).findElement(By.className("year-btn-ct")).click();
        new WebDriverWait(edge, 1)
                .until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//span[@class='x-menu-item-text' and text()= '" + year + "']/../..")));
        edge.findElement(By.className("syno-vs2-datefield-year-menu")).findElement(By.xpath(".//*[text()='" + year + "']")).click();
    }

    private void selectAndOpenDateFieldOnScreen(By selector) {
        js.executeScript("arguments[0].scrollIntoView();", edge.findElement(selector));
        edge.findElement(selector).click();
    }
}
