package com.sanaz;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class App {

    private static BlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(10);

    public static void main(String[] args) throws InterruptedException {
        // One thread for producer
	    Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                producer();
            }
        });

        // One thread for consumer
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                consumer();
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

    }

    private static void producer(){
        Random random = new Random();

        while (true) {
            try {
                queue.put(random.nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void consumer(){
        Random random = new Random();

        while (true) {
            try {
                Thread.sleep(100);

                if (random.nextInt(10) == 0){
                    Integer value = queue.take();

                    System.out.println("Taken value: " + value + "; Queue size is: " + queue.size());
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
