import com.fasterxml.jackson.databind.ObjectMapper;
import entities.Profile;
import entities.ProfileEvaluator;
import entities.ResourceClass;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pegah on 9/24/17.
 */
public class Evaluator {

    private boolean quoteConsideration;
    public Evaluator(boolean quoteConsideration){
        this.quoteConsideration = quoteConsideration;
    }

    public void strongCompareResults() {
        List<Profile> predictedProfiles = readResult("out/profiles_SampleStoryResolved.json");
        ProfileEvaluator[] groundTruthProfiles = readGroundTruth("ground-truth-Arthur.json");
        List<String> TP_adj = new ArrayList<>();
        List<String> FP_adj = new ArrayList<>();
        List<String> FN_adj = new ArrayList<>();

        List<String> TP_verb = new ArrayList<>();
        List<String> FP_verb = new ArrayList<>();
        List<String> FN_verb = new ArrayList<>();


        for (ProfileEvaluator groundTruthProfile : groundTruthProfiles) {
            for (Profile predictedProfile : predictedProfiles) {
                if (groundTruthProfile.getName().contains(predictedProfile.getName()) ||
                        predictedProfile.getName().contains(groundTruthProfile.getName())) {

                    //Adjective/////////////////////////////////////////////////////////
                    for (String predictedAdj: predictedProfile.getAdjs().keySet()) {
                        predictedAdj = predictedAdj.toLowerCase().trim();
                        boolean isthere= false;
                        for(String groundAdj: groundTruthProfile.getAdj()){
                            groundAdj = groundAdj.toLowerCase().trim();
                            if(groundAdj.contains("'") && !quoteConsideration) continue;
                            if(groundAdj.contains(predictedAdj) || predictedAdj.contains(groundAdj))
                                isthere = true;
                        }
                        if(isthere)
                            TP_adj.add(predictedAdj);
                        else
                            FP_adj.add(predictedAdj);
                    }
                    for(String groundAdj: groundTruthProfile.getAdj()){
                        groundAdj = groundAdj.toLowerCase().trim();
                        if(groundAdj.contains("'") && !quoteConsideration) continue;
                        boolean isthere= false;
                        for (String predictedAdj: predictedProfile.getAdjs().keySet()) {
                            predictedAdj = predictedAdj.toLowerCase().trim();
                            if(groundAdj.contains(predictedAdj) || predictedAdj.contains(groundAdj))
                                isthere = true;
                        }
                        if(!isthere)
                            FN_adj.add(groundAdj);
                    }
                    //Verb////////////////////////////////////////////////////////
                    for (String predictedVerb: predictedProfile.getVerbs().keySet()) {
                        predictedVerb = predictedVerb.toLowerCase().trim();
                        boolean isthere= false;
                        for(String groundVerb: groundTruthProfile.getVerb()){
                            groundVerb = groundVerb.toLowerCase().trim();
                            if(groundVerb.contains("'") && !quoteConsideration) continue;
                            if(groundVerb.contains(predictedVerb) || predictedVerb.contains(groundVerb))
                                isthere = true;
                        }
                        if(isthere)
                            TP_verb.add(predictedVerb);
                        else
                            FP_verb.add(predictedVerb);
                    }
                    for(String groundVerb: groundTruthProfile.getVerb()){
                        groundVerb = groundVerb.toLowerCase().trim();
                        if(groundVerb.contains("'") && !quoteConsideration) continue;
                        boolean isthere= false;
                        for (String predictedVerb: predictedProfile.getVerbs().keySet()) {
                            predictedVerb = predictedVerb.toLowerCase().trim();
                            if(groundVerb.contains(predictedVerb) || predictedVerb.contains(groundVerb))
                                isthere = true;
                        }
                        if(!isthere)
                            FN_verb.add(groundVerb);
                    }

                }
            }
        }
        float recall_adj = Float.parseFloat(String.valueOf(TP_adj.size())) / (Float.parseFloat(String.valueOf(TP_adj.size())) + Float.parseFloat(String.valueOf(FN_adj.size())));
        float precision_adj = Float.parseFloat(String.valueOf(TP_adj.size())) / (Float.parseFloat(String.valueOf(TP_adj.size()))+ Float.parseFloat(String.valueOf(FP_adj.size())));
        float fMeasure_adj = (2 * (recall_adj * precision_adj)) / (recall_adj + precision_adj);
        System.out.println("strong recall for adjectives: " + recall_adj);
        System.out.println("strong precision for adjectives: " + precision_adj);
        System.out.println("strong fMeasure for adjectives: " + fMeasure_adj);

        float recall_verb = Float.parseFloat(String.valueOf(TP_verb.size())) / (Float.parseFloat(String.valueOf(TP_verb.size())) + Float.parseFloat(String.valueOf(FN_verb.size())));
        float precision_verb = Float.parseFloat(String.valueOf(TP_verb.size())) / (Float.parseFloat(String.valueOf(TP_verb.size()))+ Float.parseFloat(String.valueOf(FP_verb.size())));
        float fMeasure_verb = (2 * (recall_verb * precision_verb)) / (recall_verb + precision_verb);
        System.out.println("strong recall for verbs: " + recall_verb);
        System.out.println("strong precision for verbs: " + precision_verb);
        System.out.println("strong fMeasure for verbs: " + fMeasure_verb);
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

}
