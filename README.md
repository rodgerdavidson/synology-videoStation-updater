# Synology VideoStation Updater
_Update TV Show metadata in Synology's VideoStation from Kodi-style NFO files with episode details._


## What's it about?
For years, I've used Kodi to manage and watch my video library.  Recently, I bought a Synology NAS, which comes with its own software - VideoStation.  

When I moved my files over, I found that movies generally scraped metadata pretty well, but not so much for TV shows.  Without metadata, every episode shows up as its own series in VideoStation, making the interface a mess.

It seems there was no ability out of the box to scrape from the TV DB.  Which is dumb.  Apparently, TV DB went to a pay-for model (which is dumb), so Synology (and everyone else) said "Hell with that" and provides no interface at all.

If there was already an adapter, I might have paid for a TV DB api-key long enough to scrape my collection, but there was no way I was paying them for the privilege of writing my own adapter.  Besides, I already had all the data I wanted in Kodi.  

I just needed to copy it over.  So, here we are....


## The Basics
This program will sweep a directory of NFO files, read them all, then 'key in' the data into VideoStation through the web interface.

It only works for TV Shows, though if you want, you could probably modify it for movies.

The NFO should be Kodi-style metadata.  Kodi lets you export metadata to either a big single file or individual files; this app is expecting separate files -- one for each episode, named the same as the corresponding episode file.


## Caveats and things to know
I'll be the first to tell you: "this is the wrong way to do it."

Not just because there should be an adapter included and paid for by Synology.  My solution is an overall bad approach. It's a hack and a workaround, but it worked for me.  

The _right way_ would be to authenticate and send a well-formatted JSON to an exposed endpoint for VideoStation.  I briefly explored doing that, but -- my God -- are Synology's APIs a mess.

So, in the end, the easiest way for my once-and-done update was to key the data through the browser the same way I would do it myself.

Which brings us to the first important thing:

###1) This isn't a refined user-friendly app
This is a back-end workaround solution.  A batch process, if you will.  You should expect to need some basic Java knowledge.

At minimum, you will also need:
* an IDE like [IntelliJ](https://www.jetbrains.com/idea/)
* the Java SE Development Kit [(JDK)](https://www.oracle.com/java/technologies/downloads/)
* and [Apache Maven](https://maven.apache.org/download.cgi)

###2) You'll need your VideoStation username and password
The system logs in the same way you would.  Because I was running it over and over, it was easier to put the login info into a properties file.  

You should add your username and password into :   **src/main/resources/runner.properties**

If you want to send code to the project, make sure you... **_DO NOT COMMIT YOUR PASSWORD OR USERNAME!!_**

While you're at it, update the **url** in the properties to point to your VideoStation instance.

For whatever reason, running this on my pc always prompted a certificate warning accessing VideoStation where I don't normally get one.
So, the program has a bit to click past that.  If you don't have that, you should go comment that out in the _SeleniumRunner.openSynologyVideoStation_ method.

One more note... if you are using _**two-factor authentication**_ (like you should), the program will pause for 10 seconds for you to enter the code.  You may have to change that up if you aren't using TFA, your fingers aren't fast enough, or if it doesn't work right for you.

###3) The program uses Microsoft Edge browser
Sorry, it just does.  If you want, feel free to replace 'EdgeDriver' with 'ChromeDriver' throughout.  I have no idea what surprises you may be in for.

###4) Xpath inconsistency
So, two things here... I got better at using xpaths to find things as I went, so you will see some that are more refined, and some that are crap.  They all work for me.  If Synology updates these in the next version of VideoStation, everything might break.  Who knows.  

If you get errors that something wasn't found, try again.  Sometimes, the browsers are just flukey.  (See above, this is the wrong solution.)  Sometimes it's a timing thing.  If you keep getting them, you'll have to pop open your browser developer tools and search up the target yourself to fix them.   

###5) Generated sources
In the _src/main/xsd_ folder you will find the xml schema for the NFO format that is used.  When Maven builds the app, it takes that file and generates the corresponding classes automagically.

In theory, that means that if you have a different version of the NFO file, you can go to one of those online schema generators, create a new xsd, and drop it in here instead and you are good to roll.  In practice, that only works if none of the main field names or formats have changed.  There may otherwise be a few other changes to make.

Also, in your IDE, it may flag all those model classes as 'missing'.   After your first build, you can go to the _target/generated-sources_ folder, and -- in IntelliJ -- right-click to mark the folder as 'Generated Sources Root'.  That should fix it for you.  I don't know how this is done in Eclipse.



## Okay, it's go time!
Running from within your IDE is relatively easy.

1) Open the project
2) Add your source folder (containing all your NFO files) into the **runner.properties**
3) Update the user and pw in **runner.properties**
4) Update the url in **runner.properties**
5) Have your two-factor verification code ready
6) Run the _main_ method in **SynologyUpdaterMain**

Repeat the above for any other TV show source folders (or change the program to recurse directories, take multiple folders, etc.)

## So, how did it work out for me?
I won't lie, I had to run it a number of times back-to-back to clear up my whole archive.  That's one of the reasons for the tracking files it creates: so that it doesn't have to re-try the same file after a success.

Much of the reason I had to run it more than once had to do with the state of my shows already loaded in VideoStation.  I'd made rather a mess trying to fix them in the UI myself before breaking down and writing this program.

A couple tips...
* Synology will sometimes group episodes together.  There's not always a good reason why.  But it makes it so they can't easily be found by the filename.  You can find them using the 'multiple files' filter on the TV Show search.  So glad when I found that.
* If your episode filename includes multiple spaces in a row, VideoStation will collapse those to a single space.  That will make a name mismatch when the application tries to find the Episode.  You cannot change VideoStation, so you must change the NFO filename to remove extra spaces.
* Any show that can't be found writes out to the fail file, you can use that to start your search for what the problem might be.
* If you really can't find an episode using keywords, try searching for the word 'Episode'.  I found VideoStation defaulted to, say, 'Episode 15' when only the show title was missing.

If you run into issues, keep an eye on the console output.  It should let you know whether Selenium was expecting something on screen it couldn't find, or if something else happened.  Use a debugger if you need to.

_The good side..._ the program won't submit any changes to your show without validating the fields match the NFO.


Good luck!