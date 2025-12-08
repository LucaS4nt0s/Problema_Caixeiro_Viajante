package trabalho;

public class Main {
    
    public static void main(String[] args) {
        AlgoritmoOtimo algoritmoOtimo;
        
        try {
            algoritmoOtimo = new AlgoritmoOtimo();

            long startTime = System.currentTimeMillis();
            double custoOtimo = algoritmoOtimo.getMelhorCusto(algoritmoOtimo.getGrafo());
            long endTime = System.currentTimeMillis();
            System.out.println("Custo Ótimo: " + custoOtimo);
            if(endTime - startTime < 1000){
                System.out.println("Tempo de execução: " + (endTime - startTime) + " ms");
            } else if(endTime - startTime < 60000){
                System.out.println("Tempo de execução: " + ((endTime - startTime)/1000.0) + " s");
            } else {
                System.out.println("Tempo de execução: " + ((endTime - startTime)/60000.0) + " min");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
