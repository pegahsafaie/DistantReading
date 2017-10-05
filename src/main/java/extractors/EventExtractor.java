package extractors;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import entities.Context;
import entities.Event;
import entities.Profile;
import org.lambda3.graphene.core.Graphene;
import org.lambda3.graphene.core.relation_extraction.model.ExContent;
import org.lambda3.graphene.core.relation_extraction.model.ExElement;
import org.lambda3.graphene.core.relation_extraction.model.ExSPO;
import org.lambda3.graphene.core.relation_extraction.model.ExVContext;

import java.util.ArrayList;
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

    public List<Event> extract(String content, List<Profile> profiles) {
        this.profiles = profiles;
        events = new ArrayList<Event>();
        grapheneExtractor(content);

        classifier = null;
        pipeline = null;
        System.gc();

//        setLemmasList();
//        System.gc();

        //extractVerbNetInformation();
        //System.gc();

//        sentimentAnalysis();
        return events;
    }

    private void grapheneExtractor(String content) {

        Graphene graphene = new Graphene();
        ExContent ec = graphene.doRelationExtraction(content, false);

        Properties props = new Properties();
        pipeline = new AnnotationPipeline();
        pipeline.addAnnotator(new TokenizerAnnotator(false));
        pipeline.addAnnotator(new TimeAnnotator("sutime", props));

        for (ExElement element : ec.getElements()) {
            try {
                ExSPO eXSpo = element.getSpo().get();
                if (isProfile(eXSpo.getSubject()) && isNotBe(eXSpo.getPredicate())) {
                    Event event = new Event();
                    event.setVerb(eXSpo.getPredicate());
                    event.setLemmatizedVerb(eXSpo.getPredicate());
                    event.setObject(eXSpo.getObject());
                    event.setPredicate(eXSpo.getPredicate());
                    event.setSubject(eXSpo.getSubject());
                    event.setSentence(element.getText());
                    Context[] contexts = new Context[element.getVContexts().size()];
                    int i = 0;
                    for (ExVContext verbContext : element.getVContexts()) {//each event can have multi contexts
                        Context context = new Context();
                        context.setClassification(verbContext.getClassification().name());
                        context.setText(verbContext.getText());
                        context.setEventDateTime(dateNormilize(verbContext.getText()));
                        contexts[i++] = context;
                    }
//                    //each event can have multiple verb, but we hope that just one verb, because we dont support array
//                    List<Map<String, Map<String, String>>> semanticLabeldInput = semanticRoleLabel(element.getText());
//                    if(semanticLabeldInput != null)
//                    for (Map<String, Map<String, String>> extent : semanticLabeldInput) {
//                        //use SemLink to find verbNet corresponding class
//                        for (Map.Entry<String, Map<String, String>> role : extent.entrySet()) {//for each sentence
//                            String verb_pos_simpleVerb = role.getKey();
//                            String[] info = verb_pos_simpleVerb.split("_");
//                            String tense = info[1];
//                            String simple_verb = info[2];
//                            Map<String, String> arguments = role.getValue();
//                            Map<String, Map<String, String>> semLinkMap = findSemLinkMap(simple_verb);
//                            event.setVerb(simple_verb);
//                            event.setVerbTense(tense);
//                            event.setProbArguments(arguments);
//                            for (Map.Entry<String,Map<String,String>> map:semLinkMap.entrySet()) {
//                                event.setVerbNetId(map.getKey());
//                                event.setVerbNetArguments(map.getValue());
//                            }
//                        }
//                    } 0

                    event.setvContexts(contexts);
                    events.add(event);
                }
            } catch (Exception ex) {
                System.out.println("ERROR:" + ex.getMessage());
            }
        }
    }

    private boolean isProfile(String name) {
        for (Profile profile : profiles) {
            if (profile.getName().toLowerCase().trim().contains(name.toLowerCase().trim())) {
                return true;
            }
        }
        return false;
    }

    private void setLemmasList() {

        for (Event event : events) {
            try {
                String verb = event.getPredicate();
                String lemma = new Sentence(verb).lemma(0);
                if (!lemma.equals("be"))
                    event.setLemmatizedVerb(lemma);
                else
                    events.remove(event);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private String[] dateNormilize(String text) {
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);
        List<CoreMap> timexAnnsAll = annotation.get(TimeAnnotations.TimexAnnotations.class);

        String[] dateTimes = new String[timexAnnsAll.size()];
        int i = 0;
        for (CoreMap cm : timexAnnsAll) {
            dateTimes[i++] = cm.get(TimeExpression.Annotation.class).getTemporal().toString();
        }
        return dateTimes;
    }

    private boolean isEvent_NER(ExElement element) {
        if (classifier == null)
            classifier = CRFClassifier.getDefaultClassifier();
        boolean isEvent = false;
        List<List<CoreLabel>> classify = classifier.classify(element.getNotSimplifiedText());
        for (List<CoreLabel> coreLabels : classify) {
            for (CoreLabel coreLabel : coreLabels) {
                String word = coreLabel.word();
                String category = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);
                if (category.equals("LOCATION") || category.equals("DATE") || category.equals("TIME"))
                    return true;
            }
        }
        return isEvent;
    }

    private void sentimentAnalysis() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        for (Event event : events) {
            Annotation annotation = pipeline.process(event.getSentence());
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                event.addToSentiment(sentiment);
            }
        }
    }

    private boolean isNotBe(String verb){
        if(verb.equals("was")||verb.equals("is")||verb.equals("were")||verb.equals("are"))
            return false;
        else return  true;
    }
}
