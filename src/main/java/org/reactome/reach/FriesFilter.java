package org.reactome.reach;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.gk.reach.model.fries.Entity;
import org.gk.reach.model.fries.Event;
import org.gk.reach.model.fries.FrameCollection;
import org.gk.reach.model.fries.FriesObject;
import org.gk.reach.model.fries.Sentence;
import org.gk.reach.ReachUtils;

public class FriesFilter {

    public FriesFilter() {
    }
    
    private FrameCollection<Event> getEventsFromFile(Path friesFile) throws IOException {
        FriesObject friesObject = null;
        List<Event> eventFrames = null;

        if (!friesFile.toString().endsWith(".json"))
            return null;
        friesObject = ReachUtils.readFile(friesFile.toString());
        eventFrames = friesObject.getEvents().getFrameObjects(); 
        // If Events is empty, return null.
        if (eventFrames.size() == 0)
            return null;

        // For all Events in the file.
        FrameCollection<Event> eventCollection = new FrameCollection<Event>();
        for (Event event : eventFrames) {
            // If the number of participants is less than 2, continue.
            if (event.getArguments().size() < 2)
                continue;
            // Otherwise, add the event to the collection.
            eventCollection.addFrameObject(event);
        }

        return eventCollection;
    }
    
    private void writeFriesFile(Path friesFile, FriesObject friesObject) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Write FriesObject copy to the file copy.
        // TODO Only write the Entity and Sentence objects that are referred
        // to by the written Events.
        mapper.writeValue(friesFile.toFile(), friesObject);
    }
    
    private FriesObject createFriesObject(FrameCollection<Event> events,
                                          FrameCollection<Entity> entities,
                                          FrameCollection<Sentence> sentences) {
        FriesObject friesObject = new FriesObject();
        friesObject.setEvents(events);
        friesObject.setEntities(entities);
        friesObject.setSentences(sentences);

        return friesObject;
    }

    public static void main(String[] args) throws Exception {
        String inputDir = "/Users/beckmanl/Documents/reach/output/testing/output2/";
        String outputDir = "/Users/beckmanl/Documents/reach/output/testing/output2/filtered/";
        FrameCollection<Event> eventCollection = null;
        List<Path> friesFiles = FriesUtils.getFilesInDir(Paths.get(inputDir));
        FriesObject friesObjectCopy = new FriesObject();
        FriesObject friesObject = null;
        FriesFilter friesFilter = new FriesFilter();
        String progress = null;
        int i = 0;

        // For all JSON FRIES files (in the input directory).
        for (Path friesFile : friesFiles) {
            eventCollection = friesFilter.getEventsFromFile(friesFile);
            if (eventCollection.getFrameObjects() == null || eventCollection.getFrameObjects().size() == 0) {
                System.out.println(friesFile.getFileName().toString() + " -> No events found.");
                i++;
                continue;
            }
            
            friesObject = ReachUtils.readFile(friesFile.toString());
            friesObjectCopy = friesFilter.createFriesObject(eventCollection,
                                                            friesObject.getEntities(),
                                                            friesObject.getSentences());

            // Write new file to output directory.
            friesFilter.writeFriesFile(Paths.get(outputDir), friesObjectCopy);
            progress = FriesUtils.getProgress(friesFile.getFileName().toString(), ++i, friesFiles.size());
            System.out.println(progress);
        }
    }
}
