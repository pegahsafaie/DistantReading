package extractors;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
import entities.Context;
import entities.Event;
import entities.Profile;
import org.lambda3.graphene.core.Graphene;
import org.lambda3.graphene.core.relation_extraction.model.ExContent;
import org.lambda3.graphene.core.relation_extraction.model.ExElement;
import org.lambda3.graphene.core.relation_extraction.model.ExSPO;
import org.lambda3.graphene.core.relation_extraction.model.ExVContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Created by pegah on 7/3/17.
 */
public class EventExtractor {

    List<Event> events;
    CRFClassifier<CoreLabel> classifier;
    AnnotationPipeline pipeline;
    List<Profile> profiles;

    public List<Event> extract(String content, List<Profile> profiles, boolean removeEventsWithoutLocationAndObejct, boolean removeEventsWithNoCharacterObject) {
        try {
            List<Event> events = new ArrayList<>();
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize, ssplit, pos,lemma, parse, ner");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
            Annotation annotation = pipeline.process(content);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
                for (CoreLabel coreLabel : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    if (coreLabel.tag().startsWith("VB")) {
                        String verb = coreLabel.lemma();
                        String obj = "";
                        String subj = "";
                        String locations = "";
                        String times = "";
                        String confirmedSubj = "";
                        String confirmedObj = "";
                        boolean negative = false;
                        IndexedWord verbNode = dependencies.getNodeByWordPattern(coreLabel.word());
                        List<SemanticGraphEdge> outComingEdgesFromVerb = dependencies.getOutEdgesSorted(verbNode);
                        for (SemanticGraphEdge edge : outComingEdgesFromVerb) {
                            if (edge.getRelation().getShortName().equals("neg")) {
                                negative = true;
                            }
                            if (edge.getRelation().getShortName().equals("compound:prt")) {
                                verb = verb + " " + edge.getTarget().word();
                            }
                            if (edge.getRelation().getShortName().contains("obj")) {
                                obj = edge.getTarget().word().toLowerCase().trim();
                                obj += " " + findNameDependencies(dependencies, sentence, obj).toLowerCase().trim();
                            }
                            if (edge.getRelation().getShortName().contains("subj") && (edge.getTarget().ner().equals("PERSON") || edge.getTarget().ner().equals("ORGANIZATION"))) {
                                subj = edge.getTarget().word().toLowerCase().trim();
                                subj += " " + findNameDependencies(dependencies, sentence, subj).trim().toLowerCase();
                            }
                        }
                        for (Profile profile : profiles) {
                            if (subj != "" && (profile.getName().toLowerCase().trim().contains(subj)
                                    || subj.contains(profile.getName().toLowerCase().trim()))) {
                                confirmedSubj = profile.getName();
                            }
                            if (obj != "" && (profile.getName().toLowerCase().trim().contains(obj)
                                    || obj.contains(profile.getName().toLowerCase().trim()))) {
                                confirmedObj = profile.getName();
                            }
                        }
                        for (CoreLabel coreLabelNested : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                            if (coreLabelNested.ner().equals("LOCATION") && !locations.contains(coreLabelNested.word())) {
                                if (sentence.get(CoreAnnotations.TokensAnnotation.class).get(coreLabelNested.index() - 1).ner().equals("LOCATION")) {
                                    locations += " " + coreLabelNested.word();
                                } else {
                                    locations += "," + coreLabelNested.word();
                                }

                            }
                            if ((coreLabelNested.ner().equals("TIME") || coreLabelNested.ner().equals("DATE")) && !times.contains(coreLabelNested.word())) {
                                    times = ", " + coreLabelNested.word();
                                    times += " " + findNameDependencies(dependencies, sentence, coreLabelNested.word());
                            }
                        }
                        if (verb.equals("become") || verb.equals("do") || verb.equals("have") || verb.equals("be") || confirmedSubj.isEmpty())
                            continue;
                        if (removeEventsWithoutLocationAndObejct && obj.isEmpty() && locations.isEmpty())
                            continue;
                        if (!removeEventsWithoutLocationAndObejct && (obj.isEmpty() && locations.isEmpty() && times.isEmpty()))
                            continue;
                        if (removeEventsWithNoCharacterObject && obj.isEmpty())
                            continue;
                        if (confirmedObj == confirmedSubj)
                            continue;

                        Event event = new Event();
                        event.setLemmatizedVerb(negative ? "not " + verb : verb);
                        event.setObject(obj);
                        event.setSubject(!confirmedSubj.isEmpty() ? confirmedSubj : subj);
                        events.add(event);
                        event.setTimes(times.split(","));
                        event.setLocations(locations.split(","));
                    }
                }
            }
            return events;
        }catch(Exception ex){
            System.out.println("Error in event extractor: " + ex.getMessage());
            return null;
        }
    }

    private void extractByCoreNLPRelationAnnotator(String content){
        Properties props = PropertiesUtils.asProperties(
                "annotators", "tokenize,ssplit,pos,lemma,depparse,natlog,openie"
        );
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        String[] chapters = content.split("\n\n\n\n");
        for (String chapter : chapters) {


            Annotation doc = new Annotation(chapter);
            pipeline.annotate(doc);
            for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {

                // Print SemanticGraph
//            System.out.println(sentence.get(SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation.class).toString(SemanticGraph.OutputFormat.LIST));

                // Get the OpenIE triples for the sentence
                Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);

                // Print the triples
                for (RelationTriple triple : triples) {
                    System.out.println(triple.confidence + "\t" +
                            triple.subjectLemmaGloss() + "\t" +
                            triple.relationLemmaGloss() + "\t" +
                            triple.objectLemmaGloss());
                }
            }
        }
    }

    private void extractByGraphene(){

    }


    private String findNameDependencies(SemanticGraph dependencies, CoreMap sentence, String object) {
        try {
            List<SemanticGraphEdge> edges = new ArrayList<>();

            IndexedWord profileName = dependencies.getNodeByWordPattern(object);
            edges.addAll(dependencies.getOutEdgesSorted(profileName));
            edges.addAll(dependencies.getIncomingEdgesSorted(profileName));


            List<CoreLabel> coreLabels = sentence.get(CoreAnnotations.TokensAnnotation.class);
            int indexOfObjectWord = 0;
            for (int i = 0; i < coreLabels.size(); i++) {
                if (coreLabels.get(i).word().equals(object)) {
                    indexOfObjectWord = i;
                }
            }
            //find the nouns exatl before Person as his role
            String objectDependencies = "";
            if (indexOfObjectWord < coreLabels.size() - 1) {
                objectDependencies = "";
                int counter = indexOfObjectWord + 1;
                CoreLabel coreLabel = coreLabels.get(counter);
                String pos_category = coreLabel.tag();

                while (pos_category.startsWith("NN") || pos_category.equals("DT")
                        || pos_category.equals("IN")) {

                    if(pos_category.startsWith("NN"))
                    objectDependencies += " " + coreLabel.word();

                    if (counter < coreLabels.size() - 1) {
                        //add DT and IN just in the case that we have another word after them
                        if(pos_category.equals("DT") || pos_category.equals("IN")) {
                            String w = new String(coreLabel.word());
                            coreLabel = coreLabels.get(++counter);
                            pos_category = coreLabel.tag();
                            if(pos_category.startsWith("NN") || pos_category.equals("DT")
                                    || pos_category.equals("IN")){
                                objectDependencies += " " + w;
                            }
                        }else{
                            coreLabel = coreLabels.get(++counter);
                            pos_category = coreLabel.tag();
                        }
                    } else {
                        pos_category = "o";
                    }
                }
                if (objectDependencies != "")
                    return objectDependencies;
            }
        } catch (Exception ex) {
            System.out.println("Error in finding objectDependencies:");
            System.out.println(ex.getMessage());
            System.out.println("---------------------");
        }
        return "";
    }
}
