public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length != 1){
            System.out.println("Usage: java Main <graph_file>");
            return;
        }

        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(args[0]));
        String[] firstLine = br.readLine().trim().split(" ");
        int num_nodes = Integer.parseInt(firstLine[0]);
        int num_arcs = Integer.parseInt(firstLine[1]);
        int s = Integer.parseInt(firstLine[2]);
        int t = Integer.parseInt(firstLine[3]);

        int[][] arc_data = new int[num_arcs][4];
        for(int i = 0; i < num_arcs; i++){
            String[] line = br.readLine().trim().split(" ");
            arc_data[i][0] = Integer.parseInt(line[0]);
            arc_data[i][1] = Integer.parseInt(line[1]);
            arc_data[i][2] = Integer.parseInt(line[2]);
            arc_data[i][3] = Integer.parseInt(line[3]);
        }
        br.close();

        Noeud[] noeuds = new Noeud[num_nodes];
        for(int i = 0; i < num_nodes; i++) noeuds[i] = new Noeud(i);

        // Ford-Fulkerson
        MaxFlow graph1 = new MaxFlow(num_nodes);
        graph1.S = noeuds[s]; graph1.T = noeuds[t];
        for(int[] a : arc_data) graph1.add_arc(new Arc(noeuds[a[0]], noeuds[a[1]], a[2], a[3]));
        System.out.println("Max Flow (Ford-Fulkerson): " + graph1.ford_fulkerson());
        new java.io.File("out").mkdirs();
        graph1.write_graphviz("out/ford_fulkerson.gv");

        // Min Cost Flow - Bellman-Ford
        MinCostGraph graph2 = new MinCostGraph(num_nodes);
        graph2.S = noeuds[s]; graph2.T = noeuds[t];
        for(int[] a : arc_data) graph2.add_arc(new Arc(noeuds[a[0]], noeuds[a[1]], a[2], a[3]));
        System.out.println("Min Cost Flow (Bellman-Ford): " + graph2.min_cost_flow_bellman_ford());
        graph2.write_graphviz("out/min_cost_bellman.gv");

        // Min Cost Flow - Dijkstra
        MinCostGraph graph3 = new MinCostGraph(num_nodes);
        graph3.S = noeuds[s]; graph3.T = noeuds[t];
        for(int[] a : arc_data) graph3.add_arc(new Arc(noeuds[a[0]], noeuds[a[1]], a[2], a[3]));
        System.out.println("Min Cost Flow (Dijkstra): " + graph3.min_cost_flow_dijkstra());
        graph3.write_graphviz("out/min_cost_dijkstra.gv");
    }
}