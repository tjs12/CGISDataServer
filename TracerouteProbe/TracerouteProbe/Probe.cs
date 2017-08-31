using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Net;
using System.Data;
//using MySql.Data.MySqlClient;
using MongoDB.Driver;
using MongoDB.Bson;

namespace TracerouteProbe
{
    class EdgeType 
    {
        public IPAddress dest;
        public double weight;
        public EdgeType(IPAddress ipdest, double w)
        {
            dest = ipdest;
            weight = w;
        }
    }

    class Probe
    {
        Traceroute trt;
        Dictionary<IPAddress, double> ipprob;

        public Dictionary<IPAddress, List<EdgeType>> edges;

        private DateTime starttime;

        public Probe()
        {
            trt = new Traceroute();
            ipprob = new Dictionary<IPAddress, double>();
            edges = new Dictionary<IPAddress,List<EdgeType>>();
            targets = new List<IPAddress>();

            //MongoDB
            //client = new MongoClient("mongodb://166.111.68.178:27017");
            client = new MongoClient("mongodb://127.0.0.1:27017");
            //client.DropDatabase("CGISData");
            server = client.GetServer();
            database = server.GetDatabase("CGISData");
            //database.DropCollection("ipv4nodes");
            //database.DropCollection("ipv4links");
            nodes = database.GetCollection<BsonDocument>("ipv4nodes");
            links = database.GetCollection<BsonDocument>("ipv4links");
            node_count = 0;
            starttime = DateTime.Now;
        }

        MongoClient client;
        MongoServer server;
        MongoDatabase database;
        MongoCollection<BsonDocument> nodes, links;
        int node_count;


        double getIPProb(IPAddress ip)
        {
            if (ipprob.Keys.Contains(ip)) return ipprob[ip];
            else return 0.5;
        }

        void foundIP()
        {
        }

        void foundNoIP()
        {
        }

        public List<IPAddress> targets;

        public void doProbe()
        {
            foreach (IPAddress tar in targets)
            {
                trt.Trace(tar);
                Console.WriteLine(edges.Count);
                for (int i = 1; i < trt.Result.Count; i++)
                {
                    EdgeType newedge = null;
                    if (edges.ContainsKey(trt.Result[i - 1].Ip))
                    {
                        bool flag = false;
                        foreach (EdgeType j in edges[trt.Result[i - 1].Ip])
                        {
                            if (j.dest.Equals(trt.Result[i].Ip))
                            {
                                flag = true;

                                break;
                            }
                        }
                        if (!flag)
                        {
                            newedge = new EdgeType(trt.Result[i].Ip, trt.Result[i].Time);
                            edges[trt.Result[i - 1].Ip].Add(newedge);
                        }
                        
                    }
                    else
                    {
                        List<EdgeType> temp_list = new List<EdgeType>();
                        newedge = new EdgeType(trt.Result[i].Ip, trt.Result[i].Time);
                        temp_list.Add(newedge);
                        edges.Add(trt.Result[i - 1].Ip, temp_list);

                        addnodeDatabase(trt.Result[i - 1].Ip, node_count++ - 1);
                        
                    }

                    if (!edges.ContainsKey(trt.Result[i].Ip))
                    {
                        List<EdgeType> temp_list = new List<EdgeType>();
                        edges.Add(trt.Result[i].Ip, temp_list);
                        addnodeDatabase(trt.Result[i].Ip, node_count++ - 1);
                    }

                    if (newedge != null)
                    {
                        addlinkDatabase(trt.Result[i - 1].Ip, newedge);
                    }
                }
            }

            //put into MySQL database
            /*MySqlConnection	conn;
		    MySqlCommand cmd;
            string connStr = String.Format("server=localhost;uid=root;pwd=admin;database=CGISData");
            conn = new MySqlConnection(connStr);
            conn.Open();
            string sql;*/

            

            /*int i_n = 0;//TODO get existing num!
            foreach (IPAddress i in edges.Keys)
            {
                sql = "insert into IPv4s (ip, dev_id) values (" + i.MapToIPv4().Address + ", " + i_n.ToString() + ")";
                cmd = new MySqlCommand(sql, conn);
                //cmd.CommandText = sql;
                cmd.ExecuteNonQuery();
                foreach (EdgeType j in edges[i])
                {
                    sql = "insert into Edges_IPv4 (src_ip, dest_ip, tdelay) values (" + i.MapToIPv4().Address + ", " + j.dest.MapToIPv4().Address + ", " + j.weight.ToString() + ")";
                    cmd.CommandText = sql;
                    cmd.ExecuteNonQuery();
                }
                i_n++;
            }*/

            int i_n = 0;//TODO get existing num!
            /*foreach (IPAddress i in edges.Keys)
            {
                var doc = new BsonDocument
                {
                    {"ip", i.MapToIPv4().Address},
                    {"dev_id", i_n}
                };
                nodes.InsertOneAsync(doc);
                foreach (EdgeType j in edges[i])
                {
                    doc = new BsonDocument
                    {
                        {"src_ip", i.MapToIPv4().Address},
                        {"dst_ip", j.dest.MapToIPv4().Address},
                        {"tdelay", j.weight}
                    };
                    links.InsertOneAsync(doc);
                }
                i_n++;
            }*/


            
        }

        long changeEndian(long i)
        {
            return i<<24 & 0xff000000L | i>>8 & 0xff00 | i<<8 & 0xff0000 | i>>24;
        }

        void addnodeDatabase(IPAddress src, int dev_id)
        {
            var doc = new BsonDocument
                {
                    {"ip", changeEndian(src/*.MapToIPv4()*/.Address)},
                    {"dev_id", dev_id},
                    {"time", starttime}
                };
            lock(nodes)
            nodes.Insert(doc);
        }

        void addlinkDatabase(IPAddress src, EdgeType edge)
        {
            var doc = new BsonDocument
            {
                {"src_ip", changeEndian(src/*.MapToIPv4()*/.Address)},
                {"dst_ip", changeEndian(edge.dest/*.MapToIPv4()*/.Address)},
                {"tdelay", edge.weight},
                {"time", starttime}
            };
            links.Insert(doc);
        }

        public void doProbeOne(object num)
        {
            IPAddress tar = targets[(int)num];
            Traceroute trt1 = new Traceroute();
            trt1.Trace(tar);
            Console.WriteLine(edges.Count);
            for (int i = 1; i < trt1.Result.Count; i++)
            {
                EdgeType newedge = null;
                if (edges.ContainsKey(trt1.Result[i - 1].Ip))
                {
                    bool flag = false;
                    foreach (EdgeType j in edges[trt1.Result[i - 1].Ip])
                    {
                        if (j.dest.Equals(trt1.Result[i].Ip))
                        {
                            flag = true;

                            break;
                        }
                    }
                    if (!flag)
                    {
                        newedge = new EdgeType(trt1.Result[i].Ip, trt1.Result[i].Time);
                        edges[trt1.Result[i - 1].Ip].Add(newedge);
                    }

                }
                else
                {
                    List<EdgeType> temp_list = new List<EdgeType>();
                    newedge = new EdgeType(trt1.Result[i].Ip, trt1.Result[i].Time);
                    temp_list.Add(newedge);
                    edges.Add(trt1.Result[i - 1].Ip, temp_list);

                    addnodeDatabase(trt1.Result[i - 1].Ip, node_count++ - 1);

                }

                if (!edges.ContainsKey(trt1.Result[i].Ip))
                {
                    List<EdgeType> temp_list = new List<EdgeType>();
                    edges.Add(trt1.Result[i].Ip, temp_list);
                    addnodeDatabase(trt1.Result[i].Ip, /*node_count++ - 1*/ -1);
                }

                if (newedge != null)
                {
                    addlinkDatabase(trt1.Result[i - 1].Ip, newedge);
                }
            }
        }
    }

    
}
