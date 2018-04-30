package com.example.serialization;

import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary.AdditionVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary.DivisionVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary.MultiplicationVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary.PowerVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import org.json.JSONObject;

import java.util.Hashtable;

public class VertexFactory{

    Hashtable<String,Vertex> vertex_map;

    private DoubleVertex getVertexOrDouble(JSONObject obj, String key, String postfix){
        DoubleVertex v;
        if(obj.get(key) instanceof Double){
            v = new ConstantDoubleVertex(obj.getDouble(key));
        }
        else{
            String source = obj.getString(key);
            if(vertex_map.keySet().contains(source)){
                v = (DoubleVertex)vertex_map.get(obj.getString(key));
            }
            else{
                v = (DoubleVertex)vertex_map.get(obj.getString(key)+postfix);
            }

        }
        return v;
    }

    public Vertex buildGaussian(JSONObject obj, String postfix){
        DoubleVertex mean = getVertexOrDouble(obj,"mean", postfix);
        DoubleVertex std = getVertexOrDouble(obj,"std",postfix);
        GaussianVertex v = new GaussianVertex(mean,std);
        return (Vertex)v;
    }

    public Vertex makeDoubleConstant(JSONObject obj, String postfix){
        ConstantDoubleVertex v = new ConstantDoubleVertex(obj.getDouble("value"));
        return (Vertex)v;
    }

    public Vertex makeMultiplication(JSONObject obj, String postfix){
        DoubleVertex a = getVertexOrDouble(obj,"a",postfix);
        DoubleVertex b = getVertexOrDouble(obj,"b",postfix);
        MultiplicationVertex v = new MultiplicationVertex(a,b);
        return (Vertex)v;
    }

    public Vertex makePower(JSONObject obj, String postfix){
        DoubleVertex base = getVertexOrDouble(obj,"base",postfix);
        DoubleVertex exponent = getVertexOrDouble(obj,"exponent",postfix);
        PowerVertex v = new PowerVertex(base,exponent);
        return (Vertex)v;
    }

    public Vertex makeDivision(JSONObject obj, String postfix){
        DoubleVertex numerator = getVertexOrDouble(obj,"numerator",postfix);
        DoubleVertex denominator = getVertexOrDouble(obj,"denominator",postfix);
        DivisionVertex v = new DivisionVertex(numerator,denominator);
        return (Vertex)v;
    }

    public Vertex makeAddition(JSONObject obj, String postfix){
        DoubleVertex a = getVertexOrDouble(obj,"a",postfix);
        DoubleVertex b = getVertexOrDouble(obj,"b",postfix);
        AdditionVertex v = new AdditionVertex(a,b);
        return (Vertex)v;
    }

    public VertexFactory(Hashtable<String,Vertex> vertex_map){
        this.vertex_map = vertex_map;
        return;

    }

    public Vertex build(JSONObject obj, String postfix){
        String name = obj.getString("type").toUpperCase();
        Vertex v = null;
        switch(name){
            case "GAUSSIAN": v = buildGaussian(obj, postfix); break;
            case "DOUBLE_CONSTANT": v = makeDoubleConstant(obj, postfix); break;
            case "MULTIPLICATION": v = makeMultiplication(obj, postfix); break;
            case "ADDITION": v = makeAddition(obj, postfix); break;
            case "POWER": v = makePower(obj,postfix); break;
            case "DIVISION": v = makeDivision(obj,postfix); break;

        }
        return v;

    }




}
