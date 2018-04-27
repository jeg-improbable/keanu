package com.example.serialization;

import io.improbable.keanu.vertices.Vertex;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Stack;

public class Node{
    private HashSet<Node> sources;
    private HashSet<Node> destinations;
    String name;
    JSONObject obj;
    boolean is_replicate;
    private Vertex v;

    public Node(String name, JSONObject obj){
        this.name = new String(name);
        this.obj = obj;
        this.sources = new HashSet<Node>();
        this.destinations = new HashSet<Node>();
        is_replicate = false;

        v = null;
    }

    public void setReplicate(boolean b){
        is_replicate = b;
    }

    public void add_source(Node source){

        System.out.printf("Adding source %s<=%s\n",name,source.name);

        sources.add(source);
        source.add_destinations(this);
    }

    public void add_destinations(Node n){
        this.destinations.add(n);
    }

    public void crawl_to_target(String target_name, HashSet<ArrayList<Node>> paths, Stack<Node> current_path){

        current_path.push(this);

        System.out.printf("Crawling %s\n",name);
        for(Node n: sources){
            System.out.printf("Source: %s\n",n.getName());
        }

        if(this.name.equals(target_name)){
            ArrayList<Node> path = new ArrayList<Node>();
            for(Node n: current_path){
                path.add(n);
            }
            paths.add(path);
            return;
        }

        for(Node n: this.sources){
            n.crawl_to_target(target_name, paths, current_path);
        }
        current_path.pop();
    }

    public int getNumberOfSources(){
        return this.sources.size();
    }

    public String getName(){
        return this.name;
    }

    public JSONObject getObj(){
        return this.obj;
    }

    public void setVertex(Vertex v){
        this.v = v;
    }

    public void setName(String name){
        this.name = name;
    }

    public void instantiate_with_blacklist(HashSet<Node> blacklist_nodes, Hashtable<String,Vertex> vertex_map, VertexFactory factory,String postfix){

        System.out.printf("instantiate_with_blacklist %s \t %s\n",this.name,postfix);
        if(blacklist_nodes.contains(this)){
            return;
        }

        if(this.v == null || this.is_replicate){
            System.out.printf("\t\t In condition %s\n",this.name);
            boolean have_parents = true;

            for(Node s: this.sources){
                System.out.printf("\t\t Checking %s \n",s.getName());

                if(!(vertex_map.containsKey(s.getName()) || vertex_map.containsKey(s.getName()+postfix))){
                    System.out.printf("Do not have key\n");
                    have_parents = false;
                }
            }

            if(have_parents){
                System.out.println("\tI have my parents!");
                Vertex v = factory.build(this.obj, postfix);
                vertex_map.put(this.name+postfix,v);
                setVertex(v);
            }
        }

        for(Node n: destinations){
            System.out.printf("This is the destination-- %s\n",n.getName());
            n.instantiate_with_blacklist(blacklist_nodes, vertex_map, factory,postfix);
        }

    }
}