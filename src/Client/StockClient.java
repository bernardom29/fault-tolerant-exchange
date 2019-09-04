package Client;

import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class StockClient implements Runnable{
    private int id;
    private StockStub stockStub;
    private int counter;

    public StockClient(int id, int port) {
        this.id = id;
        this.counter = 0;
        try {
            this.stockStub = new StockStub(id,port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void benchReqs() {
        counter++;
    }

    public int benchReset() {
        int value = counter;
        counter = 0;
        return value;
    }

    public void runBench() {
        Random rand = new Random();
        String[] stocks = {"apple", "pear", "banana"};
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int value = rand.nextInt(10);
        int op = rand.nextInt(2);
        int stock = rand.nextInt(3);
        if (op == 0) {
            CompletableFuture<Boolean> cf = stockStub.buy(stocks[stock], value);
            getAsync(cf, -1);
        } else if (op == 1) {
            CompletableFuture<Boolean> cf = stockStub.sell(stocks[stock], value);
            getAsync(cf, -1);
        }
    }

    @Override
    public void run() {
        int op = 0;
        Scanner scanner = new Scanner(System.in);
        while(true) {
            System.out.println("Introduza um comando");
            String[] command = scanner.nextLine().split("\\s+");
            if(command[0].equals("buy")) {
                CompletableFuture<Boolean> cf = stockStub.buy(command[1],Integer.parseInt(command[2]));
                System.out.println("Operação#" + op + " enviada.");
                getAsync(cf, op);
            }
            else if(command[0].equals("sell")) {
                CompletableFuture<Boolean> cf = stockStub.sell(command[1],Integer.parseInt(command[2]));
                System.out.println("Operação#" + op + " enviada.");
                getAsync(cf, op);
            }
            else {
                System.out.println("Comando inválido.");
            }
            op++;
        }
    }

    private void getAsync(CompletableFuture<Boolean> cf, int op) {
        cf.whenCompleteAsync((r, t) -> {
            try {
                if(cf.get()) {
                    //   System.out.println("Operação#"+op+" com succeso.");
                }
                else {
                    //   System.out.println("Operação#"+op+" sem succeso.");
                }
                this.benchReqs();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args){
        // StockClient 1 3000
        StockClient client = new StockClient(Integer.parseInt(args[0]),Integer.parseInt(args[1]));
        new Thread(client).start();
    }

}
