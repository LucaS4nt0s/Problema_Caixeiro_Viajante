package trabalho;

import criarGrafo.MatrizDeAdjacencia;
import grafos.Aresta;
import grafos.FileManager;
import grafos.Grafo;
import grafos.Vertice;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AlgoritmoOtimo {
    private MatrizDeAdjacencia grafo; // Grafo representado como uma matriz de adjacência
    private final String caminhoDoArquivo = "trabalho/Algoritmos/CarregarGrafo/Grafo.txt"; // Caminho do arquivo que contém a representação do grafo
    private double melhorCusto; // Melhor custo encontrado até o momento
    private Vertice[] melhorCaminho; // Melhor caminho encontrado até o momento

    private double[][] cachePesos; // Cache para armazenar os pesos das arestas entre os vértices para facilitar o acesso
    private List<Aresta> todasArestasOrdenadas; // Lista de todas as arestas do grafo ordenadas por peso para facilitar Kruskal
    private int[][] vizinhosOrdenados; // Vizinhos de cada vértice ordenados por peso para facilitar buscas

    private class UnionFind{ // Estrutura de dados Union-Find para Kruskal
        private int[] pai; // Array que armazena o pai de cada elemento

        public UnionFind(int tamanho){ // Construtor que inicializa o array pai
            this.pai = new int[tamanho]; // cria o array pai com o tamanho especificado
            for(int i = 0; i < tamanho; i++){ // inicializa cada elemento para ser seu próprio pai
                this.pai[i] = i; // cada elemento é seu próprio pai no início
            }
        }

        public int find(int elemento){ // Método para encontrar o representante (raiz) do conjunto que contém o elemento
            if(this.pai[elemento] != elemento){ // se o elemento não é seu próprio pai
                this.pai[elemento] = find(this.pai[elemento]); // aplica compressão de caminho recursivamente
            }
            return this.pai[elemento]; // retorna o representante do conjunto
        }

        public boolean union(int elemento1, int elemento2){ // Método para unir dois conjuntos
            int raiz1 = find(elemento1); // encontra o representante do conjunto do primeiro elemento
            int raiz2 = find(elemento2); // encontra o representante do conjunto do segundo elemento

            if(raiz1 != raiz2){ // se os representantes são diferentes, os conjuntos são diferentes
                this.pai[raiz1] = raiz2; // une os dois conjuntos
                return true; // retorna true indicando que a união foi realizada
            }

            return false; // retorna false indicando que os elementos já estavam no mesmo conjunto
        }
    }

    public AlgoritmoOtimo() {
        try {
            this.grafo = carregarGrafo(); // carrega o grafo a partir do arquivo
            inicializarEstruturasOtimizadas(); // inicializa as estruturas de dados otimizadas
        } catch (Exception e) {
            System.out.println("Erro ao carregar o grafo: " + e.getMessage());
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

    private void inicializarEstruturasOtimizadas() { // inicializa as estruturas de dados otimizadas para o algoritmo
        int n = this.grafo.numeroDeVertices(); // obtém o número de vértices do grafo
        this.cachePesos = new double[n][n]; // cria a matriz de cache de pesos
        this.todasArestasOrdenadas = new ArrayList<>(); // cria a lista de todas as arestas ordenadas
        this.vizinhosOrdenados = new int[n][]; // cria o array de vizinhos ordenados

        for (int i = 0; i < n; i++) { // percorre os vértices do grafo
            for (int j = 0; j < n; j++) { // percorre os vértices do grafo
                if (i == j) { // se for o mesmo vértice 
                    this.cachePesos[i][j] = Double.MAX_VALUE; // define peso como infinito pois não ajuda na solução
                } else {
                    try {
                        Vertice v1 = grafo.vertices().get(i); // obtém o vértice de origem
                        Vertice v2 = grafo.vertices().get(j); // obtém o vértice de destino

                        double peso = this.grafo.arestasEntre(v1, v2).get(0).peso(); // obtém o peso da aresta entre os dois vértices
                        this.cachePesos[i][j] = peso; // armazena o peso na matriz de cache

                        this.todasArestasOrdenadas.add(new Aresta(v1, v2, peso)); // adiciona a aresta à lista de todas as arestas ordenadas
                    } catch (Exception e) {
                        this.cachePesos[i][j] = Double.MAX_VALUE; // se não houver aresta, define peso como infinito
                    }
                }
            }
        }

        Collections.sort(this.todasArestasOrdenadas, (a1, a2) -> Double.compare(a1.peso(), a2.peso())); // ordena todas as arestas pelo peso
        
        for(int i = 0; i < n; i++) {
            List<Integer> vizinhos = new ArrayList<>(); // lista para armazenar os vizinhos do vértice i
            for(int j = 0; j < n; j++) { // percorre os vértices do grafo
                if(i != j && this.cachePesos[i][j] < Double.MAX_VALUE) { // se for um vizinho (não é o mesmo vértice e há aresta)
                    vizinhos.add(j); // adiciona o vizinho à lista
                }
            }

            final int origem = i; // variável final para uso no lambda
            vizinhos.sort((v1, v2) -> Double.compare(this.cachePesos[origem][v1], this.cachePesos[origem][v2])); // ordena os vizinhos pelo peso da aresta
            this.vizinhosOrdenados[i] = new int[vizinhos.size()]; // inicializa o array de vizinhos ordenados
            for (int k = 0; k < vizinhos.size(); k++) { // percorre a lista de vizinhos
                this.vizinhosOrdenados[i][k] = vizinhos.get(k); // armazena o vizinho ordenado no array
            }
        }
    }

    private Vertice definirVerticeInicial() { // define o vértice inicial com base no menor grau de saída
        ArrayList<Vertice> vertices = this.grafo.vertices(); // obtém a lista de vértices do grafo
        int menorGrau = Integer.MAX_VALUE; // inicializa o menor grau com o valor máximo possível
        Vertice verticeInicial = null; // inicializa o vértice inicial como nulo

        for (Vertice v : vertices) { // percorre os vértices do grafo
            try {
                if (this.grafo.grauDeSaidaDoVertice(v) < menorGrau) { // se o grau de saída do vértice for menor que o menor grau atual
                    menorGrau = this.grafo.grauDeSaidaDoVertice(v); // atualiza o menor grau
                    verticeInicial = v; // define o vértice inicial como o vértice atual
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        return verticeInicial; 
    }

    private double calcularCustoAGM(boolean[] visitados, int verticesParaConectar) { // calcula o custo da Árvore Geradora Mínima (AGM) usando Kruskal para os vértices não visitados
        if (verticesParaConectar <= 1) { // se houver 1 ou menos vértices para conectar, o custo é 0
            return 0;
        }

        UnionFind uf = new UnionFind(this.grafo.numeroDeVertices()); // inicializa a estrutura Union-Find
        double custoAGM = 0; // inicializa o custo da AGM
        int arestasAdicionadas = 0; // inicializa o contador de arestas adicionadas

        for (Aresta aresta : this.todasArestasOrdenadas) { // percorre todas as arestas ordenadas
            if (!visitados[aresta.origem().id()] && !visitados[aresta.destino().id()]) { // se ambos os vértices da aresta não foram visitados
                if (uf.union(aresta.origem().id(), aresta.destino().id())) { // tenta unir os dois conjuntos
                    custoAGM += aresta.peso(); // adiciona o peso da aresta ao custo da AGM
                    arestasAdicionadas++; // incrementa o contador de arestas adicionadas
    
                    if (arestasAdicionadas == verticesParaConectar - 1) { // se o número de arestas adicionadas for igual ao número de vértices para conectar menos 1
                        break; // sai do loop pois a AGM está completa
                    }
                }
            }
        }
        return custoAGM;
    }

    private double calcularCustoConexao(int idAtual, int idInicio, boolean[] visitados) { // calcula o custo mínimo de conexão do vértice atual para os vértices não visitados e do retorno ao vértice inicial
        double menorIda = Double.MAX_VALUE; // inicializa o menor custo de ida como infinito
        double menorVolta = Double.MAX_VALUE; // inicializa o menor custo de volta como infinito
        int n = this.grafo.numeroDeVertices(); // obtém o número de vértices do grafo

        for(int i = 0; i < n; i++) { // percorre os vértices do grafo
            if(!visitados[i]) { // se o vértice não foi visitado
                double pesoIda = this.cachePesos[idAtual][i]; // obtém o peso da aresta do vértice atual para o vértice i
                if(pesoIda < menorIda) { // se o peso da ida for menor que o menor custo de ida atual
                    menorIda = pesoIda; // atualiza o menor custo de ida
                }

                double pesoVolta = this.cachePesos[i][idInicio]; // obtém o peso da aresta do vértice i para o vértice inicial
                if(pesoVolta < menorVolta) { // se o peso da volta for menor que o menor custo de volta atual
                    menorVolta = pesoVolta; // atualiza o menor custo de volta
                }
            }
        }

        if(menorIda == Double.MAX_VALUE || menorVolta == Double.MAX_VALUE) { // se não houver conexão possível
            return Double.MAX_VALUE; // retorna infinito
        }

        return menorIda + menorVolta; // retorna a soma do menor custo de ida e volta
    }
    
    private void calcularLimiteInicial(Vertice verticeInicial){ // calcula o limite inicial usando o Algoritmo Genético
        AlgortimoGenetico ag = new AlgortimoGenetico(); // cria uma instância do Algoritmo Genético
        Vertice[] caminhoAG = ag.algoritmoGenetico(); // executa o Algoritmo Genético para obter um caminho inicial

        double custoTotalAG = 0; // inicializa o custo total do caminho obtido pelo Algoritmo Genético
        if(caminhoAG != null && caminhoAG.length > 0){ // se o caminho obtido não for nulo e tiver vértices
            for(int i = 0; i < caminhoAG.length -1; i++){ // percorre o caminho obtido pelo Algoritmo Genético
                custoTotalAG += this.cachePesos[caminhoAG[i].id()][caminhoAG[i+1].id()]; // soma os pesos das arestas do caminho
            }
            custoTotalAG += this.cachePesos[caminhoAG[caminhoAG.length -1].id()][verticeInicial.id()]; // adiciona o custo de retorno ao vértice inicial

            this.melhorCusto = custoTotalAG; // define o melhor custo como o custo total do caminho do Algoritmo Genético
            this.melhorCaminho = caminhoAG.clone(); // define o melhor caminho como o caminho obtido pelo Algoritmo Genético
        }else{
            this.melhorCusto = Double.MAX_VALUE; // se o caminho for nulo, define o melhor custo como infinito
        }
    }

    public double getMelhorCusto(Grafo grafo) { // método principal para obter o melhor custo do PCV usando Branch and Bound com AGM Kruskal como lower bound e AG como Upper bound
        Vertice verticeInicial = definirVerticeInicial(); // define o vértice inicial
        this.melhorCaminho = new Vertice[this.grafo.numeroDeVertices()]; // inicializa o array do melhor caminho
        
        calcularLimiteInicial(verticeInicial); // calcula o limite inicial usando o Algoritmo Genético
        System.out.println("Limite Inicial (AG): " + this.melhorCusto); // exibe o limite inicial encontrado pelo Algoritmo Genético

        Vertice[] caminhoAtual = new Vertice[this.grafo.numeroDeVertices()]; // array para armazenar o caminho atual durante a busca
        boolean[] jaVisitado = new boolean[this.grafo.numeroDeVertices()]; // array para marcar os vértices já visitados

        caminhoAtual[0] = verticeInicial; // define o primeiro vértice do caminho atual como o vértice inicial
        jaVisitado[verticeInicial.id()] = true; // marca o vértice inicial como visitado

        calcularAdjacentes(verticeInicial.id(), caminhoAtual, 0.0, jaVisitado, 1); // inicia a busca recursiva para calcular os caminhos adjacentes
 
        System.out.println("Melhor Caminho (Kruskal Branch and Bound): "); // exibe o melhor caminho encontrado
        for (Vertice v : this.melhorCaminho) { // percorre os vértices do melhor caminho
            System.out.print(v.id() + " => "); // exibe o id do vértice
        }

        System.out.println(verticeInicial.id()); // exibe o retorno ao vértice inicial

        return this.melhorCusto; // retorna o melhor custo encontrado
    }

    private void calcularAdjacentes(int idAtual, Vertice[] caminhoAtual, double custoAtual, boolean[] jaVisitado, int verticeVisitadosCount) { // método recursivo para calcular os caminhos adjacentes usando Branch and Bound
        if(custoAtual >= this.melhorCusto) { // se o custo atual for maior ou igual ao melhor custo encontrado, poda o ramo
            return;
        }

        int n = this.grafo.numeroDeVertices(); // obtém o número de vértices do grafo

        if(verticeVisitadosCount == n) { // se todos os vértices foram visitados
            double pesoRetorno = this.cachePesos[idAtual][caminhoAtual[0].id()]; // obtém o peso da aresta de retorno ao vértice inicial

            if(pesoRetorno < Double.MAX_VALUE){ // se houver uma aresta de retorno válida
                double custoTotal = custoAtual + pesoRetorno; // calcula o custo total do ciclo completo

                if(custoTotal < this.melhorCusto) { // se o custo total for menor que o melhor custo encontrado
                    this.melhorCusto = custoTotal; // atualiza o melhor custo
                    System.arraycopy(caminhoAtual, 0, this.melhorCaminho, 0, n); // atualiza o melhor caminho
                    System.out.println("Novo Melhor Custo encontrado: " + this.melhorCusto); // exibe o novo melhor custo encontrado
                }
            }
            return;
        }

        int verticesRestantes = n - verticeVisitadosCount; // calcula o número de vértices restantes para visitar
        double custoAGM = calcularCustoAGM(jaVisitado, verticesRestantes); // calcula o custo da AGM para os vértices não visitados
        double custoConexao = calcularCustoConexao(idAtual, caminhoAtual[0].id(), jaVisitado); // calcula o custo mínimo de conexão para os vértices não visitados e o retorno ao início

        if(custoAtual + custoAGM + custoConexao >= this.melhorCusto) { // se o custo estimado for maior ou igual ao melhor custo encontrado, poda o ramo
            return;
        }

        int[] vizinhos = this.vizinhosOrdenados[idAtual]; // obtém os vizinhos ordenados do vértice atual

        for(int proximoId : vizinhos) { // percorre os vizinhos do vértice atual
            if(!jaVisitado[proximoId]) { // se o vizinho ainda não foi visitado
                double novoPeso = this.cachePesos[idAtual][proximoId]; // obtém o peso da aresta para o vizinho

                jaVisitado[proximoId] = true; // marca o vizinho como visitado
                caminhoAtual[verticeVisitadosCount] = this.grafo.vertices().get(proximoId); // adiciona o vizinho ao caminho atual
                calcularAdjacentes(proximoId, caminhoAtual, custoAtual + novoPeso, jaVisitado, verticeVisitadosCount + 1); // chama recursivamente para o próximo vértice

                jaVisitado[proximoId] = false; // desmarca o vizinho para permitir outras permutações
            }
        }
    }

    public MatrizDeAdjacencia getGrafo() { // método para obter o grafo para passar como parâmetro na main
        return this.grafo;
    }
}
