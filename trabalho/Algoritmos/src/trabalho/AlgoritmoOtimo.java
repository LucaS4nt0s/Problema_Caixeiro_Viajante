package trabalho;

import grafos.FileManager;
import grafos.Grafo;
import criarGrafo.MatrizDeAdjacencia;
import grafos.Vertice;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class AlgoritmoOtimo {
    private MatrizDeAdjacencia grafo;
    private final String caminhoDoArquivo = "trabalho/AlgoritmosEmGrafos/CarregarGrafo/Grafo.txt";
    private double melhorCusto;  

    public Grafo carregarGrafo() throws Exception {
        FileManager fileManager = new FileManager();
        ArrayList<String> conteudo = fileManager.stringReader(this.caminhoDoArquivo); // lê o conteúdo do arquivo e armazena em uma lista de strings
        
        if (conteudo.isEmpty()) { // verifica se o arquivo está vazio
            throw new Exception("Caminho inválido ou arquivo vazio."); // lança uma exceção se o arquivo estiver vazio
        }
        
        int numeroDeVertices = Integer.parseInt(conteudo.get(0)); // o primeiro elemento da lista é o número de vértices
        int numeroDeArestas = 0; // inicializa o número de arestas
        for (int i = 1; i < conteudo.size(); i++) { // percorre o restante da lista para contar as arestas
            String[] partes = conteudo.get(i).split(" "); // divide a linha onde há espaços
            numeroDeArestas += partes.length - 1; // o número de arestas é o total de partes menos 1 (o primeiro elemento é o vértice origem)
        }

        ArrayList<Integer> destinosList = new ArrayList<>(numeroDeArestas); // lista para armazenar os vértices destino
        ArrayList<Integer> origensList = new ArrayList<>(numeroDeArestas); // lista para armazenar os vértices origem
        ArrayList<Double> pesosList = new ArrayList<>(numeroDeArestas); // lista para armazenar os pesos das arestas

        for (int j = 1; j < conteudo.size(); j++) { // percorre o conteúdo do arquivo a partir da segunda linha
            String[] partes = conteudo.get(j).split(" "); // divide a linha onde há espaços
            for (int k = 1; k < partes.length; k++) { // percorre as partes a partir do segundo elemento
                String[] subpartes = partes[k].split("-"); // divide a parte onde há hífen
                String[] pesos = subpartes[1].split(";"); // divide a parte do peso onde há ponto e vírgula
                destinosList.add(Integer.valueOf(subpartes[0])); // adiciona o vértice destino à lista
                origensList.add(Integer.valueOf(partes[0])); // adiciona o vértice origem à lista
                pesosList.add(Double.valueOf(pesos[0])); // adiciona o peso à lista
            }
        }

        this.grafo = new MatrizDeAdjacencia(numeroDeVertices); // cria o grafo como matriz de adjacência
        for (int i = 0; i < destinosList.size(); i++) {  // percorre a lista de destinos
            this.grafo.adicionarAresta(new Vertice(origensList.get(i)), new Vertice(destinosList.get(i)), pesosList.get(i)); // adiciona a aresta ao grafo com o peso definido
        }

        return this.grafo;
    }

    public Vertice definirVerticeInicial() {
        ArrayList<Vertice> vertices = this.grafo.vertices();
        int menorGrau = Integer.MAX_VALUE;
        Vertice verticeInicial = null;

        for (Vertice v : vertices) {
            try {
                if (this.grafo.grauDoVertice(v) < menorGrau) {
                    menorGrau = this.grafo.grauDoVertice(v);
                    verticeInicial = v;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        return verticeInicial; 
    }

    public double getMelhorCusto(Grafo grafo) {
        Vertice verticeInicial = definirVerticeInicial();
        Vertice[] caminhoAtual = new Vertice[this.grafo.numeroDeVertices()];
        melhorCusto = Double.MAX_VALUE;
        double custoAtual = 0;

        caminhoAtual[0] = verticeInicial;   

        custoAtual = visitarAdjacentes(grafo, verticeInicial, caminhoAtual, custoAtual);

        return melhorCusto;
    }

    private double visitarAdjacentes(Grafo grafo, Vertice vertice, Vertice[] caminhoAtual, double custoAtual) {
        if(caminhoAtual[caminhoAtual.length - 1] != null){
            if(custoAtual < melhorCusto){
                melhorCusto = custoAtual;
            }
            return melhorCusto;
        }

        return 0.0;
    }

}
