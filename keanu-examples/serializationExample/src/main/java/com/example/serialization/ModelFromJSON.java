package com.example.serialization;

import java.io.File;

import io.improbable.keanu.network.BayesNet;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.probabilistic.ProbabilisticDouble;
import org.apache.commons.io.FileUtils;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class ModelFromJSON {

    private HashMap<String,Integer> number_of_inputs_by_name;

    private HashSet<String> output_observation_names;
    private HashSet<String> input_observation_names;

    private HashMap<String,Node> nodes_by_name;
    private HashSet<Node> independent_nodes;

    private HashSet<Node> replicate_nodes;

    private HashMap<String,Vertex> vertexes_by_name;

    private VertexFactory vertex_factory;
    private Hashtable<String,ArrayList<Double>> observation_values;

    private HashSet<String> output_names_of_interest;

    public ModelFromJSON(File file) {

        try {
            System.out.println("Working Directory = " +
                    System.getProperty("user.dir"));



            //File file = new File(filename);
            String content = FileUtils.readFileToString(file, "utf-8");
            JSONObject object = new JSONObject(content);
            countNumberOfInputsByName(object);

            independent_nodes = new HashSet<>();
            replicate_nodes = new HashSet<>();

            createObjsByName(object);

            output_observation_names = new HashSet<>();
            input_observation_names = new HashSet<>();

            observation_values = new Hashtable<>();
            output_names_of_interest = new HashSet<String>();

            getObservationObjects(object, "output", output_observation_names);
            getObservationObjects(object, "input", input_observation_names);

            getPathsBetweenInputndOutpus();


            // This method builds vertex
            vertexes_by_name = new HashMap<>();

            vertex_factory = new VertexFactory(vertexes_by_name);


            instantiate_independent_vertexes();
            build_nonreplicate_network();
            build_replicate_networks();

            //this.prettyPrint();

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void instantiate_independent_vertexes(){
        for(Node n: independent_nodes){
            if(!replicate_nodes.contains(n)){
                Vertex v = vertex_factory.build(n.getObj(),"");
                vertexes_by_name.put(n.getName(),v);
                n.setVertex(v);

                if(n.getObj().keySet().contains("report_final_value")){
                    if(n.getObj().getBoolean("report_final_value")){
                        output_names_of_interest.add(n.getName());
                    }
                }

            }
        }

    }

    private void getPathsBetweenInputndOutpus(){

        HashSet<ArrayList<Node>> paths = new HashSet<ArrayList<Node>>();
        for(String input: input_observation_names){
            for(String outputs: output_observation_names){
                Node source = this.nodes_by_name.get(outputs);
                source.crawl_to_target(input,paths,new Stack<Node>());
            }
        }



        for(ArrayList<Node> l: paths){
            for(Node n: l){
                n.setReplicate(true);
                replicate_nodes.add(n);
            }
        }



    }

    private void createObjsByName(JSONObject object){
        nodes_by_name = new HashMap<String,Node>();

        JSONArray net = object.getJSONArray("network");

        for(int i = 0; i<net.length(); ++i){
            JSONObject obj = net.getJSONObject(i);
            String name = obj.getString("name");
            Node n = new Node(obj.getString("name"), obj);
            nodes_by_name.put(name,n);

        }

        for(int i=0; i<net.length(); ++i){
            JSONObject obj = net.getJSONObject(i);
            Set<String> keys = obj.keySet();
            for(String s:keys){
                if(!s.equals("name") && !s.equals("type")){
                    if(obj.get(s) instanceof String){
                        Node n = nodes_by_name.get(obj.getString("name"));
                        Node linked = nodes_by_name.get(obj.getString(s));
                        n.add_source(linked);
                    }
                }
            }

            if(nodes_by_name.get(obj.getString("name")).getNumberOfSources() == 0){
                independent_nodes.add(nodes_by_name.get(obj.getString("name")));
            }
        }
    }

    private void getObservationObjects(JSONObject object, String compare, HashSet<String> target){

        JSONArray observations = object.getJSONArray("observations");

        for(int i = 0; i<observations.length(); ++i){
            if(observations.getJSONObject(i).getString("type").equals(compare)){
                String name = observations.getJSONObject(i).getString("name");
                target.add(name);
                ArrayList<Double> list = new ArrayList<>();



                JSONArray a = observations.getJSONObject(i).getJSONArray("samples");
                ArrayList<Double> sampleArray = new ArrayList<Double>();

                for(Object b: a){
                    Double d;
                    if(b instanceof Integer)
                        d = ((Integer) b).doubleValue();
                    else
                        d = (Double)b;
                    sampleArray.add(d);

                }
                observation_values.put(name,sampleArray);

                //observation_values.put(observations.getJSONObject(i).getString("name"),);

            }
        }

    }


    private void countNumberOfInputsByName(JSONObject object){

        number_of_inputs_by_name = new HashMap<String,Integer>();

        JSONArray net = object.getJSONArray("network");

        for(int i = 0; i<net.length(); ++i){
            JSONObject obj = net.getJSONObject(i);
            Set<String> keys= obj.toMap().keySet();

            String name = obj.getString("name");
            number_of_inputs_by_name.put(name,0);

            for(String s: keys){
                // If the current member defines the input to a vertex
                if(!s.equals("name") && !s.equals("type")){
                    // If this member defines a link to another node (is a string)
                    if(obj.get(s) instanceof String){
                        number_of_inputs_by_name.put(name,number_of_inputs_by_name.get(name)+1);
                    }
                }
            }
        }

    }

    public void prettyPrint(){
        System.out.print("ModelFromJson\n\t Inputs: ");
        for (String s: this.input_observation_names){
            System.out.printf("%s ",s);
        }
        System.out.print("\n\t Outputs: ");

        for (String s: this.output_observation_names){
            System.out.printf("%s ",s);
        }

        System.out.print("\n\t Independent: ");
        for (Node n: this.independent_nodes){
            System.out.printf("%s ",n.getName());
        }

        System.out.println("Network\n");
        for (String s: vertexes_by_name.keySet()){
            System.out.printf("\t%s\n",s);
        }
    }

    private void build_nonreplicate_network(){
        for(Node n:independent_nodes){
            n.instantiate_with_blacklist(replicate_nodes,vertexes_by_name,vertex_factory,"");
        }
    }

    public void build_replicate_networks(){

        int length = observation_values.get(observation_values.keys().nextElement()).size();

        for(Integer i=0; i<length; ++i){
            String postfix = i.toString();
            for(String s:input_observation_names){



                Node n = nodes_by_name.get(s);

                double value = observation_values.get(s).get(i);
                n.getObj().put("value",value);
                n.instantiate_with_blacklist(new HashSet<Node>(),vertexes_by_name,vertex_factory,postfix);
            }
            for(String s:output_observation_names){


                ProbabilisticDouble v = (ProbabilisticDouble)vertexes_by_name.get(s+postfix);
                Double value = observation_values.get(s).get(i);
                v.observe(value);
            }
        }
    }

    public BayesNet getNetworkOfConnectedGraph(){
        Vertex v = vertexes_by_name.get(vertexes_by_name.keySet().iterator().next());
        return new BayesNet(v.getConnectedGraph());
    }

    public Hashtable <String,Double> getVertecValues(){
        Hashtable <String,Double> ht = new Hashtable<>();

        for(String n: output_names_of_interest){
            ht.put(n,(Double)vertexes_by_name.get(n).getValue());
        }
        /*
        for(String s:vertexes_by_name.keySet()){
            ht.put(s,(Double)vertexes_by_name.get(s).getValue());
        }*/
        return ht;

    }

}