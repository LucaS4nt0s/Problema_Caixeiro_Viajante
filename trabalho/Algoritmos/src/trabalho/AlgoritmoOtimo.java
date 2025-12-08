package trabalho;

import grafos.FileManager;
import grafos.Grafo;
import criarGrafo.MatrizDeAdjacencia;
import grafos.Vertice;
import java.util.ArrayList;
import java.util.Collection;
import grafos.Aresta;


public class AlgoritmoOtimo {
    private MatrizDeAdjacencia grafo;
    private final String caminhoDoArquivo = "trabalho/Algoritmos/CarregarGrafo/Grafo.txt";
    private double melhorCusto;  
    private Vertice[] melhorCaminho;

    public AlgoritmoOtimo() {
        try {
            this.grafo = carregarGrafo();
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

    private Vertice definirVerticeInicial() {
        ArrayList<Vertice> vertices = this.grafo.vertices();
        int menorGrau = Integer.MAX_VALUE;
        Vertice verticeInicial = null;

        for (Vertice v : vertices) {
            try {
                if (this.grafo.grauDeSaidaDoVertice(v) < menorGrau) {
                    menorGrau = this.grafo.grauDeSaidaDoVertice(v);
                    verticeInicial = v;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        return verticeInicial; 
    }

    private double calcularCustoAGM(ArrayList<Vertice> caminhoParcial) {
        if(caminhoParcial.isEmpty() || caminhoParcial.size() == 1) {
            return 0;
        }
        
        boolean[] fazParteDoCaminho = new boolean[this.grafo.numeroDeVertices()];
        for(Vertice v : caminhoParcial){
            fazParteDoCaminho[v.id()] = true;
        }

        double[] menorPesoParaChegar = new double[this.grafo.numeroDeVertices()];
        boolean[] incluidoNaAGM = new boolean[this.grafo.numeroDeVertices()];

        for(Vertice v : caminhoParcial){
            menorPesoParaChegar[v.id()] = Double.MAX_VALUE;
        }

        Vertice primeiro = caminhoParcial.get(0);
        menorPesoParaChegar[primeiro.id()] = 0;

        double custoAGM = 0;
        int totalVertices = caminhoParcial.size();

        for(int i = 0; i < totalVertices; i++) {
            int u = -1;
            double minValor = Double.MAX_VALUE;
            
            for(Vertice v : caminhoParcial){
                if(!incluidoNaAGM[v.id()] && menorPesoParaChegar[v.id()] < minValor){
                    minValor = menorPesoParaChegar[v.id()];
                    u = v.id();
                }
            }

            if(u == -1 || minValor == Double.MAX_VALUE) {
                break;
            }

            incluidoNaAGM[u] = true;
            custoAGM += minValor;

            try {
                Vertice verticeU = null;

                for(Vertice v : caminhoParcial){
                    if(v.id() == u){
                        verticeU = v;
                        break;
                    }
                }

                if(verticeU != null) {
                    for(Vertice v : this.grafo.adjacentesDe(verticeU)){
                        if(fazParteDoCaminho[v.id()] && !incluidoNaAGM[v.id()]){
                            double pesoAresta = this.grafo.arestasEntre(verticeU, v).get(0).peso();
                            if(pesoAresta < menorPesoParaChegar[v.id()]){
                                menorPesoParaChegar[v.id()] = pesoAresta;
                            }
                        }
                    }
                }

            } catch (Exception e) {
            }
        }
        return custoAGM;
    }

    private double calcularCustoConexao(Vertice atual, Vertice inicio, ArrayList<Vertice> caminhoParcial) {
        if(caminhoParcial.isEmpty()) {
            return 0;
        }
        
        double menorArestaIda = Double.MAX_VALUE;
        double menorArestaVolta = Double.MAX_VALUE;

        for(Vertice v : caminhoParcial){
            try {
                double pesoIda = this.grafo.arestasEntre(atual, v).get(0).peso();
                if(pesoIda < menorArestaIda){
                    menorArestaIda = pesoIda;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        for(Vertice v : caminhoParcial){
            try {
                double pesoVolta = this.grafo.arestasEntre(v, inicio).get(0).peso();
                if(pesoVolta < menorArestaVolta){
                    menorArestaVolta = pesoVolta;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        return menorArestaIda + menorArestaVolta;
    }
    
    private void calcularLimiteInicial(Grafo grafo, Vertice verticeInicial){
        try {
            double custoEstimado = 0;
            Vertice verticeAtual = verticeInicial;
            ArrayList<Vertice> visitados = new ArrayList<>();

            visitados.add(verticeAtual);

            boolean[] visitado = new boolean[grafo.numeroDeVertices()];
            visitado[verticeAtual.id()] = true;

            for(int i = 0; i < (grafo.numeroDeVertices() - 1); i++) {
                Vertice proximo = null;
                double menorPesoLocal = Double.MAX_VALUE;

                for(Vertice v : grafo.adjacentesDe(verticeAtual)){
                    if(!visitado[v.id()]){
                        double pesoAresta = grafo.arestasEntre(verticeAtual, v).get(0).peso();
                        if(pesoAresta < menorPesoLocal){
                            menorPesoLocal = pesoAresta;
                            proximo = v;
                        }
                    }
                }

                if(proximo != null){
                    custoEstimado += menorPesoLocal;
                    verticeAtual = proximo;
                    visitados.add(verticeAtual);
                    visitado[verticeAtual.id()] = true;
                }
            }

            custoEstimado += grafo.arestasEntre(verticeAtual, verticeInicial).get(0).peso();

            this.melhorCusto = custoEstimado;

            for(int i = 0; i < visitados.size(); i++) {
                this.melhorCaminho[i] = visitados.get(i);
            }

            System.out.println("Estimativa Inicial (Guloso): " + this.melhorCusto);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public double getMelhorCusto(Grafo grafo) {
        Vertice verticeInicial = definirVerticeInicial();
        this.melhorCusto = Double.MAX_VALUE;
        this.melhorCaminho = new Vertice[this.grafo.numeroDeVertices()];
        calcularLimiteInicial(grafo, verticeInicial);
        Vertice[] caminhoAtual = new Vertice[this.grafo.numeroDeVertices()];
        Boolean[] jaVisitado = new Boolean[this.grafo.numeroDeVertices()];

        for (int i = 0; i < jaVisitado.length; i++) {
            jaVisitado[i] = false;
        }

        caminhoAtual[0] = verticeInicial; 
        jaVisitado[verticeInicial.id()] = true;

        calcularAdjacentes(grafo, verticeInicial, caminhoAtual, 0, jaVisitado, 1);

        System.out.println("Melhor Caminho: ");
        for (Vertice v : this.melhorCaminho) {
            System.out.print(v.id() + " => ");
        }

        System.out.println(verticeInicial.id());

        return this.melhorCusto;
    }

    private void calcularAdjacentes(Grafo grafo, Vertice verticeAtual, Vertice[] caminhoAtual, double custoAtual, Boolean[] jaVisitado, int indice) {
        
        ArrayList<Vertice> caminhoParcial = new ArrayList<>();
        for (Vertice v : grafo.vertices()) {
            if (!jaVisitado[v.id()]) {
                caminhoParcial.add(v);
            }
        }
        
        if(!caminhoParcial.isEmpty()) {
            double custoAGM = calcularCustoAGM(caminhoParcial);
            double custoConexao = calcularCustoConexao(verticeAtual, caminhoAtual[0], caminhoParcial);
            double custoEstimado = custoAtual + custoAGM + custoConexao;

            if(custoEstimado >= this.melhorCusto) {
                return;
            }
        } else{
            try {
                double pesoRetorno = this.grafo.arestasEntre(verticeAtual, caminhoAtual[0]).get(0).peso();
                if (custoAtual + pesoRetorno >= this.melhorCusto) {
                    return;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return;
            }
        }

        if(indice == grafo.numeroDeVertices()) {
            try {
                Aresta arestaRetorno = this.grafo.arestasEntre(verticeAtual, caminhoAtual[0]).get(0);
                double custoTotal = custoAtual + arestaRetorno.peso();

                if(custoTotal < this.melhorCusto) {
                    this.melhorCusto = custoTotal;
                    System.arraycopy(caminhoAtual, 0, this.melhorCaminho, 0, caminhoAtual.length);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return;
        }

        try {
            ArrayList<Vertice> adjacentes = grafo.adjacentesDe(verticeAtual);
            adjacentes.sort((v1, v2) -> {
            double p1 = Double.MAX_VALUE;
            try {
                p1 = grafo.arestasEntre(verticeAtual, v1).get(0).peso();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            double p2 = Double.MAX_VALUE;
            try {
                p2 = grafo.arestasEntre(verticeAtual, v2).get(0).peso();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return Double.compare(p1, p2);
            });
            for(Vertice v : adjacentes) {
                if(jaVisitado[v.id()] == false) {
                    double pesoAresta = this.grafo.arestasEntre(verticeAtual, v).get(0).peso();

                    jaVisitado[v.id()] = true;
                    caminhoAtual[indice] = v;

                    calcularAdjacentes(grafo, v, caminhoAtual, custoAtual + pesoAresta, jaVisitado, indice + 1);

                    jaVisitado[v.id()] = false;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public MatrizDeAdjacencia getGrafo() {
        return this.grafo;
    }

}
