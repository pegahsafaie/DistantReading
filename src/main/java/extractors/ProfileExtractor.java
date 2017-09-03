package extractors;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import entities.Profile;
import entities.Relation;

import java.util.*;

public class ProfileExtractor {

    public List<Profile> extract(String content, boolean useQuote) {
        try {

            if(!useQuote) {
                String quoteRegex = "(\"(.*?)\"|\'(.*?)\'|(``(.*)'')|(''(.*)'')|(``(.*)``)|(`(.*)`)|(`(.*)')|('(.*)`))";
                content = content.replaceAll(quoteRegex, "");
            }
            List<Profile> extractedProfiles = profileExtraction(content);
            Set<Profile> invalidProfiles = new HashSet<>();
            for (Profile profile:extractedProfiles) {
                if(profile.getFrequency() <= 1){
                    invalidProfiles.add(profile);
                }
                if(profile.getVerbs().size() == 0 && profile.getAdjs().size() == 0){
                    invalidProfiles.add(profile);
                }
            }
            extractedProfiles.removeAll(invalidProfiles);
            return extractedProfiles;

        } catch (Exception ex) {
            System.out.print(ex.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Profile> profileExtraction(String content) {
        List<Profile> profiles = new ArrayList<>();
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos,lemma, parse,sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        CRFClassifier<CoreLabel> classifier = CRFClassifier.getDefaultClassifier();

        List<List<CoreLabel>> classify = classifier.classify(content);
        for (List<CoreLabel> coreLabels : classify) {
            for (int i = 0; i < coreLabels.size(); i++) {
                CoreLabel coreLabel = coreLabels.get(i);
                String namedEntity = "";
                String category = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);
                if (category.equals("PERSON") || category.equals("ORGANIZATION")) {

                    while (category.equals("PERSON") || category.equals("ORGANIZATION")) {
                        namedEntity += " " + coreLabel.word();
                        coreLabel = coreLabels.get(++i);
                        category = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);
                    }

                    Profile profile = new Profile();
                    profile.setName(namedEntity);

                    boolean existed = false;
                    // it it sth else rather than profile name. we profile name, keeps its first
                    //occurance name, because it is almost always the most complete one
                    //but we prefer to use the short name to capzure all the occurances in tag ans
                    //dependency tree and ..
                    String currentName = namedEntity.trim();
                    for (Profile definedProfile : profiles) {
                        // the reason of using two directional contains instead of equality:
                        //for example Arthur comes first to story. then Arthur becomes a king and we have
                        //King arthur. Or vice versa. for example one person is president Obama and after
                        //some lines his period is finished and he is called Obama
                        if (definedProfile.getName().trim().contains(namedEntity.trim())
                                || namedEntity.trim().contains(definedProfile.getName().trim())) {
                            profile = definedProfile;
                            profile.addFrequency();
                            existed = true;
                        }
                    }

                    if (!existed)
                        profiles.add(profile);


                    //sentiment detection of the sentence containing this person
                    StringBuilder sb = new StringBuilder();
                    for (CoreLabel label : coreLabels) {
                        sb.append(label.word());
                        sb.append(" ");
                    }
                    Annotation annotation = pipeline.process(sb.toString());
                    for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                        addLocation(profile, coreLabels);
                        addTime(profile, coreLabels);
                        addSentiment(sentence, profile);
                        SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
                        addAjd(dependencies, profile, currentName);
                        addVerb(dependencies, profile, currentName);
                    }

                }
            }
        }
        pipeline = null;
        System.gc();
        return profiles;
    }

    private void addLocation(Profile profile, List<CoreLabel> coreLabels) {
        for (CoreLabel coreLabel : coreLabels) {
            String category = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);
            if (category.equals("LOCATION")) {
                profile.addLocation(coreLabel.word());
            }
        }
    }

    private void addTime(Profile profile, List<CoreLabel> coreLabels) {
        for (CoreLabel coreLabel : coreLabels) {
            String category = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);
            if (category.equals("DATE") || category.equals("TIME")) {
                profile.addTemporal(coreLabel.word());
            }
        }
    }

    private void addSentiment(CoreMap sentence, Profile profile) {
        //Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
        //int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
        String sentimentClassName = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
        profile.addToSentiment(sentimentClassName);
    }

    private void addAjd(SemanticGraph dependencies, Profile profile, String currentProfileName) {

        //old Merlin
        //old and clever Merlin, old clever Merlin
        IndexedWord profileName = dependencies.getNodeByWordPattern(currentProfileName);
        List<SemanticGraphEdge> outgoingEdges = dependencies.getOutEdgesSorted(profileName);
        List<SemanticGraphEdge> incomingEdges = dependencies.getIncomingEdgesSorted(profileName);
        for (SemanticGraphEdge edge : outgoingEdges) {
            if (edge.getTarget().tag().startsWith("JJ")) {
                profile.addToAdjs(edge.getTarget().word());
                List<SemanticGraphEdge> outgoingEdgesFromJJ = dependencies.getOutEdgesSorted(edge.getTarget());
                for (SemanticGraphEdge edgeFromJJ : outgoingEdgesFromJJ) {
                    if ((edgeFromJJ.getRelation().getShortName().equals("conj") ||
                            edgeFromJJ.getRelation().getShortName().equals("amod") ||
                            edgeFromJJ.getRelation().getShortName().equals("punct")) &&
                            edgeFromJJ.getTarget().tag().startsWith("JJ")) {
                        profile.addToAdjs(edgeFromJJ.getTarget().word());
                    }
                }
            }
        }

        //Merlin was an old and clever Merlin, old clever man
        //Merlin was a good man
        for (SemanticGraphEdge edge : incomingEdges) {
            if (edge.getRelation().toString().equals("nsubj")) {
                if ((edge.getSource().tag().equals("NN") || edge.getSource().tag().equals("NNP"))){
//                        && (!edge.getSource().ner().equals("LOCATION") && !edge.getSource().ner().equals("DATE") && !edge.getSource().ner().equals("TIME"))) {
                    IndexedWord NN = edge.getSource();
                    List<SemanticGraphEdge> edgesOutFromNN = dependencies.getOutEdgesSorted(NN);
                    for (SemanticGraphEdge outedge : edgesOutFromNN) {
                        if (outedge.getTarget().tag().startsWith("JJ")) {
                            profile.addToAdjs(outedge.getTarget().word() + " " + edge.getSource().word());
                            List<SemanticGraphEdge> outgoingEdgesFromJJ = dependencies.getOutEdgesSorted(edge.getTarget());
                            for (SemanticGraphEdge edgeFromJJ : outgoingEdgesFromJJ) {
                                if ((edgeFromJJ.getRelation().getShortName().equals("conj") ||
                                        edgeFromJJ.getRelation().getShortName().equals("amod")||
                                        edgeFromJJ.getRelation().getShortName().equals("punct")) &&
                                        edgeFromJJ.getTarget().tag().startsWith("JJ")) {
                                    profile.addToAdjs(edgeFromJJ.getTarget().word());
                                }
                            }
                        }
                    }
                }
                //Arthur was strong// Arthur was strong and young //Arthur was strong, young
                if (edge.getSource().tag().contains("JJ") || edge.getSource().tag().contains("RB")) {
                    profile.addToAdjs(edge.getSource().word());
                    List<SemanticGraphEdge> outgoingEdgesFromJJ = dependencies.getOutEdgesSorted(edge.getSource());
                    for (SemanticGraphEdge edgeFromJJ : outgoingEdgesFromJJ) {
                        if ((edgeFromJJ.getRelation().getShortName().equals("conj")
                                ||
                                edgeFromJJ.getRelation().toString().equals("amod")
                                || edgeFromJJ.getRelation().toString().equals("punct")) &&
                                edgeFromJJ.getTarget().tag().startsWith("JJ")) {//Merlin was an old and clever Merlin, old clever man
                            profile.addToAdjs(edgeFromJJ.getTarget().word());
                        }
                    }
                }
            }
        }
    }

    private void addVerb(SemanticGraph dependencies, Profile profile, String currentProfileName) {
        IndexedWord profileName = dependencies.getNodeByWordPattern(currentProfileName);
        List<SemanticGraphEdge> incomingEdges = dependencies.getIncomingEdgesSorted(profileName);
        for (SemanticGraphEdge edge : incomingEdges) {
            if (edge.getSource().tag().contains("VB") && edge.getRelation().toString().equals("nsubj")) {
                profile.addToVerbs(edge.getSource().lemma());
            }
        }
    }

}