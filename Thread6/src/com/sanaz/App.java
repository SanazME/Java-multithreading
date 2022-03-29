package com.sanaz;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Processor implements Runnable {

    private int id;

    // since we're going to create lots of processor objects, we add a constructor with the id of the processor
    public Processor(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        System.out.println("Starting " + id);

        // Doing some work like processing a file etc
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Completed " + id);
    }
}

public class App {

    public static void main(String[] args) {

        ExecutorService executor = Executors.newFixedThreadPool(2);

        for (int i=0; i<5; i++){
            executor.submit(new Processor(i));
        }

        executor.shutdown();

        System.out.println("All tasks submitted.");

        // if the tasks did not finish in a day, this line will wait one day and terminates
        try {
            executor.awaitTermination(1, TimeUnit.DAYS); // wait for one day
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("All tasks completed.");
    }
}
