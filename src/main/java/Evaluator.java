import com.fasterxml.jackson.databind.ObjectMapper;
import entities.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pegah on 9/24/17.
 */
public class Evaluator {

    private boolean quoteConsideration;

    public Evaluator(boolean quoteConsideration) {
        this.quoteConsideration = quoteConsideration;
    }

    public void strongCompareResults() {

        String content1= readFile("SampleArticle.txt");
        String content2= readFile("SampleStory.txt");
        String content3= readFile("Socrates.txt");
        String content4= readFile("SampleInterview.txt");
        System.out.println("average word count:" + (content1.split(" ").length + content2.split(" ").length + content3.split(" ").length +
                content4.split(" ").length)/4);

        List<Profile> predictedProfiles = readResult("out/profiles_SampleStoryResolved.json");
        Relation[] predictedRelations = readRelationResult("out/relations_SampleStoryResolved.json");
        RelationEvaluate[] groundTruthRelations = readGroundTruth_relations("relation-ground-truth-Arthur.json");
        ProfileEvaluator[] groundTruthProfiles = readGroundTruth("ground-truth-Arthur.json");

        List<String> TP_adj = new ArrayList<>();
        List<String> FP_adj = new ArrayList<>();
        List<String> FN_adj = new ArrayList<>();

        List<String> TP_verb = new ArrayList<>();
        List<String> FP_verb = new ArrayList<>();
        List<String> FN_verb = new ArrayList<>();


        List<String> TP_loc = new ArrayList<>();
        List<String> FP_loc = new ArrayList<>();
        List<String> FN_loc = new ArrayList<>();

        List<String> TP_sentiment = new ArrayList<>();
        List<String> FP_sentiment = new ArrayList<>();
        List<String> FN_sentiment = new ArrayList<>();

        List<String> TP_character = new ArrayList<>();
        List<String> FP_character = new ArrayList<>();
        List<String> FN_character = new ArrayList<>();


        for (ProfileEvaluator groundTruthProfile : groundTruthProfiles) {
            boolean isThereCharacter = false;
            for (Profile predictedProfile : predictedProfiles) {
                if (groundTruthProfile.getName().contains(predictedProfile.getName()) ||
                        predictedProfile.getName().contains(groundTruthProfile.getName())) {

                    isThereCharacter = true;
                    //Adjective/////////////////////////////////////////////////////////
                    for (String predictedAdj : predictedProfile.getAdjs().keySet()) {
                        predictedAdj = predictedAdj.toLowerCase().trim();
                        boolean isthere = false;
                        for (String groundAdj : groundTruthProfile.getAdj()) {
                            groundAdj = groundAdj.toLowerCase().trim();
                            if (groundAdj.contains("'") && !quoteConsideration) continue;
                            if (groundAdj.contains(predictedAdj) || predictedAdj.contains(groundAdj))
                                isthere = true;
                        }
                        if (isthere)
                            TP_adj.add(predictedAdj);
                        else
                            FP_adj.add(predictedAdj);
                    }
                    for (String groundAdj : groundTruthProfile.getAdj()) {
                        groundAdj = groundAdj.toLowerCase().trim();
                        if (groundAdj.contains("'") && !quoteConsideration) continue;
                        boolean isthere = false;
                        for (String predictedAdj : predictedProfile.getAdjs().keySet()) {
                            predictedAdj = predictedAdj.toLowerCase().trim();
                            if (groundAdj.contains(predictedAdj) || predictedAdj.contains(groundAdj))
                                isthere = true;
                        }
                        if (!isthere)
                            FN_adj.add(groundAdj);
                    }
                    //Verb////////////////////////////////////////////////////////
                    for (String predictedVerb : predictedProfile.getVerbs().keySet()) {
                        predictedVerb = predictedVerb.toLowerCase().trim();
                        boolean isthere = false;
                        for (String groundVerb : groundTruthProfile.getVerb()) {
                            groundVerb = groundVerb.toLowerCase().trim();
                            if (groundVerb.contains("'") && !quoteConsideration) continue;
                            if (groundVerb.contains(predictedVerb) || predictedVerb.contains(groundVerb))
                                isthere = true;
                        }
                        if (isthere)
                            TP_verb.add(predictedVerb);
                        else
                            FP_verb.add(predictedVerb);
                    }
                    for (String groundVerb : groundTruthProfile.getVerb()) {
                        groundVerb = groundVerb.toLowerCase().trim();
                        if (groundVerb.contains("'") && !quoteConsideration) continue;
                        boolean isthere = false;
                        for (String predictedVerb : predictedProfile.getVerbs().keySet()) {
                            predictedVerb = predictedVerb.toLowerCase().trim();
                            if (groundVerb.contains(predictedVerb) || predictedVerb.contains(groundVerb))
                                isthere = true;
                        }
                        if (!isthere)
                            FN_verb.add(groundVerb);
                    }
                    //Location////////////////////////////////////////////////////////
                    for (String predictedLocation : predictedProfile.getLocations()) {
                        predictedLocation = predictedLocation.toLowerCase().trim();
                        boolean isthere = false;
                        for (String groundLoc : groundTruthProfile.getLocation()) {
                            groundLoc = groundLoc.toLowerCase().trim();
                            if (groundLoc.contains("'") && !quoteConsideration) continue;
                            if (groundLoc.contains(predictedLocation) || predictedLocation.contains(groundLoc))
                                isthere = true;
                        }
                        if (isthere)
                            TP_loc.add(predictedLocation);
                        else
                            FP_loc.add(predictedLocation);
                    }
                    for (String groundLoc : groundTruthProfile.getLocation()) {
                        groundLoc = groundLoc.toLowerCase().trim();
                        if (groundLoc.contains("'") && !quoteConsideration) continue;
                        boolean isthere = false;
                        for (String predictedLoc : predictedProfile.getLocations()) {
                            predictedLoc = predictedLoc.toLowerCase().trim();
                            if (groundLoc.contains(predictedLoc) || predictedLoc.contains(groundLoc))
                                isthere = true;
                        }
                        if (!isthere)
                            FN_loc.add(groundLoc);
                    }
                    //Sentiment/////////////////////////////////////////////////////
                    int nCount = predictedProfile.getNegativeSentimentCount();
                    int pCount = predictedProfile.getPositiveSentimentCount();
                    int vPCount = predictedProfile.getVeryPositiveSentimentCount();
                    int vNCount = predictedProfile.getVeryNegativeSentimentCount();
                    int max = 0;
                    String sentimentClass = "";
                    if(nCount > pCount){
                        max = nCount;
                        sentimentClass = "negative";
                    }else{
                        max = pCount;
                        sentimentClass = "positive";
                    }

                    if(max < vPCount){
                        max = pCount;
                        sentimentClass = "very positive";
                    }
                    if(max < vNCount){
                        sentimentClass = "very negative";
                    }
                    if(groundTruthProfile.getSentiment().equals(sentimentClass)){
                        TP_sentiment.add(sentimentClass);
                    }else{
                        FN_sentiment.add(sentimentClass);
                    }
                    ///////////////////////////////////////////////////////////
                }
            }
            if (isThereCharacter)
                TP_character.add(groundTruthProfile.getName());
            else
                FN_character.add(groundTruthProfile.getName());

        }

        for (Profile predictedProfile : predictedProfiles) {
            boolean isThereCharacter = false;
            for (ProfileEvaluator groundTruthProfile : groundTruthProfiles) {
                if (groundTruthProfile.getName().contains(predictedProfile.getName()) ||
                        predictedProfile.getName().contains(groundTruthProfile.getName())) {
                    isThereCharacter = true;
                }
            }
            if(!isThereCharacter)
                FP_character.add(predictedProfile.getName());
        }

        float recall_character = Float.parseFloat(String.valueOf(TP_character.size())) / (Float.parseFloat(String.valueOf(TP_character.size())) + Float.parseFloat(String.valueOf(FN_character.size())));
        float precision_character = Float.parseFloat(String.valueOf(TP_character.size())) / (Float.parseFloat(String.valueOf(TP_character.size())) + Float.parseFloat(String.valueOf(FP_character.size())));
        float fMeasure_character = (2 * (recall_character * precision_character)) / (recall_character + precision_character);
        System.out.println("strong recall for character: " + recall_character);
        System.out.println("strong precision for character: " + precision_character);
        System.out.println("strong fMeasure for character: " + fMeasure_character);


        float recall_adj = Float.parseFloat(String.valueOf(TP_adj.size())) / (Float.parseFloat(String.valueOf(TP_adj.size())) + Float.parseFloat(String.valueOf(FN_adj.size())));
        float precision_adj = Float.parseFloat(String.valueOf(TP_adj.size())) / (Float.parseFloat(String.valueOf(TP_adj.size())) + Float.parseFloat(String.valueOf(FP_adj.size())));
        float fMeasure_adj = (2 * (recall_adj * precision_adj)) / (recall_adj + precision_adj);
        System.out.println("strong recall for adjectives: " + recall_adj);
        System.out.println("strong precision for adjectives: " + precision_adj);
        System.out.println("strong fMeasure for adjectives: " + fMeasure_adj);

        float recall_verb = Float.parseFloat(String.valueOf(TP_verb.size())) / (Float.parseFloat(String.valueOf(TP_verb.size())) + Float.parseFloat(String.valueOf(FN_verb.size())));
        float precision_verb = Float.parseFloat(String.valueOf(TP_verb.size())) / (Float.parseFloat(String.valueOf(TP_verb.size())) + Float.parseFloat(String.valueOf(FP_verb.size())));
        float fMeasure_verb = (2 * (recall_verb * precision_verb)) / (recall_verb + precision_verb);
        System.out.println("strong recall for verbs: " + recall_verb);
        System.out.println("strong precision for verbs: " + precision_verb);
        System.out.println("strong fMeasure for verbs: " + fMeasure_verb);


        float recall_loc = Float.parseFloat(String.valueOf(TP_loc.size())) / (Float.parseFloat(String.valueOf(TP_loc.size())) + Float.parseFloat(String.valueOf(FN_loc.size())));
        float precision_loc = Float.parseFloat(String.valueOf(TP_loc.size())) / (Float.parseFloat(String.valueOf(TP_loc.size())) + Float.parseFloat(String.valueOf(FP_loc.size())));
        float fMeasure_loc = (2 * (recall_loc * precision_loc)) / (recall_loc + precision_loc);
        System.out.println("strong recall for location: " + recall_loc);
        System.out.println("strong precision for location: " + precision_loc);
        System.out.println("strong fMeasure for location: " + fMeasure_loc);

        float recall_sentiment = Float.parseFloat(String.valueOf(TP_sentiment.size())) / (Float.parseFloat(String.valueOf(TP_sentiment.size())) + Float.parseFloat(String.valueOf(FN_sentiment.size())));
        float precision_sentiment = Float.parseFloat(String.valueOf(TP_sentiment.size())) / (Float.parseFloat(String.valueOf(TP_sentiment.size())) + Float.parseFloat(String.valueOf(FP_sentiment.size())));
        float fMeasure_sentiment = (2 * (recall_sentiment * precision_sentiment)) / (recall_sentiment + precision_sentiment);
        System.out.println("strong recall for sentiment: " + recall_sentiment);
        System.out.println("strong precision for sentiment: " + precision_sentiment);
        System.out.println("strong fMeasure for sentiment: " + fMeasure_sentiment);


        //Relation/////////////////////////////////////////////////////
        evaluateelation(predictedRelations, groundTruthRelations);
    }

    private List<Profile> readResult(String fileAddress) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Profile[] arrProfiles = mapper.readValue(new File(fileAddress), Profile[].class);
            List<Profile> profiles = new ArrayList<>();
            for (Profile profile : arrProfiles) {
                profiles.add(profile);
            }
            return profiles;
        } catch (Exception ex) {
            System.out.print("ERROR IN convertFromJson: " + ex.getMessage());
            return null;
        }
    }

    private Relation[] readRelationResult(String fileAddress) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Relation[] arrRelations = mapper.readValue(new File(fileAddress), Relation[].class);
            return arrRelations;
        } catch (Exception ex) {
            System.out.print("ERROR IN convertFromJson: " + ex.getMessage());
            return null;
        }
    }

    private ProfileEvaluator[] readGroundTruth(String fileAddress) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ProfileEvaluator[] arrProfiles = mapper.readValue(new File(fileAddress), ProfileEvaluator[].class);
            return arrProfiles;
        } catch (Exception ex) {
            System.out.print("ERROR IN convertFromJson: " + ex.getMessage());
            return null;
        }
    }

    private RelationEvaluate[] readGroundTruth_relations(String fileAddress) {
        try {
            if(fileAddress == "")
                return new RelationEvaluate[0];
            ObjectMapper mapper = new ObjectMapper();
            RelationEvaluate[] arrRelations = mapper.readValue(new File(fileAddress), RelationEvaluate[].class);
            return arrRelations;
        } catch (Exception ex) {
            System.out.print("ERROR IN convertFromJson: " + ex.getMessage());
            return null;
        }
    }

    private void evaluateelation(Relation[] predictedRelations, RelationEvaluate[] groundTruthRelation) {
        List<String> TP_rel = new ArrayList<>();
        List<String> FP_rel = new ArrayList<>();
        List<String> FN_rel = new ArrayList<>();


        for (Relation relation : predictedRelations) {
            String[] predictedProfileNames = relation.getProfileNames();
            for (RelationEvaluate grounftruthRel : groundTruthRelation) {
                String[] groundTruthProfileNames = grounftruthRel.getProfileNames();
                Arrays.sort(predictedProfileNames);
                Arrays.sort(groundTruthProfileNames);
                if (Arrays.equals(predictedProfileNames, groundTruthProfileNames)) {
                    for (String predictedTypeName : relation.getTypeName().keySet()) {
                        boolean isThere = false;
                        predictedTypeName = predictedTypeName.toLowerCase().trim();
                        for (String GroundTypeName : grounftruthRel.getTypeNames()) {
                            GroundTypeName = GroundTypeName.trim().toLowerCase();
                            if (GroundTypeName.equals(predictedTypeName)) isThere = true;
                        }
                        if (isThere)
                            TP_rel.add(predictedTypeName);
                        else
                            FP_rel.add(predictedTypeName);
                    }

                    for (String GroundTypeName : grounftruthRel.getTypeNames()) {
                        boolean isThere = false;
                        GroundTypeName = GroundTypeName.toLowerCase().trim();
                        for (String predictedTypeName : relation.getTypeName().keySet()) {
                            predictedTypeName = predictedTypeName.toLowerCase().trim();
                            if (GroundTypeName.equals(predictedTypeName)) isThere = true;

                        }
                        if (!isThere)
                            FN_rel.add(GroundTypeName);
                    }
                }


            }
        }

        float recall_rel = Float.parseFloat(String.valueOf(TP_rel.size())) / (Float.parseFloat(String.valueOf(TP_rel.size())) + Float.parseFloat(String.valueOf(FN_rel.size())));
        float precision_rel = Float.parseFloat(String.valueOf(TP_rel.size())) / (Float.parseFloat(String.valueOf(TP_rel.size())) + Float.parseFloat(String.valueOf(FP_rel.size())));
        float fMeasure_rel = (2 * (recall_rel * precision_rel)) / (recall_rel + precision_rel);
        System.out.println("strong recall for relation: " + recall_rel);
        System.out.println("strong precision for relation: " + precision_rel);
        System.out.println("strong fMeasure for relation: " + fMeasure_rel);
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
}
