using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Net;
using System.Net.NetworkInformation;
using System.Threading;
using System.Diagnostics;

namespace TracerouteProbe
{

    class IPNode
    {
        public IPAddress Ip;
        public double Time;

        public IPNode(IPAddress _ip, double _time)
        {
            Ip = _ip;
            Time = _time;
        }

        public override string ToString()
        {
            return (Ip == null ? "null" : Ip.ToString()) + " " + Time.ToString();
        }
    }


    class Traceroute
    {
        public List<IPNode> Result;

        IPAddress localIp;
        public Traceroute()
        {
            Result = new List<IPNode>();
            string strHostName = Dns.GetHostName();
            IPHostEntry ipEntry = Dns.GetHostEntry(strHostName);
            IPAddress[] addr = ipEntry.AddressList;
            /*for (int i = 0; i < addr.Length; i++)
            {
                Console.WriteLine("IP Address {0}: {1} ", i, addr[i].ToString());
            }*/
            localIp = addr[addr.Length - 1];

        }
        public void Trace(IPAddress target_ip, int max_hop = 15)
        {
            Result.Clear();
            Result.Add(new IPNode(localIp, 0.0));
            Ping ping = new Ping();
            //List<Thread> thrs
            string data = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
            byte[] buffer = Encoding.ASCII.GetBytes (data);

            double last_time = 0;

            for (int i = 1; i <= max_hop; i++)
            {
                //Thread nthr(new ThreadStart(ping.Send);
                Stopwatch sw = new Stopwatch();
                sw.Start();
                PingReply reply = ping.Send(target_ip, 500, buffer, new PingOptions(i, false));
                sw.Stop();
                if (reply.Status == IPStatus.TtlExpired || reply.Status == IPStatus.Success) 
                    Result.Add(new IPNode(reply.Address, sw.ElapsedMilliseconds / 2.0 - last_time));
                
                if (reply.Address != null && reply.Address.Equals(target_ip)) break;
            }

           
            /*foreach (IPNode i in Result)
            {
                if (i.Ip == null) continue;
                PingReply reply = ping.Send(i.Ip);
                if (reply.Status == IPStatus.Success)
                {
                    i.Time = Math.Max(reply.RoundtripTime / 2.0 - last_time, 0);
                    last_time = reply.RoundtripTime / 2.0;
                }
            }*/
            
        }
        
    }
}
