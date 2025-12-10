package trabalho;

import java.util.Scanner;

public class Main {
    
    public static void main(String[] args) {
        AlgoritmoOtimo algoritmoOtimo;
        AlgortimoGenetico algoritmoGenetico;
        Scanner Leitor = new Scanner(System.in);
        long startTime, endTime;
        
        System.out.println("Qual algoritmo deseja executar?");
        System.out.println("1 - Algoritmo Exato (Força Bruta)");
        System.out.println("2 - Algoritmo Genético");
        int escolha = Leitor.nextInt();

        switch (escolha) {
            case 1 -> {
                startTime = System.currentTimeMillis();
                algoritmoOtimo = new AlgoritmoOtimo();
                algoritmoOtimo.getMelhorCusto(algoritmoOtimo.getGrafo());
                endTime = System.currentTimeMillis();
                if (endTime - startTime >= 60000) {
                    System.out.println("Tempo de execução: " + (endTime - startTime) / 60000 + " minutos.");
                } else if (endTime - startTime >= 1000) {
                    System.out.println("Tempo de execução: " + (endTime - startTime) / 1000 + " segundos.");
                } else {
                    System.out.println("Tempo de execução: " + (endTime - startTime) + " milissegundos.");
                }
            }
            case 2 -> {
                startTime = System.currentTimeMillis();
                algoritmoGenetico = new AlgortimoGenetico();
                algoritmoGenetico.algoritmoGenetico();
                endTime = System.currentTimeMillis();
                if (endTime - startTime >= 60000) {
                    System.out.println("Tempo de execução: " + (endTime - startTime) / 60000 + " minutos.");
                } else if (endTime - startTime >= 1000) {
                    System.out.println("Tempo de execução: " + (endTime - startTime) / 1000 + " segundos.");
                } else {
                    System.out.println("Tempo de execução: " + (endTime - startTime) + " milissegundos.");
                }
            }
            default -> System.out.println("Opção inválida. Por favor, escolha 1 ou 2.");
        }

        Leitor.close();
    }
}
