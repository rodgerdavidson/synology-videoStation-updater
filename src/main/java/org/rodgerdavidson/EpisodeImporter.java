package org.rodgerdavidson;

import java.io.File;
import java.util.HashMap;

import org.rodgerdavidson.xml.model.Episodedetails;
import javax.xml.bind.*;

public class EpisodeImporter {

    File searchFolder;

    public EpisodeImporter(String searchFolder) {
        this.searchFolder = new File(searchFolder);
    }


    public HashMap<String, Episodedetails> importAll() {
        HashMap<String, Episodedetails> loadedFileDetails = new HashMap<>();

        for (File currentFile : searchFolder.listFiles()) {
            if(!currentFile.getAbsolutePath().endsWith(".nfo")) continue;
            String episodeName = reformatFileName(currentFile.getName());

            if(currentFile.isFile() && currentFile.canRead()){
                try {
                    JAXBContext jc = JAXBContext.newInstance(Episodedetails.class);
                    Unmarshaller unmarshaller = jc.createUnmarshaller();
                    Episodedetails episode = (Episodedetails) unmarshaller.unmarshal(currentFile);
                    loadedFileDetails.put(episodeName, episode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return loadedFileDetails;
    }


    private String reformatFileName(String name) {
        return name
                .replace(".nfo", "")
                .replace("_", " ");
    }
}
