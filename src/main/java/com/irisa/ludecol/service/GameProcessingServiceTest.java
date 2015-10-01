package com.irisa.ludecol.service;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.*;

/**
 * Created by dorian on 24/09/15.
 */

public class GameProcessingServiceTest extends Application {

    private int nb_submissions = 3;
    private int nb_points = 10;
    private Random rand = new Random();
    private GameProcessingService service = new GameProcessingService();
    private Map<double[],List<double[]>> points;
    private List<List<double[]>> layers;
    private List<double[]> result;

    private void simulate(Group group) {

        final List<double[]> base = new ArrayList<>();
        for(int j=0;j<nb_points;j++) {
            base.add(new double[]{rand.nextDouble() * 500,rand.nextDouble() * 500});
        }
        layers = new ArrayList<>();
        for(int i=0;i<nb_submissions;i++) {
            List<double[]> tmp = new ArrayList<>();
            for(int j=0;j<nb_points;j++) {
                double[] point = base.get(j);
                double theta = rand.nextDouble() * 2 * Math.PI;
                double r = rand.nextDouble() * 64;
                double x = point[0] + r * Math.cos(theta);
                double y = point[1] + r * Math.sin(theta);
                tmp.add(new double[]{x,y});
            }
            layers.add(tmp);
        }
        points = new HashMap<>();
        for(int i=0;i<nb_submissions-1;i++) {
            for(double[] p : layers.get(i)) {
                final List<double[]> tmp = new ArrayList<>();
                for(int j=i+1;j<nb_submissions;j++) {
                    for(double[] q : layers.get(j)) {
                        double x = p[0]-q[0];
                        double y = p[1]-q[1];
                        if(x*x+y*y <= 64*64) {
                            tmp.add(q);
                        }
                    }
                }
                points.put(p,tmp);
            }
        }
        base.clear();
        for(int i=0;i<nb_points;i++) {
            double x=0;
            double y=0;
            for(int j=0;j<nb_submissions;j++) {
                double[] p = layers.get(j).get(i);
                x+=p[0];
                y+=p[1];
            }
            x/=nb_submissions;
            y/=nb_submissions;
            base.add(new double[]{x,y});
        }

        result = service.createGraph(points);
        List children = group.getChildren();
        children.clear();

        for(int i=0;i<layers.size();i++) {
            List<double[]> layer = layers.get(i);
            for(int j=0;j<layer.size();j++) {
                final double[] point = layer.get(j);
                final double r = i==0?1:0;
                final double g = i==1?1:0;
                final double b = i==2?1:0;
                Circle pointShape = new Circle(point[0]+100,point[1]+100,1);
                pointShape.strokeProperty().setValue(new Color(r,g,b,1));
                pointShape.fillProperty().setValue(new Color(r,g,b,1));
                Circle circleShape = new Circle(point[0]+100,point[1]+100,32);
                circleShape.strokeProperty().setValue(new Color(r,g,b,0.1));
                circleShape.fillProperty().setValue(new Color(r,g,b,0.1));
                children.add(pointShape);
                children.add(circleShape);
            }
        }
        for(int i=0;i<result.size();i++) {
            double[] point = result.get(i);
            Circle pointShape = new Circle(point[0]+100,point[1]+100,1);
            pointShape.strokeProperty().setValue(new Color(0,0,0,1));
            children.add(pointShape);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(new Color(1,1,1,1),null,null)));

        Group resultGroup = new Group();
        Button btn = new Button();
        btn.setText("New simulation");
        btn.setOnAction((e)->simulate(resultGroup));

        root.setTop(btn);
        root.setCenter(resultGroup);

        Scene scene = new Scene(root, 400, 400);

        primaryStage.setTitle("Game processing simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


//    public static void main(String[] args) {
//        launch(args);
//    }
}
