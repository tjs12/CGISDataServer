using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Net;
using System.Threading;

namespace TracerouteProbe
{
    class Program
    {
        static void Main(string[] args)
        {
            /*Traceroute tr = new Traceroute();
            for (int j = 0; j <= 3; j++)
            {
                tr.Trace(IPAddress.Parse("119.75.217.109"));
                foreach (IPNode i in tr.Result)
                {
                    Console.WriteLine(i.ToString());
                }
                Console.WriteLine();
            }
            Console.Read();*/ //test
			

            Probe p = new Probe();
            
			for (int i = 1; i < 256; i+=1)
            {
                for (int j = 1; j < 256; j+=1)
                {
					p.targets.Add(IPAddress.Parse("59.66." + i.ToString() + "." + j.ToString()));
					p.targets.Add(IPAddress.Parse("101.5." + i.ToString() + "." + j.ToString()));
				}
			}
			
			/*List<Probe> ps = new List<Probe>();
            for (int j = 0; j < 256; j++) ps.Add( new Probe());
            for (int i = 1; i < 256; i+=1)
            {
                for (int j = 1; j < 256; j+=1)
                {
                    ps[j-1].targets.Add(IPAddress.Parse("59.66." + i.ToString() + "." + j.ToString()));
                    ps[j-1].targets.Add(IPAddress.Parse("101.5." + i.ToString() + "." + j.ToString()));
                }
            }*/ //multi-threading
			
			
            //p.targets.Add(IPAddress.Parse("59.66.0.0"));
            
            
            p.doProbe();
			
            /*for (int i = 0; i < 256*256/128; i++)//p.targets.Count
            {
                Thread[] thrs = new Thread[128];
                for (int j = 0; j < 128; j++)
                {
                    thrs[j] = new Thread(ps[j].doProbeOne);
                    thrs[j].Start(i * 128 + j);
                }

                for (int j = 0; j < 128; j++)
                {
                    thrs[j].Join();
                    Console.WriteLine("    Join " + j.ToString());
                }
            }*/ //multi-threading

            
			Console.WriteLine(p.edges.ToString());
            Console.Read();
        }
    }
}
