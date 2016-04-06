package com.example;

import java.util.HashMap;

public class EdgeOutput {
	public Long src_IP;
	public Long dst_IP;
	public int src_id;
	public int dst_id;
	public double weight;
	public int index;
	
	public EdgeOutput(Long src_ip, Long dst_ip, int sid, int did)
	{
		src_IP = src_ip;
		dst_IP = dst_ip;
		src_id = sid;
		dst_id = did;
	}
	
	public HashMap<String, Object> toMap()
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("src_id", src_id);
		map.put("dst_id", dst_id);
		map.put("weight", weight);
		map.put("load", 1);
		map.put("index", index);
		return map;
	}
}
