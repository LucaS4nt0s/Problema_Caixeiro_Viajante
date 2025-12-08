package trabalho;

import criarGrafo.MatrizDeAdjacencia;
import grafos.FileManager;
import grafos.Vertice;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AlgortimoGenetico {

    private final String caminhoDoArquivo = "trabalho/Algoritmos/CarregarGrafo/Grafo.txt";
    private MatrizDeAdjacencia grafo;
    private final int tamanhoDaPopulacao = 2000;
    Vertice[][] populacao;

    public AlgortimoGenetico() {
        try {
            this.grafo = carregarGrafo();
        } catch (Exception e) {
            System.out.println("Erro ao carregar o grafo: " + e.getMessage());
        }

        this.populacao = new Vertice[this.tamanhoDaPopulacao][grafo.numeroDeVertices()];

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
        int maxGeracoes = 1000; // alterar para calibrar o algoritmo (quantidade de gerações)
        double taxaDeMutacao = 0.1; // alterar para calibrar o algoritmo (0.05 = 5% de chance de mutação)
        boolean elitismo = true; // alterar para calibrar o algoritmo (se true, o melhor indivíduo de cada geração é mantido na próxima geração)
        
        populacaoInicial(); // cria a população inicial
        double[] fitness = avaliarPopulacao(this.populacao); // avalia a população inicial

        Vertice[] melhorSolucaoGlobal = null;
        double melhorCustoGlobal = Double.MAX_VALUE;
       
        int geracaoAtual = 0;

        while(geracaoAtual < maxGeracoes){
            int melhorIndice = obterIndiceMelhorIndividuo(fitness); // obtém o índice do melhor indivíduo da geração atual
            double melhorCustoAtual = fitness[melhorIndice]; // obtém o custo do melhor indivíduo da geração atual

            if(melhorCustoAtual < melhorCustoGlobal){ // se o melhor custo atual for melhor que o global
                melhorCustoGlobal = melhorCustoAtual; // atualiza o melhor custo global
                melhorSolucaoGlobal = this.populacao[melhorIndice].clone(); // atualiza a melhor solução global
                System.out.println("Geração " + geracaoAtual + ": Melhor custo = " + melhorCustoGlobal);
            }

            Vertice[][] novaPopulacao = new Vertice[this.tamanhoDaPopulacao][grafo.numeroDeVertices()]; // cria a nova população
            int indexNovaPopulacao = 0;

            if(elitismo){ // se o elitismo estiver ativado, copia o melhor indivíduo para a nova população
                novaPopulacao[0] = this.populacao[melhorIndice].clone(); // copia o melhor indivíduo para a nova população na posição 0
                indexNovaPopulacao = 1; // começa a preencher a nova população a partir do índice 1
            }

            double[] probabilidades = calculoProbabilidades(fitness); // calcula as probabilidades de seleção

            for(int i = indexNovaPopulacao; i < this.tamanhoDaPopulacao; i+=2){
                Vertice[][] pais = selecaoPaisRoleta(this.populacao, probabilidades); // seleciona os pais usando roleta
                Vertice[][] paisInvertidos = new Vertice[][] {pais[1], pais[0]}; // inverte a ordem dos pais para diversidade
 
                Vertice[] filho1 = recombinarPaisOX(pais); // gera o primeiro filho pela recombinação OX
                Vertice[] filho2 = recombinarPaisOX(paisInvertidos); // gera o segundo filho pela recombinação OX

                if(Math.random() < taxaDeMutacao){
                    filho1 = mutacaoInsercao(filho1); // aplica mutação por inserção no primeiro filho
                    filho2 = mutacaoInsercao(filho2); // aplica mutação por inserção no segundo filho
                }
                novaPopulacao[i] = filho1;
                if (i + 1 < this.tamanhoDaPopulacao) {
                    novaPopulacao[i + 1] = filho2;
                }
            }   
            this.populacao = novaPopulacao; // atualiza a população com a nova população
            fitness = avaliarPopulacao(this.populacao); // avalia a nova população
            geracaoAtual++; // incrementa a geração atual
        }

        System.out.println("\nAlgoritmo Genético finalizado após " + maxGeracoes + " gerações.");
        System.out.println("Melhor solução encontrada:");
        if(melhorSolucaoGlobal != null){
            for(Vertice v : melhorSolucaoGlobal){
                System.out.print(v.id() + " ");
            }
        }
        System.out.println("\nCusto total: " + melhorCustoGlobal);
    }

    private int obterIndiceMelhorIndividuo(double[] fitness){
        int melhorIndice = 0; // índice do melhor indivíduo
        double melhorFitness = fitness[0]; // melhor fitness inicializado com o primeiro indivíduo

        for (int i = 1; i < fitness.length; i++) { // percorre o vetor de fitness
            if (fitness[i] < melhorFitness) { // se o fitness atual for melhor (menor)
                melhorFitness = fitness[i]; // atualiza o melhor fitness
                melhorIndice = i; // atualiza o índice do melhor indivíduo
            }
        }

        return melhorIndice;
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

    private double[] calculoProbabilidades(double[] fitness){
        double probabilidades[] = new double[this.tamanhoDaPopulacao]; // vetor para armazenar as probabilidades de seleção
        double aptidao[] = new double[this.tamanhoDaPopulacao]; // vetor para armazenar a aptidão de cada indivíduo
        double somaAptidao = 0; // variável para armazenar a soma dos fitness

        for (int i = 0; i < this.tamanhoDaPopulacao; i++) {
            if(fitness[i] >= Double.MAX_VALUE - 1){ // para precisão
                aptidao[i] = 0.0; // se o fitness for infinito, considera como 0 de aptidão
            } else{
                aptidao[i] = 1.0 / (fitness[i] + 0.0001); // calcula a aptidão como o inverso do fitness (com um pequeno valor para evitar divisão por zero)
            }
            somaAptidao += aptidao[i]; // calcula a soma dos fitness
        }

        for (int i = 0; i < this.tamanhoDaPopulacao; i++) {
            if(somaAptidao == 0){
                probabilidades[i] = 0.0; // se a soma da aptidão for zero, todas as probabilidades são zero
            } else{
                probabilidades[i] = aptidao[i] / somaAptidao; // calcula a probabilidade de seleção para cada indivíduo
            }
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

        int pontoCorte1 = 1 + (int) (Math.random() * (n - 1)); // gera o primeiro ponto de corte aleatório (não pode ser o primeiro)
        int pontoCorte2 = 1 + (int) (Math.random() * (n - 1)); // gera o segundo ponto de corte aleatório (não pode ser o primeiro)

        int comeco = Math.min(pontoCorte1, pontoCorte2); // determina o início do segmento
        int fim = Math.max(pontoCorte1, pontoCorte2); // determina o fim do segmento

        Set<Vertice> verticesNoFilho = new HashSet<>(); // conjunto para rastrear os vértices já adicionados ao filho em O(1)
        verticesNoFilho.add(filho[0]); // adiciona o vértice inicial ao conjunto pois é fixo

        for (int i = comeco; i <= fim; i++) {
            filho[i] = pai1[i]; // copia o segmento do pai1 para o filho
            verticesNoFilho.add(filho[i]); // adiciona o vértice ao conjunto
        }

        int indiceFilho = 1; // começa depois do vértice inicial

        for(int i = 0; i < n; i++){
            if(indiceFilho>= comeco && indiceFilho <= fim){
                indiceFilho = fim + 1; // pula o segmento já preenchido
                if(indiceFilho >= n){
                    break; // se ultrapassar o tamanho, sai do loop
                }
            }

            Vertice verticeCandidato = pai2[i]; // obtém o vértice candidato do pai2

            if(!verticesNoFilho.contains(verticeCandidato)){ // se o vértice ainda não foi adicionado ao filho
                filho[indiceFilho] = verticeCandidato; // adiciona o vértice ao filho
                indiceFilho++; // avança para a próxima posição no filho
            }
        }

        return filho;
    }

    private Vertice[] mutacaoInsercao(Vertice[] individuo){
        int posOrigem = 1 + (int) (Math.random() * (individuo.length - 1)); // posição aleatória (não pode ser o primeiro e nem o último)
        int posDestino = 1 + (int) (Math.random() * (individuo.length - 1));

        while(posOrigem == posDestino){ // garante que as posições de origem e destino sejam diferentes
            posDestino = 1 + (int) (Math.random() * (individuo.length - 1));
        }

        Vertice verticeASerMovido = individuo[posOrigem]; // vértice a ser movido

        Vertice[] individuoAux = new Vertice[individuo.length]; // cria uma cópia do indivíduo para modificar
        System.arraycopy(individuo, 0, individuoAux, 0, individuo.length); // copia o indivíduo original para a cópia

        if(posOrigem < posDestino){ // se a posição de origem é menor que a posição de destino
            System.arraycopy(individuo, posOrigem + 1, individuoAux, posOrigem, posDestino - posOrigem); // move os elementos entre posOrigem e posDestino para a esquerda
        }else{
            System.arraycopy(individuo, posDestino, individuoAux, posDestino + 1, posOrigem - posDestino); // move os elementos entre posDestino e posOrigem para a direita
        }

        individuoAux[posDestino] = verticeASerMovido; // insere o vértice movido na nova posição
        return individuoAux;
    }

    private Vertice[] mutacaoMistura(Vertice[] individuo){
        int pos1 = 1 + (int) (Math.random() * (individuo.length - 1)); // define a posicao 1
        int pos2 = 1 + (int) (Math.random() * (individuo.length - 1)); // define a posicao 2

        Vertice[] individuoAux = new Vertice[individuo.length]; // cria uma cópia do indivíduo para modificar
        System.arraycopy(individuo, 0, individuoAux, 0, individuo.length); // copia o indivíduo original para a cópia

        while(pos1 == pos2){ // se forem iguais sorteia até ser diferente
            pos2 = 1 + (int) (Math.random() * (individuo.length - 1));
        }

        int comeco = Math.min(pos1, pos2); // define o início do segmento
        int fim = Math.max(pos1, pos2); // define o fim do segmento
        ArrayList<Vertice> segmento = new ArrayList<>(); // cria uma lista para armazenar o segmento

        for(int i = comeco; i <= fim; i++){
            segmento.add(individuoAux[i]); // adiciona os vértices ao segmento
        }

        Collections.shuffle(segmento); // embaralha o segmento
        for(int i = comeco; i <= fim; i++){
            individuoAux[i] = segmento.get(i - comeco); // insere o segmento embaralhado de volta ao vetor
        }
        return individuoAux;
    }

    private Vertice[] mutacaoTroca(Vertice[] individuo){
        int pos1 = 1 + (int) (Math.random() * (individuo.length - 1)); // define a posicao 1
        int pos2 = 1 + (int) (Math.random() * (individuo.length - 1)); // define a posicao 2

        
        Vertice[] individuoAux = new Vertice[individuo.length]; // cria uma cópia do indivíduo para modificar
        System.arraycopy(individuo, 0, individuoAux, 0, individuo.length); // copia o indivíduo original para a cópia


        while(pos1 == pos2){ // se forem iguais sorteia até ser diferente
            pos2 = 1 + (int) (Math.random() * (individuo.length - 1));
        }

        Vertice temp = individuoAux[pos1];
        individuoAux[pos1] = individuoAux[pos2];
        individuoAux[pos2] = temp;

        return individuoAux;
    }

    private Vertice[] mutacaoInversao(Vertice[] individuo){
        int pos1 = 1 + (int) (Math.random() * (individuo.length - 1)); // define a posicao 1
        int pos2 = 1 + (int) (Math.random() * (individuo.length - 1)); // define a posicao 2

        Vertice[] individuoAux = new Vertice[individuo.length]; // cria uma cópia do indivíduo para modificar
        System.arraycopy(individuo, 0, individuoAux, 0, individuo.length); // copia o indivíduo original para a cópia

        while(pos1 == pos2){ // se forem iguais sorteia até ser diferente
            pos2 = 1 + (int) (Math.random() * (individuo.length - 1));
        }

        int comeco = Math.min(pos1, pos2); // define o início do segmento
        int fim = Math.max(pos1, pos2); // define o fim do segmento

        while(comeco < fim){ // enquanto comeco for menor que fim
            Vertice temp = individuoAux[comeco]; // armazena temporariamente o vértice na posição comeco
            individuoAux[comeco] = individuoAux[fim]; // substitui o vértice na posição comeco pelo vértice na posição fim
            individuoAux[fim] = temp; // substitui o vértice na posição fim pelo vértice temporariamente armazenado
            comeco++; // incrementa o índice comeco
            fim--; // decrementa o indice fim
        }

        return individuoAux;
    }
}