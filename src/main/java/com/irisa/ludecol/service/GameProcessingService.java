package com.irisa.ludecol.service;

import com.irisa.ludecol.domain.*;
import com.irisa.ludecol.domain.subdomain.*;
import com.irisa.ludecol.repository.*;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.GraphPathImpl;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by dorian on 26/05/15.
 */
@Service
public class GameProcessingService {

    private static final int MIN_PATH_LENGTH = 2;
    private static final int MAX_DISTANCE = 64;
    private final Logger log = LoggerFactory.getLogger(GameProcessingService.class);

    @Inject
    private ImageRepository imageRepository;

    @Inject
    private ImageSetRepository imageSetRepository;

    @Inject
    private GameRepository gameRepository;

    @Inject
    private GameNotificationRepository gameNotificationRepository;

    @Inject
    private GameNotificationService gameNotificationService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private ObjectiveRepository objectiveRepository;

    @Inject
    private ObjectiveService objectiveService;


    private void processPresenceGrid(final List<Boolean> inputData, final List<Double> processedData, int n) {
        if(processedData.isEmpty()) {
            inputData.forEach(b -> processedData.add(b ? 1. : 0.));
        }
        else {
            for (int i = 0; i < inputData.size(); i++) {
                processedData.set(i,processedData.get(i) * n + (inputData.get(i) ? 1 : 0) / ((double) (n+1)));
            }
        }
    }

    private Map<PlantSpecies,List<Boolean>> processPlantIdentification(final List<PlantIdentificationResult> gameResults) {
        final Map<PlantSpecies,List<Boolean>> result = new HashMap();
        final List<Iterator<Boolean>> iterators = new ArrayList<>();

        for(PlantSpecies species : PlantSpecies.values()) {
            List<Boolean> tmp = new ArrayList<>();
            for(PlantIdentificationResult gameResult : gameResults) {
                iterators.add(gameResult.getSpeciesMap().get(species).iterator());
            }
            while(iterators.stream().map(it->it.hasNext()).reduce(true,(a,b)->a&&b)) {
                int nbOccurences = 0;
                for(Iterator<Boolean> iterator : iterators) {
                    nbOccurences = nbOccurences + (iterator.next() ? 1 : 0);
                }
                tmp.add(Boolean.valueOf(nbOccurences >= MIN_PATH_LENGTH));
            }
            result.put(species,tmp);
            iterators.clear();
        }
        return result;
    }

    private Map<Species,Boolean> processAllStars(final List<AllStarsResult> gameResults) {
        final Map<Species,Boolean> result = new HashMap();

        for(Species species : Species.values()) {
            int presence = 0;
            for(AllStarsResult gameResult : gameResults) {
                if(gameResult.getSpeciesMap().get(species)) {
                    presence++;
                }
            }
            if(presence >= MIN_PATH_LENGTH) {
                result.put(species,Boolean.TRUE);
            } else {
                result.put(species,Boolean.FALSE);
            }
        }

        return result;
    }

    private Map<AnimalSpecies,List<double[]>> processAnimalIdentification(final List<AnimalIdentificationResult> gameResults) {
        final int nb_submissions = gameResults.size();
        final Map<AnimalSpecies,List<double[]>> result = new HashMap();
        for(AnimalSpecies species : AnimalSpecies.values()) {
            final Map<double[],List<double[]>> pointMap = new HashMap<>();
            for(int i=0;i<nb_submissions-1;i++) {
                final List<double[]> pointList1 = gameResults.get(i).getSpeciesMap().get(species);
                for(double[] p : pointList1) {
                    final List<double[]> tmp = new ArrayList<>();
                    for(int j=i+1;j<nb_submissions;j++) {
                        final List<double[]> pointList2 = gameResults.get(j).getSpeciesMap().get(species);
                        for(double[] q : pointList2) {
                            final double x = p[0]-q[0];
                            final double y = p[1]-q[1];
                            if(x*x+y*y <= 64*64) {
                                tmp.add(q);
                            }
                        }
                    }
                    pointMap.put(p,tmp);
                }
            }
            result.put(species, createGraph(pointMap));
        }
        return result;
    }

    private List<double[]> matchPointSets(final List<double[]> a, final List<double[]> b) {
        List<double[]> matchedPoints = new ArrayList<>();
        if(a != null && b != null && !a.isEmpty() && !b.isEmpty()) {
            List<double[]> referencePoints = new ArrayList<>(b);
            for (double[] p : a) {
                double d = MAX_DISTANCE*MAX_DISTANCE;
                double[] r = null;
                for (double[] q : referencePoints) {
                    Double dist = Math.pow(p[0] - q[0], 2) + Math.pow(p[1] - q[1], 2);
                    if (dist < d) {
                        d = dist;
                        r = q;
                    }
                }
                if (r != null) {
                    matchedPoints.add(new double[]{p[0],p[1]});
                    referencePoints.remove(r);
                }
            }
        }
        return matchedPoints;
    }

    private void awardPoints(Game game, int score) {
        User player = userRepository.findOneByLogin(game.getUsr()).get();
        if(player != null) {
            int scoreGain = score - 50;
            if(scoreGain > 0 && player.getBonusPoints() > 0) {
                int bonusScore = Math.min(scoreGain,player.getBonusPoints());
                scoreGain += bonusScore;
                player.setBonusPoints(player.getBonusPoints() - bonusScore);
            }
            int newScore = player.getScore() + scoreGain;
            int newRank = player.getRank();
            if(newScore >= 100) {
                if(newRank == 1) {newScore = 100;}
                else {newScore-=100;newRank--;}
            }
            else if (newScore <= 0) {
                if(newRank == 50) {newScore = 0;}
                else {newScore+=100;newRank++;}
            }
            player.setScore(newScore);
            player.setRank(newRank);

            String gameId = game.getId();
            GameNotification gameNotification = new GameNotification();
            gameNotification.setTitle("Game reviewed!");
            gameNotification.setContent("You scored a " + score + "!");
            gameNotification.setUsr(player.getLogin());
            gameNotification.setGameId(gameId);
            gameNotificationRepository.save(gameNotification);

            objectiveRepository.findAllByUsr(player.getLogin()).stream().forEach(objective -> {
                List<String> pendingGames = objective.getPendingGames();
                if (pendingGames.contains(gameId)) {
                    pendingGames.remove(gameId);
                    objective.setBonusPoints(objective.getBonusPoints() + Math.max(score - 50, 0));
                    //Remove objectives when they do not contain any pending game and they have the required number of completed games.
                    if (objective.getNbGamesToComplete() == objective.getNbCompletedGames() && pendingGames.isEmpty()) {
                        player.setBonusPoints(player.getBonusPoints() + objective.getBonusPoints());
                        //Objective is completed, remove it from the database.
                        objectiveRepository.delete(objective);
                        //Add a notification informing the player he completed an objective.
                        GameNotification objectiveNotification = new GameNotification();
                        objectiveNotification.setTitle("Objective completed!");
                        objectiveNotification.setContent("You gained " + objective.getBonusPoints() + " bonus points!");
                        objectiveNotification.setUsr(player.getLogin());
                        gameNotificationRepository.save(objectiveNotification);
                    } else {
                        objectiveRepository.save(objective);
                    }
                    objectiveService.handleObjectiveUpdate(player.getLogin());
                }
            });
            userRepository.save(player);
            game.setLastModified(new DateTime());
            game.setScore(score);
            gameRepository.save(game);
            gameNotificationService.handleNewNotification(player.getLogin());
        }
    }

    public void rateGames(Image image, GameMode mode) {
        List<Game> games = gameRepository.findAllByImgAndGameModeAndCompleted(image.getName(),mode,true)
            .stream().filter(g->g.getScore()==-1).collect(Collectors.toList());
        ImageModeStatus imageModeStatus = image.getModeStatus().get(mode);
        switch(mode) {
            case AllStars: {
                Map<Species,Boolean> referenceMap = (Map<Species,Boolean>) imageModeStatus.getReferenceResult();
                for(Game game : games) {
                    Map<Species, Boolean> submittedMap = ((AllStarsResult) game.getGameResult()).getSpeciesMap();

                    int mistakes = 0;
                    int correct = 0;
                    int total = 0;

                    for (Species key : referenceMap.keySet()) {
                        boolean r = referenceMap.get(key);
                        boolean s = submittedMap.get(key);
                        mistakes += r != s ? 1 : 0;
                        correct += r == s ? 1 : 0;
                        total++;
                    }

                    int score = (int) Math.floor(100 * Math.max(correct - mistakes, 0) / (total * 1.));
                    awardPoints(game,score);
                }
            }
            break;
            case AnimalIdentification: {
                Map<AnimalSpecies,List<double[]>> referenceMap = (Map<AnimalSpecies,List<double[]>>) imageModeStatus.getReferenceResult();
                for(Game game : games) {
                    Map<AnimalSpecies, List<double[]>> submittedMap = ((AnimalIdentificationResult) game.getGameResult()).getSpeciesMap();

                    int mistakes = 0;
                    int correct = 0;
                    int total = 0;

                    for (AnimalSpecies key : submittedMap.keySet()) {
                        List<double[]> referenceList = referenceMap.get(key);
                        List<double[]> submittedList = submittedMap.get(key);
                        List<double[]> correctedList = matchPointSets(submittedList, referenceList);
                        mistakes += submittedList.size() - correctedList.size();
                        correct += correctedList.size();
                        total += referenceList.size();
                    }

                    int score = (int) Math.floor(100 * Math.max(correct - mistakes, 0) / (total * 1.));
                    awardPoints(game,score);
                }
            }
            break;
            case PlantIdentification: {
                Map<PlantSpecies,List<Boolean>> referenceMap = (Map<PlantSpecies,List<Boolean>>) imageModeStatus.getReferenceResult();
                for(Game game : games) {
                    Map<PlantSpecies, List<Boolean>> submittedMap = ((PlantIdentificationResult) game.getGameResult()).getSpeciesMap();

                    int mistakes = 0;
                    int correct = 0;
                    int total = 0;

                    for (PlantSpecies key : submittedMap.keySet()) {
                        List<Boolean> referenceList = referenceMap.get(key);
                        List<Boolean> submittedList = submittedMap.get(key);
                        for (int i = 0; i < referenceList.size(); i++) {
                            mistakes += (referenceList.get(i) != submittedList.get(i)) ? 1 : 0;
                            correct += (referenceList.get(i) && submittedList.get(i)) ? 1 : 0;
                            total += referenceList.get(i) ? 1 : 0;
                        }
                    }

                    int score = (int) Math.floor(100 * Math.max(correct - mistakes, 0) / (total * 1.));
                    awardPoints(game, score);
                }
            }
            break;
        }
    }

    /* The input map must satisfay the following condition:
     * The list linked to a point from the layer 'n' can only contain points from the layers 'n+1' and up.
     */
    public List<double[]> createGraph(Map<double[],List<double[]>> points) {
        DirectedWeightedMultigraph<double[],DefaultWeightedEdge> graph = new DirectedWeightedMultigraph(DefaultWeightedEdge.class);

        //use builder maybe?

        points.entrySet().stream().forEach(e -> {
            double[] p = e.getKey();
            graph.addVertex(p);
            e.getValue().stream().forEach(q -> {
                double x = p[0] - q[0];
                double y = p[1] - q[1];
                graph.addVertex(q);
                graph.setEdgeWeight(graph.addEdge(p, q), Math.sqrt(x * x + y * y));
            });
        });

        Set<double[]> set = new HashSet<>();
        set.addAll(graph.vertexSet());
        List<double[]> leafVertice = new ArrayList<>();
        set.stream().forEach(p->{
            if(graph.outDegreeOf(p) == 0) {
                if(graph.inDegreeOf(p) == 0) {
                    graph.removeVertex(p);
                } else {
                    leafVertice.add(p);
                }
            }
        });

        final List<List<DefaultWeightedEdge>> pathList = new ArrayList<>();
        leafVertice.stream().forEach(p->pathList.addAll(getPaths(p,graph)));
        final List<GraphPath<double[],DefaultWeightedEdge>> paths = new ArrayList<>();
        pathList.stream().forEach(l -> {
            if (l.size() >= MIN_PATH_LENGTH-1) {
                final GraphPath<double[], DefaultWeightedEdge> path =
                    new GraphPathImpl(graph, graph.getEdgeSource(l.get(0)),
                        graph.getEdgeTarget(l.get(l.size() - 1)), l,
                        l.stream().collect(Collectors.summingDouble(e -> graph.getEdgeWeight(e))));
                paths.add(path);
            }
        });

        final List<GraphPath<double[],DefaultWeightedEdge>> sortedPaths = paths.stream().sorted((p1,p2)->{
            if(p1.getEdgeList().size() > p2.getEdgeList().size()) {
                return 1;
            } else if(p1.getEdgeList().size() < p2.getEdgeList().size()) {
                return -1;
            } else {
                return (int)Math.signum(p1.getWeight() - p2.getWeight());
            }
        }).collect(Collectors.toList());

        List<double[]> finalResult = new ArrayList<>();

        while(!sortedPaths.isEmpty()) {
            final GraphPath<double[],DefaultWeightedEdge> path = sortedPaths.get(0);
            cleanupPaths(path, sortedPaths, graph);
            finalResult.add(handlePath(path));
        }

        return finalResult;
    }

    private double[] handlePath(GraphPath<double[],DefaultWeightedEdge> path) {
        Graph<double[],DefaultWeightedEdge> graph = path.getGraph();
        List<double[]> points = new ArrayList<>();
        path.getEdgeList().stream().forEach(e->points.add(graph.getEdgeTarget(e)));
        points.add(path.getStartVertex());
        double x = points.stream().collect(Collectors.averagingDouble(p->p[0]));
        double y = points.stream().collect(Collectors.averagingDouble(p->p[1]));
        return new double[]{x,y};
    }

    private List<GraphPath<double[],DefaultWeightedEdge>> cleanupPaths
        (final GraphPath<double[],DefaultWeightedEdge> toRemove,
         final List<GraphPath<double[],DefaultWeightedEdge>> paths,
         final DirectedWeightedMultigraph<double[],DefaultWeightedEdge> graph) {

        final Set<DefaultWeightedEdge> edges = new HashSet<>();
        toRemove.getEdgeList().stream().forEach(e -> edges.addAll(graph.edgesOf(graph.getEdgeTarget(e))));
        edges.addAll(graph.edgesOf(toRemove.getStartVertex()));

        paths.remove(toRemove);

        List<GraphPath<double[],DefaultWeightedEdge>> tmp = new ArrayList<>();
        tmp.addAll(paths);
        tmp.stream().forEach(p->{
            if(p!=null && !Collections.disjoint(edges,p.getEdgeList())) {
                paths.remove(p);
            }
        });

        return paths;
    }

    private List<List<DefaultWeightedEdge>> getPaths(final double[] p, final DirectedWeightedMultigraph<double[],DefaultWeightedEdge> graph) {
        final Set<DefaultWeightedEdge> edges = graph.incomingEdgesOf(p);
        if(edges.isEmpty()) {
            return Collections.EMPTY_LIST;
        } else {
            final List<List<DefaultWeightedEdge>> res = new ArrayList();
            edges.stream().forEach(e->{
                final double[] q = graph.getEdgeSource(e);
                final List<List<DefaultWeightedEdge>> paths = getPaths(q,graph);
                if(paths.isEmpty()) {
                    // The explored edge's source has no predecessor, we thus
                    // just add the explored edge to the result as a new path.
                    final List<DefaultWeightedEdge> path = new ArrayList();
                    path.add(e);
                    res.add(path);
                } else {
                    // The explored edge's source has several predecessors, we
                    // thus add the explored edge to each resulting path.
                    paths.stream().forEach(l->{
                        l.add(e);
                        res.add(l);
                    });
                }
            });
            return res;
        }
    }

    public void processGame(Game game) {
        User player = userRepository.findOneByLogin(game.getUsr()).get();
        if(player == null) return;
        Image img = imageRepository.findOne(game.getImg());
        ImageSet imageSet = imageSetRepository.findByName(img.getImageSet());
        GameMode mode = game.getGameMode();
        objectiveRepository.findAllByUsr(player.getLogin()).stream()
            .filter(o -> o.getGameMode() == mode && o.getNbCompletedGames() < o.getNbGamesToComplete())
            .sorted(Comparator.comparingLong(o->o.getCreationDate().getMillis()))
        .findFirst().ifPresent(objective -> {
            objective.getPendingGames().add(game.getId());
            objective.setNbCompletedGames(objective.getNbCompletedGames() + 1);
            objectiveRepository.save(objective);
        });
        userRepository.save(player);
        objectiveService.handleObjectiveUpdate(player.getLogin());

        ImageModeStatus imageModeStatus = img.getModeStatus().get(mode);
        List<GameResult> results = imageModeStatus.getGameResults();
        results.add(game.getGameResult());
        imageModeStatus.setSubmittedGames(results.size());
        imageRepository.save(img);

        //If the image hasn't been processed yet for the given mode but has accrued enough submissions, it is processed now.
        if(!imageModeStatus.getStatus().equals(ImageStatus.PROCESSED) && results.size() >= imageSet.getRequiredSubmissions()) {
            processImage(mode,results,img);
        }
    }

    public void processImage(GameMode mode, List<GameResult> results, Image image) {
        Map processedResults = null;
        switch (mode) {
            case PlantIdentification: {
                List<PlantIdentificationResult> plantResults = results.stream()
                    .filter(r -> r instanceof PlantIdentificationResult)
                    .map(r -> (PlantIdentificationResult) r).collect(Collectors.toList());
                    /*Map<PlantSpecies, List<Boolean>> */processedResults = processPlantIdentification(plantResults);
            }
            break;
            case AnimalIdentification: {
                List<AnimalIdentificationResult> animalResults = results.stream()
                    .filter(r -> r instanceof AnimalIdentificationResult)
                    .map(r -> (AnimalIdentificationResult) r).collect(Collectors.toList());
                    /*Map<AnimalSpecies, List<double[]>> */processedResults = processAnimalIdentification(animalResults);

            }
            break;
            case AllStars: {
                List<AllStarsResult> allStarsResults = results.stream()
                    .filter(r -> r instanceof AllStarsResult)
                    .map(r -> (AllStarsResult) r).collect(Collectors.toList());
                    /*Map<Species,Boolean> */processedResults = processAllStars(allStarsResults);
            }
            break;
        }
        if(processedResults != null) {
            handleGameProcessing(image, mode, processedResults);
        }
    }

    public void handleGameProcessing(Image image, GameMode mode, Map result) {
        ImageModeStatus status = image.getModeStatus().get(mode);
        switch(mode) {
            case AllStars: {
                Map<Species,Boolean> referenceMap = result;

                Set<AnimalSpecies> faunaSpecies = new HashSet<>();
                Set<PlantSpecies> floraSpecies = new HashSet<>();

                referenceMap.keySet().stream().filter(k->referenceMap.get(k)).forEach(k->{
                    try {floraSpecies.add(PlantSpecies.valueOf(k.toString()));}
                    catch (IllegalArgumentException e1) {
                        try {faunaSpecies.add(AnimalSpecies.valueOf(k.toString()));}
                        catch (IllegalArgumentException e2) {
                            e1.printStackTrace();
                            e2.printStackTrace();
                        }
                    }
                });

                status.setStatus(ImageStatus.PROCESSED);
                if(!faunaSpecies.isEmpty()) {
                    ImageModeStatus modeStatus = image.getModeStatus().get(GameMode.AnimalIdentification);
                    if(modeStatus.getStatus().equals(ImageStatus.UNAVAILABLE))
                        modeStatus.setStatus(ImageStatus.NOT_PROCESSED);
                    image.setFaunaSpecies(faunaSpecies);
                }
                if(!floraSpecies.isEmpty()) {
                    ImageModeStatus modeStatus = image.getModeStatus().get(GameMode.PlantIdentification);
                    if(modeStatus.getStatus().equals(ImageStatus.UNAVAILABLE))
                        modeStatus.setStatus(ImageStatus.NOT_PROCESSED);
                    image.setFloraSpecies(floraSpecies);
                }
                status.setReferenceResult(referenceMap);
                imageRepository.save(image);
            }
            break;
            case AnimalIdentification: {
                Map<AnimalSpecies,List<double[]>> referenceMap = result;

                status.setStatus(ImageStatus.PROCESSED);
                Set<AnimalSpecies> set = new HashSet<>();
                referenceMap.entrySet().stream().forEach(e -> {
                    if (!e.getValue().isEmpty())
                        set.add(e.getKey());
                });
                image.setFaunaSpecies(set);
                status.setReferenceResult(referenceMap);
                imageRepository.save(image);
            }
            break;
            case PlantIdentification: {
                Map<PlantSpecies,List<Boolean>> referenceMap = result;

                status.setStatus(ImageStatus.PROCESSED);
                Set<PlantSpecies> set = new HashSet<>();
                referenceMap.entrySet().stream().forEach(e -> {
                    if (e.getValue().contains(true))
                        set.add(e.getKey());
                });
                image.setFloraSpecies(set);
                status.setReferenceResult(referenceMap);
                imageRepository.save(image);
            }
            break;
        }
        log.debug("Updated image : {}", image);
        rateGames(image, mode);
    }
}
