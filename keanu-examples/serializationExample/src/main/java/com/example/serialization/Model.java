package com.example.serialization;


import io.improbable.keanu.network.BayesNet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import io.improbable.keanu.algorithms.variational.GradientOptimizer;
import io.improbable.keanu.vertices.Vertex;

import javax.annotation.Resource;


public class Model {

    HashMap DegreeOfVertexes;

    private static String getResource(String filename){
        StringBuilder result = new StringBuilder("");

        //Get file from resources folder
        ClassLoader classLoader = Model.class.getClassLoader();
        File file = new File(classLoader.getResource(filename).getFile());

        try (Scanner scanner = new Scanner(file)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }

            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();


    }

    public static void main(String[] args) {


        ClassLoader classLoader = Model.class.getClassLoader();

        System.out.println("Loading the model");
        ModelFromJSON m = new ModelFromJSON(new File(classLoader.getResource("logistic_regression.json").getFile()));


        BayesNet b = m.getNetworkOfConnectedGraph();


        List<Vertex<?>> latents = b.getLatentVertices();




        GradientOptimizer optimizer = new GradientOptimizer(b);
        optimizer.maxLikelihood(10000);

        Hashtable <String,Double>ht =  m.getVertecValues();

        for(String s:ht.keySet()){
            System.out.printf("%s %f\n",s,ht.get(s));
        }


    }



}
