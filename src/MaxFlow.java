import java.util.ArrayList;

class MaxFlow{
    ArrayList<Arc>[] adj_list;  //la structure de donnees utilisees pour representer le graphe. Chaque noeud a une liste d'arcs sortants
    int num_nodes;              //le nombre de noeuds dans le graphe
    Noeud S;             
    Noeud T;
    
    public MaxFlow(int num_nodes){
        // Initilaiser le graphe
        this.num_nodes = num_nodes;
        this.adj_list = new ArrayList[num_nodes];
        for(int i = 0; i < num_nodes; i++){
            this.adj_list[i] = new ArrayList<Arc>();
        }
    }

    void add_arc(Arc arc){
        Arc reverse_arc = new Arc(arc.N2, arc.N1, 0, 0); // on cree l'arc inverse ayant une capacite de 0 et un cout de 0
        arc.reverse_arc = reverse_arc; // on lie l'arc direct a son arc inverse
        reverse_arc.reverse_arc = arc; // on lie l'arc inverse a son arc direct
        this.adj_list[arc.N1.name].add(arc); // on ajoute l'arc a la liste d'adjacence du noeud de depart
        this.adj_list[arc.N2.name].add(reverse_arc); // on ajoute l'arc inverse a la liste d'adjacence du noeud d'arrive
    }


    boolean dfs(Noeud node, ArrayList<Arc> path, boolean[] visited){
        /*
        cette fonction sert a trouver un chemin augmentant de S a T en faisant un dfs
        */
        if (node == this.T) {   // si on atteint le noeud de destination, on a trouve un chemin augmentant 
            return true;
        }

        visited[node.name] = true;
        for(Arc arc : this.adj_list[node.name]){
            if(!visited[arc.N2.name] && arc.get_residual_capacity() > 0){ // verifier le noeud adjacent n'est pas visite et on peut encore envoyer le flow
                path.add(arc);
                visited[arc.N2.name] = true; // marquer comme visite
                boolean result = dfs(arc.N2, path, visited); // continuer le dfs a partir du noeud adjacent 
                if (result) {
                    return true;
                }
                else { // backtrack si le chemin ne mene pas la destination
                    visited[arc.N2.name] = false; 
                    path.remove(path.size() - 1);
                }
                
            }
        }
        return false;
    }

    void dfs_reachable(Noeud node, boolean[] visited){
        /*
        cette fonction sert a trouver tous les noeuds atteignables depuis un noeud donne
        */       
        visited[node.name] = true;
        for(Arc arc : this.adj_list[node.name]){
            if(!visited[arc.N2.name] && arc.get_residual_capacity() > 0){   // verifier l'autre noeud n'est pas visite et on peut encore envoyer le flow
                dfs_reachable(arc.N2, visited);
            }
        }
    }

    int ford_fulkerson(){
        /*
        cette fonction implement l'algorithme Ford-Fulkerson pour trouver le flow maximum de S a T
         */
        int max_flow = 0;
        ArrayList<Arc> path = new ArrayList<>();
        boolean[] visited = new boolean[this.num_nodes];
        while(dfs(this.S,path,visited)){    //tant qu'on trouve un chemin augmentant

            //trouver le max flow qu'On peut envoyer sur ce chemin
            int min_capacity = Integer.MAX_VALUE;
            for(Arc arc : path){
                min_capacity = Math.min(min_capacity, arc.get_residual_capacity());
            }
            max_flow += min_capacity;

            //augmenter le flow sur les arcs se trouvant dans le chemin et diminuer le flow sur les arcs inverses
            for(Arc arc : path){
                arc.augmenter_flow(min_capacity);
                arc.reverse_arc.diminuer_flow(min_capacity);
            }
            path.clear();
            visited = new boolean[this.num_nodes];
        }
        return max_flow;
    }

    ArrayList<Arc> min_cut(){
        ArrayList<Arc> cut = new ArrayList<>();
        boolean[] visited = new boolean[this.num_nodes];
        dfs_reachable(this.S, visited); // trouver tous les noeuds qu'on peut atteindre depuis S dans le graphe residuel
        
        for(int i = 0; i < this.num_nodes; i++){
            if(visited[i]){
                for(Arc arc : this.adj_list[i]){
                    if(!visited[arc.N2.name] && arc.get_capacity() > 0){ // si on ne peut pas atteindre le neoud adjacent et l'arc a une capacite positive, alors cet arc fait partie du min cut
                        cut.add(arc);
                    }
                }
            }
        }
        return cut;
    }

    void write_graphviz(String filename) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph G{\n");
        sb.append("  graph [nodesep=\"0.3\", ranksep=\"0.3\",fontsize=12]\n");
        sb.append("  node [shape=circle,fixedsize=true,width=.3,height=.3,fontsize=12]\n");
        sb.append("  edge [arrowsize=0.6]\n\n");

        for(int i = 0; i < this.num_nodes; i++){
            for(Arc arc : this.adj_list[i]){
                if(arc.max_capacity > 0){
                    sb.append("  " + arc.N1.name + " -> " + arc.N2.name + 
                    " [label = <<font color=\"green\">" + arc.max_capacity + 
                    "</font>/<font color=\"blue\">" + arc.flow +
                    "</font>/<font color=\"red\">" + arc.cost + "</font>>]\n");
                }
            }
        }
        sb.append("}");

    java.io.FileWriter fw = new java.io.FileWriter(filename);
    fw.write(sb.toString());
    fw.close();
}


}
