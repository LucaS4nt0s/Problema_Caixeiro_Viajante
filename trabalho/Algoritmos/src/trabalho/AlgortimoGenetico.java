package trabalho;

import java.util.ArrayList;

import criarGrafo.MatrizDeAdjacencia;
import grafos.FileManager;
import grafos.Vertice;
import java.util.Arrays;

public class AlgortimoGenetico {

    private final String caminhoDoArquivo = "trabalho/Algoritmos/CarregarGrafo/Grafo.txt";
    private MatrizDeAdjacencia grafo;
    private final int tamanhoDaPopulacao = 200;
    Vertice[][] populacao = new Vertice[this.tamanhoDaPopulacao][grafo.numeroDeVertices()];

    public AlgortimoGenetico() {
        try {
            this.grafo = carregarGrafo();
        } catch (Exception e) {
            System.out.println("Erro ao carregar o grafo: " + e.getMessage());
        }

        ArrayList<Vertice> vertices = grafo.vertices();

        java.util.Collections.shuffle(vertices);

        for(int i = 0; i < this.tamanhoDaPopulacao; i++){
            for(int j = 0; j < grafo.numeroDeVertices(); j++){
                this.populacao[i][j] = vertices.get(j);
            }
            java.util.Collections.shuffle(vertices);
        }
    }

    private MatrizDeAdjacencia carregarGrafo() throws Exception {
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
       
    }

    private void populacaoInicial(){
        ArrayList<Vertice> vertices = grafo.vertices(); // cria uma lista com os vértices do grafo
        Vertice inicial = vertices.get(0); // define o vértice inicial
        vertices.remove(0); // remove o vértice inicial da lista
        java.util.Collections.shuffle(vertices); // embaralha a lista de vértices

        for(int i = 0; i < this.tamanhoDaPopulacao; i++){ // para cada indivíduo da população
            for(int j = 0; j < grafo.numeroDeVertices(); j++){ // para cada posição do indivíduo
                if(j==0){ // se for a primeira posição, define o vértice inicial
                    this.populacao[i][j] = inicial; 
                } else{ // caso contrário, preenche com os vértices embaralhados
                    this.populacao[i][j] = vertices.get(j-1);
                }
            }
            java.util.Collections.shuffle(vertices); // embaralha a lista de vértices novamente para o próximo indivíduo
        }
    }

    private double[] avaliarPopulacao(Vertice[][] populacao){
        double fitness[] = new double[this.tamanhoDaPopulacao]; // vetor para armazenar o custo de cada indivíduo

        for (int i = 0; i < this.tamanhoDaPopulacao; i++){ // para cada indivíduo da população
            double custoCaminho = 0; // inicializa (reinicializa) o custo do caminho

            for (int j = 0; j < grafo.numeroDeVertices(); j++){ // para cada vértice do indivíduo
                try { // tenta obter o peso da aresta entre o vértice atual e o próximo
                    if(j == grafo.numeroDeVertices() -1){
                        custoCaminho += grafo.arestasEntre(populacao[i][populacao[i].length -1], populacao[i][0]).get(0).peso(); // adiciona o peso da aresta que fecha o ciclo
                    } else{
                        custoCaminho += grafo.arestasEntre(populacao[i][j], populacao[i][j+1]).get(0).peso(); // adiciona o peso da aresta ao custo do caminho
                    }
                } catch (Exception e) {
                    custoCaminho = Double.MAX_VALUE; // se não houver aresta, atribui custo máximo
                    break; // sai do loop interno pois ja é o peso máximo (não faz sentido continuar somando)
                }
            }

            fitness[i] = custoCaminho; // armazena o custo do caminho no vetor de fitness correspondente a posição do indivíduo
        }
        return fitness; // retorna o vetor de fitness com os custos de cada indivíduo
    }

    private double[] calculoProbabilidades(Vertice[][] populacao, double[] fitness){
        double probabilidades[] = new double[this.tamanhoDaPopulacao]; // vetor para armazenar as probabilidades de seleção
        double aptidao[] = new double[this.tamanhoDaPopulacao]; // vetor para armazenar a aptidão de cada indivíduo
        double somaFitnessInvertida = 0; // variável para armazenar a soma dos fitness
        for (int i = 0; i < this.tamanhoDaPopulacao; i++) {
            if(fitness[i] >= Double.MAX_VALUE - 1){ // para precisão
                aptidao[i] = 0.0; // se o fitness for infinito, considera como 0 de aptidão
            } else{
                aptidao[i] = 1 / (fitness[i] + 0.0001); // calcula a aptidão como o inverso do fitness (com um pequeno valor para evitar divisão por zero)
            }
            somaFitnessInvertida += 1/ aptidao[i]; // calcula a soma dos fitness
        }
        for (int i = 0; i < this.tamanhoDaPopulacao; i++) {
            probabilidades[i] = aptidao[i] / somaFitnessInvertida; // calcula a probabilidade de seleção para cada indivíduo
        }

        return probabilidades; // retorna o vetor de probabilidades
    }

    private Vertice[][] selecaoPaisRoleta(Vertice[][] populacao, double[] probabilidades){
        int n = probabilidades.length; // tamanho do vetor de probabilidades

        double acumulada[] = new double[n]; // vetor para armazenar as probabilidades acumuladas
        acumulada[0] = probabilidades[0]; // o primeiro valor acumulado é igual ao primeiro valor de probabilidade
        for (int i = 1; i < n; i++) {
            acumulada[i] = acumulada[i - 1] + probabilidades[i]; // calcula as probabilidades acumuladas
        }

        int indexPai1 = roletaBinaria(acumulada); // seleciona o índice do primeiro pai usando roleta binária
        int indexPai2 = roletaBinaria(acumulada); // seleciona o índice do segundo pai usando roleta binária

        if(indexPai1 == indexPai2){ // garante que os pais sejam diferentes
            indexPai2 = (indexPai2 + 1) % n; // se forem iguais, seleciona o próximo indivíduo como segundo pai (pega a primeira posição se for o último)
        }

        return new Vertice[][] {populacao[indexPai1], populacao[indexPai2]}; // retorna os dois pais selecionados
    }
    
    private int roletaBinaria(double[] acumulada){ // método de seleção por roleta usando busca binária para uso em seleção de pais
        double r = Math.random() * acumulada[acumulada.length - 1]; // gera um número aleatório entre 0 e o último valor acumulado

        int index = Arrays.binarySearch(acumulada, r); // realiza a busca binária para encontrar o índice correspondente

        if (index < 0) {
            index = Math.abs(index) - 1; // ajusta o índice se não encontrado
        }

        if (index >= acumulada.length) { 
            index = acumulada.length - 1; // garante que o índice esteja dentro dos limites
        }

        return index;
    }
    
    private Vertice[] recombinarPaisOX(Vertice[][] pais){
        Vertice[] pai1 = pais[0];
        Vertice[] pai2 = pais[1];
        int n = pai1.length; // tamanho do indivíduo (número de vértices)

        Vertice[] filho = new Vertice[n]; // cria o array para o filho

        filho[0] = pai1[0]; // mantém o vértice inicial do pai1

        return filho;
    }

    private void mutacaoInsercao(){

    }

    private void mutacaoMistura(){
    }

    private void mutacaoTroca(){

    }

    private void mutacaoInversao(){
    }
}
