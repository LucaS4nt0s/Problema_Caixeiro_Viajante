package trabalho;

import java.util.ArrayList;

import criarGrafo.MatrizDeAdjacencia;
import grafos.FileManager;
import grafos.Grafo;
import grafos.Vertice;
import java.lang.reflect.Array;
import java.util.Random;

public class AlgortimoGenetico {

    private final String caminhoDoArquivo = "trabalho/Algoritmos/CarregarGrafo/Grafo.txt";
    private Grafo grafo;
    private final int tamanhoDaPopulacao = 10;

    public AlgortimoGenetico() {
        try {
            this.grafo = carregarGrafo();
        } catch (Exception e) {
            System.out.println("Erro ao carregar o grafo: " + e.getMessage());
        }
    }

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

    public void algoritmoGenetico(){
        Vertice[][] populacao = new Vertice[this.tamanhoDaPopulacao][this.grafo.numeroDeVertices()];
        Boolean[] jaInserido = new Boolean[this.grafo.numeroDeVertices()];

        for (int i = 0; i < this.tamanhoDaPopulacao; i++){
            for (int j = 0; j < this.grafo.numeroDeVertices(); j++){
                if(j == 0){
                    Random rand = new Random();
                    int randomIndex = rand.nextInt(this.grafo.numeroDeVertices());
                    populacao[i][j] = new Vertice(randomIndex);
                    jaInserido[randomIndex] = true;
                }else{
                    
                }

            }
        }
    }

    public void gerarPopulacaoInicial(){

    }
    
    public void mutacaoInsercao(){

    }

    public void mutacaoMistura(){

    }

    public void mutacaoTroca(){

    }

    public void mutacaoInversao(){

    }
}
