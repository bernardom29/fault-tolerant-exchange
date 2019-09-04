import Server.StockServer;

import java.io.IOException;

public class ServerBenchmark {
    public static void main(String[] args) {
        Thread[] threads = new Thread[5];
        int limit = 5;
        for (int i = 0; i < limit; i++) {
            System.out.println(i);
            RunnableTask task = new RunnableTask(i);
            threads[i] = new Thread(task);
            threads[i].start();
        }
        for (int i = 0; i < limit; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class RunnableTask implements Runnable {
        int i;

        public RunnableTask(int i) {
            this.i = i;
        }


        @Override
        public void run() {
            try {
                StockServer ss = new StockServer(Integer.toString(i), i + 1000);
                ss.setup();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
