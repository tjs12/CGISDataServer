package com.example.servlets;

import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.EdgeOutput;
import com.example.NodeOutput;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mongodb.*;
import com.mongodb.client.*;

import org.bson.*;
import org.json.*;

import com.mysql.jdbc.Driver;


/**
 * Servlet implementation class CGISData
 */
@WebServlet("/CGISData")
public class CGISData extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CGISData() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    private String toIPStr(Long ip)
    {
    	//big endian
    	return Long.toString(ip % 256) + "." + Long.toString(ip % (65536) >> 8) + "." + 
    	Long.toString(ip % (16777216) >> 16) + "." + Long.toString(ip / 16777216);
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub	
		String returnstr = "";
		try {
			/*StringBuffer fileData = new StringBuffer(1000);
			BufferedReader reader = new BufferedReader(new FileReader("example.json"));
			char[] buf = new char[1024];
			int numRead=0;
			while ((numRead=reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}
			reader.close();
			returnstr = fileData.toString();*/
			HashMap<String, List> ret = new HashMap<String, List>();
			//ArrayList<NodeOutput> nodes = new ArrayList<NodeOutput>();
			ArrayList<HashMap<String, Object>> nodes = new ArrayList<HashMap<String, Object>>();
			ArrayList<HashMap<String, Object>> links = new ArrayList<HashMap<String, Object>>();
			
			
			/*Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager.getConnection("jdbc:mysql://localhost:3306/CGISData", "root", "admin");
			returnstr += "connection";
			statement = connect.createStatement();
			ResultSet node_rs = statement.executeQuery("SELECT * FROM IPv4s");
			returnstr += "\nexecute";*/
			NodeOutput i;
			
			MongoClient mongoClient = new MongoClient("localhost");
			MongoDatabase database = mongoClient.getDatabase("CGISData");
			MongoCollection<Document> nodes_doc = database.getCollection("ipv4nodes");
			
			FindIterable<Document> node_rs = nodes_doc.find();


			
			HashMap<Long, Integer> ip_map = new HashMap<Long, Integer> ();
			/*int count = 0;
			while (node_rs.next()) {
				i = new NodeOutput(node_rs.getInt("id"));
				nodes.add(i);
				id_map.put(i.id, count);
				count++;
			}
			
			ResultSet link_rs = statement.executeQuery("SELECT * FROM Edges_IPv4");
			EdgeOutput j;
			while (link_rs.next()) {
				j = new EdgeOutput(link_rs.getInt(1), link_rs.getInt(2));
				j.weight = link_rs.getFloat(3);
				links.add(j);
				nodes.get(id_map.get(j.src_id)).neighbors.add(j.dst_id);
				nodes.get(id_map.get(j.src_id)).weights.add(j.weight);
				nodes.get(id_map.get(j.dst_id)).neighbors.add(j.src_id);
				nodes.get(id_map.get(j.dst_id)).weights.add(j.weight);
			}*/
			
			
			int count = 0;
			node_rs.forEach(new Block<Document>() {
			    @Override
			    public void apply(final Document document) {
			    	try {
			    		//NodeOutput i = new NodeOutput(document.getInteger("dev_id"));
			    		NodeOutput i = new NodeOutput(nodes.size());
						i.name = toIPStr(document.getLong("ip"));
						/*i = new NodeOutput(temp.);
						nodes.add(i);
						id_map.put(i.id, count);*/
						nodes.add(i.toMap());
						ip_map.put(document.getLong("ip"), ip_map.entrySet().size());
						//	count++;
			    	}
			    	catch (Exception e) {
			    		;
			    	}
			    }
			});
			
			
				
				
				
			
			
			/*ResultSet link_rs = statement.executeQuery("SELECT * FROM Edges_IPv4");
			EdgeOutput j;
			while (link_rs.next()) {
				j = new EdgeOutput(link_rs.getInt(1), link_rs.getInt(2));
				j.weight = link_rs.getFloat(3);
				links.add(j);
				nodes.get(id_map.get(j.src_id)).neighbors.add(j.dst_id);
				nodes.get(id_map.get(j.src_id)).weights.add(j.weight);
				nodes.get(id_map.get(j.dst_id)).neighbors.add(j.src_id);
				nodes.get(id_map.get(j.dst_id)).weights.add(j.weight);
			}*/
			
			EdgeOutput j;
			MongoCollection<Document> links_doc = database.getCollection("ipv4links");
			FindIterable<Document> link_rs = links_doc.find();
			
			link_rs.forEach(new Block<Document>() {
			    @Override
			    public void apply(final Document document) {
			    	int src_id = -1;
			    	int dst_id = -1;
			    	double weight = -1;
			    	Long src_ip = (long) 0, dst_ip = (long) 0;
			    	try {
			    		src_ip = document.getLong("src_ip");
			    		dst_ip = document.getLong("dst_ip");
			    		weight = document.getDouble("tdelay");
			    		
			    		src_id = (Integer)nodes.get(ip_map.get(src_ip)).get("id");
			    		//nodes.get(ip_map.get(dst_ip));
			    		
			    		dst_id = (Integer)nodes.get(ip_map.get(dst_ip)).get("id");
			    		
			    		
			    		
			    	}
			    	catch (Exception e) {
			    	
			    	}
			    	
			    	
			    	if (src_id != -1 && dst_id != -1) {
						EdgeOutput j = new EdgeOutput(src_ip, dst_ip, src_id, dst_id);
						j.weight = weight;
						j.index = links.size();
						links.add(j.toMap());
						//double edged
						EdgeOutput j1 = new EdgeOutput(dst_ip, src_ip, dst_id, src_id);
						j1.weight = weight;
						j1.index = links.size();
						links.add(j1.toMap());
						
						Map<String, Object> src_node = nodes.get(src_id);
						Map<String, Object> dst_node = nodes.get(dst_id);
						List<Integer> src_neighbors = (List<Integer>)src_node.get("edgeindex");
						List<Integer> dst_neighbors = (List<Integer>)dst_node.get("edgeindex");
						src_neighbors.add(j.index);
						dst_neighbors.add(j1.index);
					}
			    	
			    }
			});
			
			
			ret.put("nodes", nodes);
			ret.put("links", links);
			
			JSONObject jsonObject = new JSONObject((Map<String, List>)ret);  
			returnstr = jsonObject.toString();  

			
		}
		catch (Exception e)
		{
			returnstr = "{\"nodes\": [{\"index\": 0, \"neighbours\": [9], \"id\": \"59.66.130.1\", \"neighbour_weights\": [1]}, {\"index\": 1, \"neighbours\": [10, 3], \"id\": \"118.229.4.98\", \"neighbour_weights\": [1, 16]}, {\"index\": 2, \"neighbours\": [3, 4], \"id\": \"118.229.4.65\", \"neighbour_weights\": [1, 8]}, {\"index\": 3, \"neighbours\": [2, 1], \"id\": \"118.229.4.33\", \"neighbour_weights\": [16, 1]}, {\"index\": 4, \"neighbours\": [2, 5, 6, 8], \"id\": \"202.112.38.9\", \"neighbour_weights\": [8, 26, 3, 5]}, {\"index\": 5, \"neighbours\": [4], \"id\": \"101.4.116.193\", \"neighbour_weights\": [3]}, {\"index\": 6, \"neighbours\": [4], \"id\": \"101.4.118.77\", \"neighbour_weights\": [5]}, {\"index\": 7, \"neighbours\": [10, 9], \"id\": \"118.229.2.113\", \"neighbour_weights\": [3, 1]}, {\"index\": 8, \"neighbours\": [4], \"id\": \"101.4.113.209\", \"neighbour_weights\": [26]}, {\"index\": 9, \"neighbours\": [0, 7], \"id\": \"118.229.2.117\", \"neighbour_weights\": [1, 3]}, {\"index\": 10, \"neighbours\": [7, 1], \"id\": \"118.229.2.65\", \"neighbour_weights\": [1, 1]}], \"links\": [{\"source\": 0, \"target\": 9, \"weight\": 1}, {\"source\": 1, \"target\": 10, \"weight\": 1}, {\"source\": 1, \"target\": 3, \"weight\": 16}, {\"source\": 2, \"target\": 3, \"weight\": 1}, {\"source\": 2, \"target\": 4, \"weight\": 8}, {\"source\": 3, \"target\": 2, \"weight\": 16}, {\"source\": 3, \"target\": 1, \"weight\": 1}, {\"source\": 4, \"target\": 2, \"weight\": 8}, {\"source\": 4, \"target\": 5, \"weight\": 26}, {\"source\": 4, \"target\": 6, \"weight\": 3}, {\"source\": 4, \"target\": 8, \"weight\": 5}, {\"source\": 5, \"target\": 4, \"weight\": 3}, {\"source\": 6, \"target\": 4, \"weight\": 5}, {\"source\": 7, \"target\": 10, \"weight\": 3}, {\"source\": 7, \"target\": 9, \"weight\": 1}, {\"source\": 8, \"target\": 4, \"weight\": 26}, {\"source\": 9, \"target\": 0, \"weight\": 1}, {\"source\": 9, \"target\": 7, \"weight\": 3}, {\"source\": 10, \"target\": 7, \"weight\": 1}, {\"source\": 10, \"target\": 1, \"weight\": 1}]}";//ioe.getLocalizedMessage();
			// e.printStackTrace(new PrintStream(returnstr));;
			returnstr += "\nError!" + e.toString();
			e.printStackTrace();
		}
		//returnstr = "{\"nodes\": [{\"index\": 0, \"neighbours\": [9], \"id\": \"59.66.130.1\", \"neighbour_weights\": [1]}, {\"index\": 1, \"neighbours\": [10, 3], \"id\": \"118.229.4.98\", \"neighbour_weights\": [1, 16]}, {\"index\": 2, \"neighbours\": [3, 4], \"id\": \"118.229.4.65\", \"neighbour_weights\": [1, 8]}, {\"index\": 3, \"neighbours\": [2, 1], \"id\": \"118.229.4.33\", \"neighbour_weights\": [16, 1]}, {\"index\": 4, \"neighbours\": [2, 5, 6, 8], \"id\": \"202.112.38.9\", \"neighbour_weights\": [8, 26, 3, 5]}, {\"index\": 5, \"neighbours\": [4], \"id\": \"101.4.116.193\", \"neighbour_weights\": [3]}, {\"index\": 6, \"neighbours\": [4], \"id\": \"101.4.118.77\", \"neighbour_weights\": [5]}, {\"index\": 7, \"neighbours\": [10, 9], \"id\": \"118.229.2.113\", \"neighbour_weights\": [3, 1]}, {\"index\": 8, \"neighbours\": [4], \"id\": \"101.4.113.209\", \"neighbour_weights\": [26]}, {\"index\": 9, \"neighbours\": [0, 7], \"id\": \"118.229.2.117\", \"neighbour_weights\": [1, 3]}, {\"index\": 10, \"neighbours\": [7, 1], \"id\": \"118.229.2.65\", \"neighbour_weights\": [1, 1]}], \"links\": [{\"source\": 0, \"target\": 9, \"weight\": 1}, {\"source\": 1, \"target\": 10, \"weight\": 1}, {\"source\": 1, \"target\": 3, \"weight\": 16}, {\"source\": 2, \"target\": 3, \"weight\": 1}, {\"source\": 2, \"target\": 4, \"weight\": 8}, {\"source\": 3, \"target\": 2, \"weight\": 16}, {\"source\": 3, \"target\": 1, \"weight\": 1}, {\"source\": 4, \"target\": 2, \"weight\": 8}, {\"source\": 4, \"target\": 5, \"weight\": 26}, {\"source\": 4, \"target\": 6, \"weight\": 3}, {\"source\": 4, \"target\": 8, \"weight\": 5}, {\"source\": 5, \"target\": 4, \"weight\": 3}, {\"source\": 6, \"target\": 4, \"weight\": 5}, {\"source\": 7, \"target\": 10, \"weight\": 3}, {\"source\": 7, \"target\": 9, \"weight\": 1}, {\"source\": 8, \"target\": 4, \"weight\": 26}, {\"source\": 9, \"target\": 0, \"weight\": 1}, {\"source\": 9, \"target\": 7, \"weight\": 3}, {\"source\": 10, \"target\": 7, \"weight\": 1}, {\"source\": 10, \"target\": 1, \"weight\": 1}]}";//ioe.getLocalizedMessage();
		response.getWriter().write(returnstr);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	
	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

}
