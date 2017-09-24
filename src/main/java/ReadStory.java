import com.fasterxml.jackson.databind.ObjectMapper;
import entities.Chapter;
import entities.Event;
import entities.Profile;
import entities.Relation;
import extractors.EventExtractor;
import extractors.ProfileExtractor;
import extractors.RelationExtraction;
import extractors.personalityExtractor;
import org.lambda3.graphene.core.Graphene;
import org.lambda3.graphene.core.coreference.model.CoreferenceContent;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class ReadStory {

    public void returnJson(String inputFileAddress, String outFolderAddress, String mode, List<String> pipeLine) {
        try {
            String content = "";
            File file = new File(inputFileAddress);
            String inputFileName = file.getName().substring(0, file.getName().lastIndexOf((".")));

            if (mode.toLowerCase().trim().equals("interview")) {
                content = readFile(inputFileAddress);
            } else if (mode.toLowerCase().trim().equals("story")) {
                content = doCoreference(inputFileAddress);
            } else {
                content = readFile(inputFileAddress);
            }


            if (pipeLine.contains("temporal")) {
                Map<String, List> chapters = extractChapters(content, mode);
                convertToJson(chapters, outFolderAddress + "/chapters_" + inputFileName + ".json");
            }
            if (pipeLine.contains("profile")) {
                List<Profile> profiles = extractProfiles(content, mode);
                if (pipeLine.contains("relation")) {
                    List<Relation> relations = extractRelations(profiles, content);
                    convertToJson(relations, outFolderAddress + "/relations_" + inputFileName + ".json");
                }
                convertToJson(profiles, outFolderAddress + "/profiles_" + inputFileName + ".json");
            }
            if (pipeLine.contains("event")) {
                List<Event> events = extractEvents(content);
                convertToJson(events, outFolderAddress + "/events_" + inputFileName + ".json");

            }
            System.out.print("you can reach your created json files under this address:" + outFolderAddress);
        } catch (Exception ex) {
            System.out.print("ERROR IN main function " + ex.getMessage());
        }
    }

    private String readFile(String inputFileAddress) {
        try {
            Path ph = Paths.get(inputFileAddress);
            String content = new String(Files.readAllBytes(ph));
            return content;
        } catch (Exception ex) {
            System.out.print("ERROR IN readFile for interview mode:" + ex.getMessage());
            return "";
        }

    }

    private String doCoreference(String fileAddress) {

        try {
            Graphene graphene = new Graphene();
            StringBuilder resolvedContent = new StringBuilder();
            Path ph = Paths.get(fileAddress);
            String content = new String(Files.readAllBytes(ph));
            String PARAGRAPH_SPLIT_REGEX = "(?m)(?=^\\s{4})";

            String[] paragraphs = content.split(PARAGRAPH_SPLIT_REGEX);
            //for timeline extract purpose we want to break between paragraphs
            for (String paragraph : paragraphs) {
                CoreferenceContent coreferenceContent = graphene.doCoreference(paragraph);
                resolvedContent.append(System.getProperty("line.separator"));
                resolvedContent.append(System.getProperty("line.separator"));
                resolvedContent.append(System.getProperty("line.separator"));
                resolvedContent.append(System.getProperty("line.separator"));
                resolvedContent.append(coreferenceContent.getSubstitutedText());
            }
            return resolvedContent.toString();

        } catch (Exception ex) {
            System.out.print("ERROR IN doCoreference:" + ex.getMessage());
            return "";
        }
    }

    private List<Profile> extractProfiles(String content, String mode) {
        try {
            boolean useQuote = true;
            ProfileExtractor profileExtractor = new ProfileExtractor();
            List<Profile> profiles = profileExtractor.extract(content, useQuote);
            personalityExtractor pExtractor = new personalityExtractor(profiles);
            return pExtractor.extract(content, mode);
        } catch (Exception ex) {
            System.out.print("ERROR IN extractProfiles: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    private Map<String, List> extractChapters(String content, String mode) {
        String PARAGRAPH_SPLIT_REGEX = "(?m)(?=^\\s{4})";
        List<Chapter> chapters = new ArrayList<>();
        String[] chaptersContent = content.split(PARAGRAPH_SPLIT_REGEX);
        List<String> profileNames = new ArrayList<>();
        int chapterIndex = 1;
        for (String chapterContent : chaptersContent) {
            Chapter chapter = new Chapter();
            List<Profile> chapterProfiles = extractProfiles(chapterContent, mode);
            HashMap<String, Map<String, Integer>> info = new HashMap<>();
            for (Profile profile : chapterProfiles) {
                info.put(profile.getName(), profile.getAdjs());
                if (!profileNames.contains(profile.getName()))
                    profileNames.add(profile.getName());
            }
            chapter.setProfiles(info);
            chapter.setChapterIndex(chapterIndex);
            chapters.add(chapter);
            chapterIndex++;
        }
        Map<String, List> chaptersEntry = new HashMap<>();
        chaptersEntry.put("profileNames", profileNames);
        chaptersEntry.put("chapters", chapters);
        return chaptersEntry;
    }

    private List<Relation> extractRelations(List<Profile> profiles, String content) {
        try {
            RelationExtraction relationExtraction = new RelationExtraction(profiles);
            return relationExtraction.extract(content);
        } catch (Exception ex) {
            System.out.print("Error in Relation extraction: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Event> extractEvents(String content) {
        try {
            EventExtractor eventExtractor = new EventExtractor();
            return eventExtractor.extract(content);
        } catch (Exception ex) {
            System.out.print("ERROR IN extractEvents: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    private void convertToJson(List objects, String fileAddress) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File(fileAddress), objects);
        } catch (Exception ex) {
            System.out.print("ERROR IN convertToJson: " + ex.getMessage());
        }
    }


    private void convertToJson(Map objects, String fileAddress) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File(fileAddress), objects);
        } catch (Exception ex) {
            System.out.print("ERROR IN convertToJson: " + ex.getMessage());
        }
    }
}
