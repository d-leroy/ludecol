package com.irisa.ludecol.service;

import com.irisa.ludecol.domain.*;
import com.irisa.ludecol.domain.subdomain.*;
import com.irisa.ludecol.repository.*;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.WeightedGraph;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.DepthFirstIterator;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collector;
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
    private ProcessedGameRepository processedGameRepository;

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

    @Inject
    private ReferenceGameRepository referenceGameRepository;


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

    private void processPlantIdentification(final PlantIdentificationResult gameResult, final ProcessedPlantIdentificationResult processedResult) {
        Map<PlantSpecies,List<Boolean>> submittedMap = gameResult.getSpeciesMap();
        Map<PlantSpecies,List<Double>> processedMap = processedResult.getSpeciesMap();

        for(PlantSpecies key : submittedMap.keySet()) {
            List<Double> processedList = processedMap.get(key);
            if(processedList == null) {
                processedList = new ArrayList<>();
                processedMap.put(key,processedList);
            }
            processPresenceGrid(submittedMap.get(key), processedList, processedResult.getNbResults());
        }

        processedResult.setNbResults(processedResult.getNbResults()+1);
    }

    private void processAllStars(final AllStarsResult gameResult, final ProcessedAllStarsResult processedResult) {
        Map<Species,Integer> submittedMap = gameResult.getSpeciesMap();
        Map<Species,Pair<Double>> processedMap = processedResult.getSpeciesMap();
        int n = processedResult.getNbResults();

        for(Species key : submittedMap.keySet()) {
            Pair<Double> processedPair = processedMap.get(key);
            if(processedPair == null) {
                processedPair = new Pair<>(0.,0.);
                processedMap.put(key, processedPair);
            }
            int val = submittedMap.get(key);
            switch(val) {
                case -1:
                    processedPair.setY((processedPair.getY() * n + 1.) / (n+1.));
                    break;
                case 0: break;
                case 1:
                    processedPair.setX((processedPair.getX() * n + 1.) / (n+1.));
                    break;
                default:
            }
        }

        processedResult.setNbResults(n + 1);
    }

    private void processAnimalIdentification(final AnimalIdentificationResult gameResult, final ProcessedAnimalIdentificationResult processedResult) {
        Map<AnimalSpecies,List<double[]>> submittedMap = gameResult.getSpeciesMap();
        Map<AnimalSpecies,List<double[]>> processedMap = processedResult.getSpeciesMap();

        submittedMap.keySet().stream().forEach(key -> {
            List<double[]> processedList = processedMap.get(key);
            if (processedList == null) {
                processedMap.put(key, submittedMap.get(key));
            } else {
                processedList.addAll(submittedMap.get(key));
            }
        });

        processedResult.setNbResults(processedResult.getNbResults() + 1);
    }

    private void configureProcessedGame(ProcessedGame processedGame, ProcessedGameResult result, Game game) {
        processedGame.setImg(game.getImg());
        processedGame.setGameMode(game.getGameMode());
        processedGame.setProcessedGameResult(result);
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
                        log.debug("--------------Distance : {}-------------",dist);
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
        Optional<User> playerRes = userRepository.findOneByLogin(game.getUsr());
        if(playerRes.get() != null) {
            User player = playerRes.get();
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

    public void rateGame(Game game) {
        switch(game.getGameMode()) {
            case AllStars: {
                Map<Species,Integer> referenceMap = ((AllStarsResult) game.getCorrectedGameResult()).getSpeciesMap();
                Map<Species,Integer> submittedMap = ((AllStarsResult) game.getGameResult()).getSpeciesMap();

                int mistakes = 0;
                int correct = 0;
                int total = 0;

                for(Species key : referenceMap.keySet()) {
                    int r = referenceMap.get(key);
                    int s = submittedMap.get(key);
                    mistakes += (s != 0 && r != s) ? 1 : 0;
                    correct += r == s ? 1 : 0;
                    total++;
                }

                int score = (int) Math.floor(100 * Math.max(correct - mistakes, 0) / (total*1.));
                //Award points to users that played on the image and add a notification to their notification queue.
                awardPoints(game, score);
            }
            break;
            case AnimalIdentification: {
                Map<AnimalSpecies,List<double[]>> referenceMap = ((AnimalIdentificationResult) game.getCorrectedGameResult()).getSpeciesMap();
                Map<AnimalSpecies,List<double[]>> submittedMap = ((AnimalIdentificationResult) game.getGameResult()).getSpeciesMap();

                int mistakes = 0;
                int correct = 0;
                int total = 0;

                for(AnimalSpecies key : submittedMap.keySet()) {
                    List<double[]> referenceList = referenceMap.get(key);
                    List<double[]> submittedList = submittedMap.get(key);
                    List<double[]> correctedList = matchPointSets(submittedList,referenceList);
                    mistakes += submittedList.size() - correctedList.size();
                    correct += correctedList.size();
                    total += referenceList.size();
                }

                int score = (int) Math.floor(100 * Math.max(correct - mistakes, 0) / (total*1.));
                //Award points to users that played on the image and add a notification to their notification queue.
                awardPoints(game,score);
            }
            break;
            case PlantIdentification: {
                Map<PlantSpecies,List<Boolean>> referenceMap = ((PlantIdentificationResult) game.getCorrectedGameResult()).getSpeciesMap();
                Map<PlantSpecies,List<Boolean>> submittedMap = ((PlantIdentificationResult) game.getGameResult()).getSpeciesMap();

                int mistakes = 0;
                int correct = 0;
                int total = 0;

                for(PlantSpecies key : submittedMap.keySet()) {
                    List<Boolean> referenceList = referenceMap.get(key);
                    List<Boolean> submittedList = submittedMap.get(key);
                    for(int i = 0; i<referenceList.size(); i++) {
                        mistakes += (referenceList.get(i) != submittedList.get(i)) ? 1 : 0;
                        correct += (referenceList.get(i) && submittedList.get(i)) ? 1 : 0;
                        total += referenceList.get(i) ? 1 : 0;
                    }
                }

                int score = (int) Math.floor(100 * Math.max(correct - mistakes, 0) / (total*1.));
                //Award points to users that played on the image and add a notification to their notification queue.
                awardPoints(game,score);
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
            if (l.size() >= MIN_PATH_LENGTH) {
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
                    // The explored edge's source has not predecessor, we thus
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
        ImageModeStatus modeStatus = img.getModeStatus().get(game.getGameMode());
        objectiveRepository.findAllByUsr(player.getLogin()).stream()
            .filter(o -> o.getGameMode() == game.getGameMode() && o.getNbCompletedGames() < o.getNbGamesToComplete())
            .sorted(Comparator.comparingLong(o->o.getCreationDate().getMillis()))
        .findFirst().ifPresent(objective -> {
            objective.getPendingGames().add(game.getId());
            objective.setNbCompletedGames(objective.getNbCompletedGames() + 1);
            objectiveRepository.save(objective);
        });
        userRepository.save(player);
        objectiveService.handleObjectiveUpdate(player.getLogin());

        ProcessedGame processedGame = processedGameRepository.findByImgAndGameMode(game.getImg(), game.getGameMode());
        switch(game.getGameMode()) {
            case PlantIdentification: {
                if(processedGame == null) {
                    processedGame = new ProcessedGame();
                    ProcessedPlantIdentificationResult processedGameResult = new ProcessedPlantIdentificationResult();
                    configureProcessedGame(processedGame,processedGameResult,game);
                }
                processPlantIdentification((PlantIdentificationResult) game.getGameResult(),
                    (ProcessedPlantIdentificationResult) processedGame.getProcessedGameResult());
                log.debug("Processed game : {}", processedGame);
                processedGameRepository.save(processedGame);
                if(processedGame.getProcessedGameResult().getNbResults() >= imageSet.getRequiredSubmissions()) {
                    if(modeStatus.getStatus().equals(ImageStatus.NOT_PROCESSED)) {
                        modeStatus.setStatus(ImageStatus.IN_PROCESSING);
                    }
                }
            }
            break;
            case AnimalIdentification: {
                if(processedGame == null) {
                    processedGame = new ProcessedGame();
                    ProcessedAnimalIdentificationResult processedGameResult = new ProcessedAnimalIdentificationResult();
                    configureProcessedGame(processedGame,processedGameResult,game);
                }
                processAnimalIdentification((AnimalIdentificationResult) game.getGameResult(),
                    (ProcessedAnimalIdentificationResult) processedGame.getProcessedGameResult());
                log.debug("Processed game : {}", processedGame);
                processedGameRepository.save(processedGame);
                if(processedGame.getProcessedGameResult().getNbResults() >= imageSet.getRequiredSubmissions()) {
                    if(modeStatus.getStatus().equals(ImageStatus.NOT_PROCESSED)) {
                        modeStatus.setStatus(ImageStatus.IN_PROCESSING);
                    }
                }
            }
            break;
            case AllStars: {
                if(processedGame == null) {
                    processedGame = new ProcessedGame();
                    ProcessedAllStarsResult processedGameResult = new ProcessedAllStarsResult();
                    configureProcessedGame(processedGame,processedGameResult,game);
                }
                processAllStars((AllStarsResult) game.getGameResult(),
                    (ProcessedAllStarsResult) processedGame.getProcessedGameResult());
                log.debug("Processed game : {}", processedGame);
                processedGameRepository.save(processedGame);
                if(processedGame.getProcessedGameResult().getNbResults() >= imageSet.getRequiredSubmissions()) {
                    if(modeStatus.getStatus().equals(ImageStatus.NOT_PROCESSED)) {
                        modeStatus.setStatus(ImageStatus.IN_PROCESSING);
                    }
                }
            }
            break;
        }
        imageRepository.save(img);

        if(modeStatus.getStatus().equals(ImageStatus.PROCESSED)) {
            ReferenceGame referenceGame = referenceGameRepository.findByImgAndGameMode(game.getImg(), game.getGameMode());
            if(referenceGame != null) {
                game.setCorrectedGameResult(referenceGame.getGameResult());
                rateGame(game);
            }

        }
    }
}
