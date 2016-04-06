package com.example;

import java.util.ArrayList;
import java.util.HashMap;


public class NodeOutput {
	public Long ipv4;
	public int id;
	public String name;
	//public ArrayList<Integer> neighbors;
	public ArrayList<Double> weights;
	public NodeOutput(int _id)
	{
		id = _id;
	}
	
	public HashMap<String, Object> toMap()
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("name", name);
		map.put("edgeindex", new ArrayList<Integer>());
		return map;
	}
}
