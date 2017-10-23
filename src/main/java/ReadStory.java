import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.IntPair;
import entities.Chapter;
import entities.Event;
import entities.Profile;
import entities.Relation;
import extractors.*;
import org.lambda3.graphene.core.Graphene;
import org.lambda3.graphene.core.coreference.model.CoreferenceContent;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class ReadStory {

    boolean profileContainsCheck;
    boolean removeProfilesWitoutInfo;
    boolean removeProfilesWithFreq1;
    boolean removeEventsWithoutLocationAndObejct;
    boolean removeEventsWithNoCharacterObject;
    boolean extractPersonalityAndOrg ;
    boolean coreferenceWithCoreNLP;
    boolean useQuote;
    boolean useNLPForQuoteExtraction;
    List<Profile> profiles;

    public ReadStory(boolean profileContainsCheck, boolean removeProfilesWitoutInfo, boolean removeProfilesWithFreq1, boolean removeEventsWithoutLocationAndObejct, boolean removeEventsWithNoCharacterObject, boolean extractPersonalityAndOrg, boolean coreferenceWithCoreNLP, boolean useQuote, boolean useNLPForQuoteExtraction) {
        this.profileContainsCheck = profileContainsCheck;
        this.removeProfilesWitoutInfo = removeProfilesWitoutInfo;
        this.removeProfilesWithFreq1 = removeProfilesWithFreq1;
        this.removeEventsWithoutLocationAndObejct = removeEventsWithoutLocationAndObejct;
        this.removeEventsWithNoCharacterObject = removeEventsWithNoCharacterObject;
        this.extractPersonalityAndOrg = extractPersonalityAndOrg;
        this.coreferenceWithCoreNLP = coreferenceWithCoreNLP;
        this.useQuote = useQuote;
        this.useNLPForQuoteExtraction = useNLPForQuoteExtraction;
    }

    public void returnJson(String inputFileAddress, String outFolderAddress, String mode, List<String> pipeLine) {
        try {

            String content = "";
            File file = new File(inputFileAddress);
            String inputFileName = file.getName().substring(0, file.getName().lastIndexOf((".")));
            profiles = new ArrayList<>();

            if (mode.toLowerCase().trim().equals("interview")) {
                content = readFile(inputFileAddress);
            } else if (mode.toLowerCase().trim().equals("story") && !coreferenceWithCoreNLP) {
                content = doCoreference(inputFileAddress);
            } else if (mode.toLowerCase().trim().equals("story") && coreferenceWithCoreNLP) {
                //this method not only resolve the text, but also create an array of
                //profiles and add the profile roles to it.
                content = doCoreference_extractRoles_CoreNLP(inputFileAddress);
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(inputFileName + "_resolvedByCoreNLP.txt"), "utf-8"))) {
                    writer.write(content);
                }catch (Exception ex){
                    System.out.println(ex.getMessage());
                }
            } else {
                content = readFile(inputFileAddress);
            }


            if (pipeLine.contains("temporal")) {
                Map<String, List> chapters = extractChapters(content, mode);
                convertToJson(chapters, outFolderAddress + "/chapters_" + inputFileName + ".json");
            }
            if (pipeLine.contains("profile")) {
                profiles = extractProfiles(content, mode, false);
                if (pipeLine.contains("relation")) {
                    List<Relation> relations = extractRelations(content);
                    convertToJson(relations, outFolderAddress + "/relations_" + inputFileName + ".json");
                }
                convertToJson(profiles, outFolderAddress + "/profiles_" + inputFileName + ".json");
            }
            if (pipeLine.contains("event")) {
                List<Event> events = extractEvents(content, removeEventsWithoutLocationAndObejct, removeEventsWithNoCharacterObject);
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

        System.out.println("Start do Co-reference With PyCobalt" + getTime());
        try {
            Graphene graphene = new Graphene();
            StringBuilder resolvedContent = new StringBuilder();
            Path ph = Paths.get(fileAddress);
            String content = new String(Files.readAllBytes(ph));
            String PARAGRAPH_SPLIT_REGEX = "\n\n\n\n";

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
            System.out.println("End do Co-reference With PyCobalt " + getTime());
            return resolvedContent.toString();

        } catch (Exception ex) {
            System.out.print("ERROR IN doCoreference:" + ex.getMessage());
            return "";
        }

    }

    public String doCoreference_extractRoles_CoreNLP(String fileAddress) {

        System.out.println("Start do Co-reference With NLP " + getTime());
        try {

            Map<String, Map<String, String>> sentenceResolver = new LinkedHashMap<>();
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);


            Path ph = Paths.get(fileAddress);
            String content = new String(Files.readAllBytes(ph));
            String PARAGRAPH_SPLIT_REGEX = "\n\n\n\n";
            String[] paragraphs = content.split(PARAGRAPH_SPLIT_REGEX);
            //for timeline extract purpose we want to break between paragraphs
            for (String paragraph : paragraphs) {

                Annotation document = new Annotation(paragraph);

                pipeline.annotate(document);
                Map<Integer, CorefChain> coref = document.get(edu.stanford.nlp.coref.CorefCoreAnnotations.CorefChainAnnotation.class);
                List<CoreMap> senences = document.get(CoreAnnotations.SentencesAnnotation.class);
                for (CoreMap sen : senences) {
                    sentenceResolver.put(sen.toString(), new HashMap<>());
                    getAllProfiles(sen);
                }
                for (Map.Entry<Integer, CorefChain> entry : coref.entrySet()) {
                    CorefChain c = entry.getValue();

                    //this is because it prints out a lot of self references which aren't that useful
                    if (c.getMentionsInTextualOrder().size() <= 1)
                        continue;

                    CorefChain.CorefMention cm = c.getRepresentativeMention();
                    String clust = "";
                    List<CoreLabel> tks = document.get(CoreAnnotations.SentencesAnnotation.class).get(cm.sentNum - 1).get(CoreAnnotations.TokensAnnotation.class);
                    for (int i = cm.startIndex - 1; i < cm.endIndex - 1; i++)
                        clust += tks.get(i).get(CoreAnnotations.TextAnnotation.class) + " ";
                    clust = clust.trim();
                    System.out.println("representative mention: \"" + clust + "\" is mentioned by:");

                    for (CorefChain.CorefMention m : c.getMentionsInTextualOrder()) {//check all the cases which are referred to clust
                        // for each case we know its sentence index
                        String clust2 = "";
                        tks = document.get(CoreAnnotations.SentencesAnnotation.class).get(m.sentNum - 1).get(CoreAnnotations.TokensAnnotation.class);
                        for (int i = m.startIndex - 1; i < m.endIndex - 1; i++) {
                            clust2 += tks.get(i).get(CoreAnnotations.TextAnnotation.class) + " ";
                        }
                        clust2 = clust2.trim();
                        //don't need the self mention
                        if (clust.equals(clust2))
                            continue;

                        String currSentence = document.get(CoreAnnotations.SentencesAnnotation.class).get(m.sentNum - 1).toString();
                        Profile profile = fetchProfile(clust);
                        if (profile != null) {
                            Map<String, String> replaces = sentenceResolver.get(currSentence);
                            if (m.mentionType.name().equals("PRONOMINAL"))
                                replaces.put(clust2, profile.getName());
                            else if (m.mentionType.name().equals("NOMINAL") && m.animacy.name().equals("ANIMATE")) {
                                profile.addToAdjs(clust2, false);
                            }
                        }
                        System.out.println("\t" + clust2);
                    }
                }
            }


            StringBuilder resolvedContent = new StringBuilder();
            int line= 0;
            for (String sentence : sentenceResolver.keySet()) {
                Map<String, String> replaces = sentenceResolver.get(sentence);
                for (String curr : replaces.keySet()) {
                    String resolved = replaces.get(curr);
                    if(sentence.indexOf(curr) == 0){
                        sentence = sentence.replace(curr + " ", " " + resolved + " ");
                    }else
                        sentence = sentence.replace(" " + curr + " ", " " + resolved + " ");
                        sentence = sentence.replace(" " + curr + ",", " " + resolved + " ");
                        sentence = sentence.replace("," + curr + " ", " " + resolved + " ");
                        sentence = sentence.replace("." + curr + " ", " " + resolved + " ");
                        sentence = sentence.replace("[',\",´,`]" + curr + " ", " " + resolved + " ");
                }
                resolvedContent.append(" " + sentence);
                line++;
                if(line == 7){
                    line = 0;
                    resolvedContent.append(System.getProperty("line.separator"));
                    resolvedContent.append(System.getProperty("line.separator"));
                    resolvedContent.append(System.getProperty("line.separator"));
                    resolvedContent.append(System.getProperty("line.separator"));
                }
            }
            System.out.println("End do Co-reference With NLP " + getTime());
            return resolvedContent.toString();

        } catch (Exception ex) {
            System.out.print("ERROR IN doCoreference:" + ex.getMessage());
            return "";
        }
    }

    private List<Profile> extractProfiles(String content, String mode, boolean forTemporalExtraction) {
        try {

            System.out.println("Start do Profile " + getTime());
            ProfileExtractor profileExtractor = new ProfileExtractor();
            if(coreferenceWithCoreNLP){
                profileExtractor.addPropertiesToCurrentProfiles(profiles, content);
                System.out.println("End do Profile " + getTime());
                if(!forTemporalExtraction) {
                    System.out.println("Start do Personality & Quote " + getTime());
                    personalityExtractor pExtractor = new personalityExtractor(profiles, profileContainsCheck, useNLPForQuoteExtraction);
                    List<Profile> result = pExtractor.extract(content, mode);
                    System.out.println("End do Personality & Quote " + getTime());
                    return result;
                }else{
                    return profiles;
                }

            }else{
                List<Profile> profiles_beforePers = profileExtractor.extract(content, useQuote, profileContainsCheck, removeProfilesWitoutInfo, removeProfilesWithFreq1);
                System.out.println("End do Profile" + getTime());
                if(!forTemporalExtraction) {
                    System.out.println("Start do Personality & Quote " + getTime());
                    personalityExtractor pExtractor = new personalityExtractor(profiles_beforePers, profileContainsCheck, useNLPForQuoteExtraction);
                    List<Profile> result = pExtractor.extract(content, mode);
                    System.out.println("End do Personality & Quote " + getTime());
                    return result;
                }else{
                    return profiles_beforePers;
                }

            }

        } catch (Exception ex) {
            System.out.print("ERROR IN extractProfiles: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    private Map<String, List> extractChapters(String content, String mode) {
        System.out.println("Start do Temporal " + getTime());
        String PARAGRAPH_SPLIT_REGEX = "\\n\\n\\n\\n";
        List<Chapter> chapters = new ArrayList<>();
        String[] chaptersContent = content.split(PARAGRAPH_SPLIT_REGEX);
        List<String> profileNames = new ArrayList<>();
        int chapterIndex = 1;
        for (String chapterContent : chaptersContent) {
            Chapter chapter = new Chapter();
            List<Profile> chapterProfiles = extractProfiles(chapterContent, mode, true);
            HashMap<String, Map<String, Integer>> info = new HashMap<>();
            for (Profile profile : chapterProfiles) {
                if (profile.getAdjs().size() > 0) {
                    info.put(profile.getName(), profile.getAdjs());
                    if (!profileNames.contains(profile.getName()))
                        profileNames.add(profile.getName());
                }
            }
            if (info.size() > 0) {
                chapter.setProfiles(info);
                chapter.setChapterIndex(chapterIndex);
                chapters.add(chapter);
                chapterIndex++;
            }
        }
        Map<String, List> chaptersEntry = new HashMap<>();
        chaptersEntry.put("profileNames", profileNames);
        chaptersEntry.put("chapters", chapters);
        System.out.println("End do Temporal " + getTime());
        return chaptersEntry;
    }

    private List<Relation> extractRelations(String content) {
        try {
            System.out.println("Start do Relation " + getTime());
            RelationExtraction relationExtraction = new RelationExtraction(profiles);
            List<Relation> result = relationExtraction.extract(content);
            System.out.println("End do Relation " + getTime());
            return result;
        } catch (Exception ex) {
            System.out.println("Error in Relation extraction: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Event> extractEvents(String content, boolean removeEventsWithoutLocationAndObejct, boolean removeEventsWithNoCharacterObject) {
        try {
            System.out.println("Start do Event " + getTime());
            EventExtractor eventExtractor = new EventExtractor();
            List<Event> result = eventExtractor.extract(content, profiles, removeEventsWithoutLocationAndObejct, removeEventsWithNoCharacterObject);
            System.out.println("End do Event " + getTime());
            return result;
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

    private void getAllProfiles(CoreMap sen) {
        List<CoreLabel> coreLabels = sen.get(CoreAnnotations.TokensAnnotation.class);
        for (int i = 0; i < coreLabels.size(); i++) {
            CoreLabel coreLabel = coreLabels.get(i);
            String namedEntity = "";
            String category = coreLabel.ner();
            if (category.equals("PERSON") || (extractPersonalityAndOrg && category.equals("ORGANIZATION"))) {

                while (category.equals("PERSON") || (extractPersonalityAndOrg && category.equals("ORGANIZATION"))) {
                    namedEntity += " " + coreLabel.word();
                    coreLabel = coreLabels.get(++i);
                    category = coreLabel.ner();
                }


                Profile profile = fetchProfile(namedEntity);
                if (profile == null) {
                    profile = new Profile();
                    profile.setName(namedEntity);
                    profiles.add(profile);
                } else {
                    profile.addFrequency();
                }
            }
        }
    }

    private Profile fetchProfile(String name) {
        String namedEntityClean = name.trim().toLowerCase().replaceAll("[',\",´,`]", "");
        for (Profile profile : profiles) {
            String profileName = profile.getName().trim().toLowerCase();
            if ((profileContainsCheck && (profileName.contains(namedEntityClean) || namedEntityClean.contains(profileName)))
                    ||
                    (!profileContainsCheck && profileName.equals(namedEntityClean))) {
                return profile;
            }
        }
        return null;
    }

    private String getTime(){
        DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        Date dateobj = new Date();
        return df.format(dateobj);
    }

}
