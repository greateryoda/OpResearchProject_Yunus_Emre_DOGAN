class Arc{
    Arc reverse_arc;    //l'arc inverse
    Noeud N1;           //noeud de depart
    Noeud N2;           //noeud d'arrive
    int max_capacity;   //capacite arc
    int flow;           //flow actuel sur l'arc
    int cost;           //cout de l'arc. (On ne l'utilise que pour le min cost flow)


    public Arc(Noeud N1, Noeud N2, int max_capacity, int cost){
        this.N1 = N1;
        this.N2 = N2;
        this.max_capacity = max_capacity;
        this.flow = 0;
        this.cost = cost;
    }

    void diminuer_flow(int decrement){
        this.flow -= decrement;
    }

    void augmenter_flow(int increment){
        this.flow += increment;
    }

    int get_capacity(){
        return this.max_capacity;
    }

    int get_residual_capacity(){
        return this.max_capacity - this.flow;
    }

    @Override
    public String toString(){
        return "(" + N1.name + " -> " + N2.name + ", max_capacity: " + max_capacity + ", flow: " + flow + ")";
    }
}