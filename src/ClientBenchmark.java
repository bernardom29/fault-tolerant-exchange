import Client.StockClient;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientBenchmark {
    public static void main(String[] args) {
        Thread[] threads = new Thread[550];
        int i = 1;
        for (i = 1; i < 51; i++) {
            RunnableTask task = new RunnableTask(i, 1000);
            threads[i - 1] = new Thread(task);
            threads[i - 1].start();
        }
        for (i = 0; i < 50; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class RunnableTask implements Runnable {
        int i;
        AtomicInteger currentReq;
        int reqs;
        long time;
        StockClient sc;
        int ts;
        ScheduledExecutorService service;

        public RunnableTask(int i, int reqs) {
            this.i = i;
            this.reqs = reqs;
            time = new Date().getTime();
            currentReq = new AtomicInteger(0);
            sc = new StockClient(i, i + 3000);
            ts = 0;
            service = Executors.newSingleThreadScheduledExecutor();
            Runnable task = () -> {
                long currentTime = new Date().getTime();
                int rc = sc.benchReset();
                double througput = (double) rc * 1000 / (double) (currentTime - time);
                System.out.println(ts + ";" + througput);
                ts++;
                time = currentTime;
                currentReq.set(0);
            };
            service.scheduleAtFixedRate(task, 1000, 1000, TimeUnit.MILLISECONDS);
        }


        @Override
        public void run() {

            int j = 0;
            Random rand = new Random();
            while (j < reqs) {

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sc.runBench();
                j++;
            }
        }
    }
}
