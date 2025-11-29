package criarGrafo;

import java.util.ArrayList;
import grafos.Grafo;
import grafos.Aresta;
import grafos.Vertice;

public class MatrizDeAdjacencia implements Grafo {

    private final double[][] matriz; // define a matriz de adjacência do grafo como final e privada
    private final int numVertices; // define o número de vértices do grafo como final e privado

    public MatrizDeAdjacencia(int numVertices) { // construtor que inicializa a matriz de adjacência
        this.numVertices = numVertices; // armazena o número de vértices
        this.matriz = new double[numVertices][numVertices]; // cria a matriz de adjacência com o tamanho V x V

        // Inicializa a matriz com infinito
        for (int i = 0; i < this.numVertices; i++) {
            for (int j = 0; j < this.numVertices; j++) {
                this.matriz[i][j] = Double.MAX_VALUE; // representa ausência de aresta para não interferir nos pesos
            }
        }
    }
    
    @Override
    public void adicionarAresta(Vertice origem, Vertice destino) throws Exception{
        if (origem.id() < 0 || origem.id() >= this.numVertices || destino.id() < 0 || destino.id() >= this.numVertices) { // caso os vértices não existam
            throw new Exception("Vértice não existe.");
        }
        // cria o objeto Aresta e adiciona na matriz
        Aresta aresta = new Aresta(origem, destino, 1);
        this.matriz[aresta.origem().id()][aresta.destino().id()] = 1; // adiciona a aresta com peso padrão 1
    }   

    @Override
    public void adicionarAresta(Vertice origem, Vertice destino, double peso) throws Exception{
        if (origem.id() < 0 || origem.id() >= this.numVertices || destino.id() < 0 || destino.id() >= this.numVertices) { // caso os vértices não existam
            throw new Exception("Vértice não existe.");
        }
        // cria o objeto Aresta e adiciona na matriz com o peso definido
        Aresta aresta = new Aresta(origem, destino, peso);
        this.matriz[aresta.origem().id()][aresta.destino().id()] = aresta.peso();
    }

    @Override
    public boolean existeAresta(Vertice origem, Vertice destino){
        return this.matriz[origem.id()][destino.id()] != Double.MAX_VALUE; // retorna true se a aresta existir, false caso contrário
    }

    @Override
    public int grauDoVertice(Vertice vertice) throws Exception{
        if (vertice.id() < 0 || vertice.id() >= this.numVertices) { // caso o vértice não exista
            throw new Exception("Vértice não existe.");
        }

        int grau = 0; // inicializa o grau do vértice
        for (int j = 0; j < this.numVertices; j++) { // percorre a linha e coluna correspondente ao vértice
            if (this.matriz[vertice.id()][j] != Double.MAX_VALUE && vertice.id() != j) { // se existir aresta na linha incrementa o grau
                grau++;
            }
            if (this.matriz[j][vertice.id()] != Double.MAX_VALUE) { // se existir aresta na coluna incrementa o grau
                grau++;
            }
        }
        return grau; 
    }

    public int grauDeSaidaDoVertice(Vertice vertice) throws Exception{
        if (vertice.id() < 0 || vertice.id() >= this.numVertices) { // caso o vértice não exista
            throw new Exception("Vértice não existe.");
        }

        int grau = 0; // inicializa o grau do vértice
        for (int j = 0; j < this.numVertices; j++) { // percorre a linha correspondente ao vértice
            if (this.matriz[vertice.id()][j] != Double.MAX_VALUE && vertice.id() != j) { // se existir aresta na linha incrementa o grau
                grau++;
            }
        }
        return grau; 
    }

    @Override
    public int numeroDeVertices(){
        return this.numVertices;
    }

    @Override
    public int numeroDeArestas(){
        int count = 0; // inicializa o contador de arestas
        for (int i = 0; i < this.numVertices; i++) { // percorre toda a matriz
            for (int j = 0; j < this.numVertices; j++) { 
                if (this.matriz[i][j] != Double.MAX_VALUE) { // se existir aresta incrementa o contador
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public ArrayList<Vertice> adjacentesDe(Vertice vertice) throws Exception{
        if (vertice.id() < 0 || vertice.id() >= this.numVertices) { // caso o vértice não exista
            throw new Exception("Vértice não existe.");
        }

        ArrayList<Vertice> adjacentes = new ArrayList<>(); // inicializa a lista de vértices adjacentes
        for (int j = 0; j < this.numVertices; j++) { // percorre a linha correspondente ao vértice
            if (this.matriz[vertice.id()][j] != Double.MAX_VALUE) { // se existir aresta, adiciona o vértice adjacente na lista
                adjacentes.add(new Vertice(j));
            }
        }
        return adjacentes;
    }

    @Override
    public void setarPeso(Vertice origem, Vertice destino, double peso) throws Exception{
        if (origem.id() < 0 || origem.id() >= this.numVertices || destino.id() < 0 || destino.id() >= this.numVertices) { // caso os vértices não existam
            throw new Exception("Vértice não existe.");
        }

        Aresta aresta = new Aresta(origem, destino, peso); // cria o objeto Aresta com o peso definido
        this.matriz[aresta.origem().id()][aresta.destino().id()] = aresta.peso(); // atualiza o peso da aresta na matriz
    }

    @Override
    public ArrayList<Aresta> arestasEntre(Vertice origem, Vertice destino) throws Exception{ 
        if (origem.id() < 0 || origem.id() >= this.numVertices || destino.id() < 0 || destino.id() >= this.numVertices) { // caso os vértices não existam
            throw new Exception("Vértice não existe.");
        }
        
        ArrayList<Aresta> arestas = new ArrayList<>(); // inicializa a lista de arestas entre os vértices origem e destino
        if (this.matriz[origem.id()][destino.id()] != Double.MAX_VALUE) { // se existir aresta de origem para destino
            Aresta aresta = new Aresta(origem, destino, this.matriz[origem.id()][destino.id()]); // cria o objeto Aresta com o peso definido
            arestas.add(aresta); // adiciona a aresta na lista
        }

        // if(matriz[destino.id()][origem.id()] != Double.MAX_VALUE){
        //     Aresta aresta = new Aresta(destino, origem, matriz[destino.id()][origem.id()]);
        //     arestas.add(aresta);
        // }

        return arestas;
    }
    
    @Override
    public ArrayList<Vertice> vertices(){
        ArrayList<Vertice> vertices = new ArrayList<>(); // inicializa a lista de vértices
        for (int i = 0; i < this.numVertices; i++) { // percorre o número de vértices
            vertices.add(new Vertice(i)); // adiciona o vértice na lista
        }
        return vertices; 
    }
}
