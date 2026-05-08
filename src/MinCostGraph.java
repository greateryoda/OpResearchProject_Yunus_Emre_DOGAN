import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class MinCostGraph extends MaxFlow{
    public MinCostGraph(int num_nodes) throws Exception {
        super(num_nodes);
    }

    @Override
    void add_arc(Arc arc) {
        Arc reverse_arc = new Arc(arc.N2, arc.N1, 0, -arc.cost); // cette fois, on cree l'arc inverse avec un cout negatif pour le min cost flow
        arc.reverse_arc = reverse_arc; 
        reverse_arc.reverse_arc = arc; 
        this.adj_list[arc.N1.name].add(arc); 
        this.adj_list[arc.N2.name].add(reverse_arc); 
    }


    void bellman_ford(Noeud S, int[] distances, Arc[] parent){
        for(int i = 0; i < this.num_nodes; i++){ // initialiser les distances a l'infini et les parents
            distances[i] = Integer.MAX_VALUE;
            parent[i] = null;
        }
        distances[S.name] = 0;

        // on trouve les distances les plus courtes de S a tous les autres noeuds
        for(int i = 0; i < this.num_nodes -1; i++){
            for(int j = 0; j < this.num_nodes; j++){
                for(Arc arc : this.adj_list[j]){
                    // si on peut encore envoyer du flow et le chemin via ce noeud est plus court, on met a jour la distance et le parent
                    if(arc.get_residual_capacity() > 0 && distances[j] != Integer.MAX_VALUE && distances[j] + arc.cost < distances[arc.N2.name]){
                        distances[arc.N2.name] = distances[j] + arc.cost;
                        parent[arc.N2.name] = arc;
                    }
                }
            }
        }
    }

    boolean negative_cycle(Noeud S, int[] distances, Arc[] parent){
        int [] temp_distances = new int[this.num_nodes];
        System.arraycopy(distances, 0, temp_distances, 0, this.num_nodes);

        // on fait une iteration de plus pour verifier s'il existe un cycle negatif. Si on peut reduire la distance d'un noeud, alors il existe un cycle negatif
        for(int j = 0; j < this.num_nodes; j++){
            for(Arc arc : this.adj_list[j]){
                if(arc.get_residual_capacity() > 0 && distances[j] != Integer.MAX_VALUE && distances[j] + arc.cost < distances[arc.N2.name]){
                    temp_distances[arc.N2.name] = temp_distances[j] + arc.cost;
                    parent[arc.N2.name] = arc;
                }
            }
        }

        // verifier si les distances ont ete reduites
        for(int i = 0; i < this.num_nodes; i++){
            if(temp_distances[i] < distances[i]){
                return true;
            }
        }
        return false;
    }

    int min_cost_flow_bellman_ford() throws Exception {
        /*
        cette fonction impelemente l'algo min cost flow en utilisant Bellman-Ford 
         */
        int total_cost = 0;
        int[] distances = new int[this.num_nodes];
        Arc[] parent = new Arc[this.num_nodes];

        while(true){
        // on trouve le chemin augmentant de cout minimum avec Bellman-Ford
            bellman_ford(this.S, distances, parent);
            
        if(negative_cycle(this.S, distances, parent)){
            // si on detecte un cycle negatif, on s'arrete car on peut envoyer des flows indefiniment pour reduire le cout total.
            // on ne peut pas trouver la solution optimale dans ce cas
            throw new Exception("Negative cycle detected!");
        }

            if (distances[this.T.name] == Integer.MAX_VALUE) {
                break; // si on ne trouve pas de chemin augmentant de S a T, on a trouve le flow de cout minimum
            }

            int min_capacity = Integer.MAX_VALUE;

            ArrayList<Arc> path = new ArrayList<>();
            Noeud current = this.T;
            while(parent[current.name] != null){ // reconstruire le chemin augmentant a partir du tableau parent
                Arc arc = parent[current.name];
                path.add(arc);
                current = arc.N1;
            }

            for(Arc arc : path){ // trouver le flow maximum qu'on peut envoyer sur ce chemin
                min_capacity = Math.min(min_capacity, arc.get_residual_capacity());
            }

            for(Arc arc : path){    // augmenter le flow sur les arcs se trouvant dans le chemin et diminuer le flow sur les arcs inverses
                arc.flow += min_capacity;
                arc.reverse_arc.flow -= min_capacity;
                total_cost += min_capacity * arc.cost;  //maj du cout total
            }
        }
        return total_cost;
    }


    int[] dijkstra(Noeud S, Arc[] parent, int[] d){
        /*
        cette fonction implemente l'algo djisktra pour trouver le chemin augmentant de cout minimum
         */

        int[] distances = new int[this.num_nodes];
        for(int i = 0; i < this.num_nodes; i++){    //initialiser les distances
            distances[i] = Integer.MAX_VALUE;
        }

        boolean[] closed = new boolean[this.num_nodes]; 
        for(int i = 0; i < this.num_nodes; i++){     //initialiser les noeuds comme ouverts
            closed[i] = false;
        }

        PriorityQueue<Noeud> open_nodes = new PriorityQueue<>(Comparator.comparingInt((Noeud node) -> distances[node.name]));   //file de priorite pour les noeuds ouverts (triee par distance)
        distances[S.name] = 0;
        open_nodes.add(S);
        while(!open_nodes.isEmpty() && !closed[this.T.name]){   //tant qu'on a des noeuds ouverts et que T n'est pas ferme
            Noeud current = open_nodes.poll();  //prendre le noeud ouvert ayant la plus petite distance
            closed[current.name] = true;
            for(Arc arc : this.adj_list[current.name]){     // pour chaque arc sortant de ce noeud
                if(!closed[arc.N2.name] && arc.get_residual_capacity() > 0){    // si le noeud adjacent n'est pas ferme et on peut encore envoyer du flow
                    int new_dist = distances[current.name] + arc.cost + d[current.name] - d[arc.N2.name];   //calculer le cout reduit du chemin via ce noeud
                    if(new_dist < distances[arc.N2.name]){  // si le chemin via ce noeud est plus court, on met a jour la distance et le parent
                        distances[arc.N2.name] = new_dist;
                        parent[arc.N2.name] = arc;
                        open_nodes.add(arc.N2);
                    }
                }
            }
        }
        return distances;   // retourner les distances trouvees 
    }  

    int min_cost_flow_dijkstra(){
        /*
        cette fonction implemente l'algo min cost flow en utilisant Dijkstra pour trouver les chemins augmentants de cout minimum
         */
        int total_cost = 0;
        Arc[] parent = new Arc[this.num_nodes];
        int[] d = new int[this.num_nodes];
        int[] distances = new int[this.num_nodes];


        bellman_ford(this.S, d, parent); // on utilise Bellman-Ford une fois au debut pour calculer les distances initiaux 


        while(true){
            
            distances = dijkstra(this.S, parent, d);
            if (parent[this.T.name] == null) {  // si on ne trouve pas de chemin augmentant de S a T, on a trouve le flow de cout minimum
                break;
            }

            for(int i = 0; i < this.num_nodes; i++){    // maj les distances reduites pour le prochain iteration de djisktra
                if(distances[i] != Integer.MAX_VALUE){
                    d[i] += distances[i];
                }
            }

            int min_capacity = Integer.MAX_VALUE;

            ArrayList<Arc> path = new ArrayList<>();
            Noeud current = this.T;
            while(parent[current.name] != null){    // reconstruire le chemin augmentant a partir du tableau parent
                Arc arc = parent[current.name];
                path.add(arc);
                current = arc.N1;
            }

            for(Arc arc : path){    // trouver le flow maximum qu'on peut envoyer sur ce chemin
                min_capacity = Math.min(min_capacity, arc.get_residual_capacity());
            }

            for(Arc arc : path){    // augmenter le flow sur les arcs se trouvant dans le chemin et diminuer le flow sur les arcs inverses
                arc.flow += min_capacity;
                arc.reverse_arc.flow -= min_capacity;
                total_cost += min_capacity * arc.cost;  // maj du cout total
            }

        parent = new Arc[this.num_nodes]; // reinitialiser le tableau parent pour le prochain iteration
        }

        return total_cost;
    }

}