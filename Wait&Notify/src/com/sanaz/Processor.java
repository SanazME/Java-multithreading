package com.sanaz;

import java.util.Scanner;

public class Processor {
    public void produce() throws InterruptedException{
        // this block runs first (before consume block) because of Thread.sleep in consume to delay the execusion to
        // after this block of code
        synchronized (this){
            System.out.println("Producer thread running ....");
            /**
             * at wait() this thread relinquishes the lock and
             * lose the control of intrinsic lock (which other thread running consumer will acquire it)
             */
            wait();
            System.out.println("Resumed.");
        }
    }

    public void consume() throws InterruptedException {

        Scanner scanner = new Scanner(System.in);
        Thread.sleep(2000);

        synchronized (this){
            System.out.println("Waiting for return key.");
            scanner.nextLine();
            System.out.println("Return key is pressed");
            /**
             * notify the other thread lock on the same object to wake up
             * notify does not relinquish the control of the lock
             * after calling notify()/notifyAll() we want to relinquish the lock quickly otherwise
             * the other thread will not be able to get the lock again.
             * We do it by adding notify() at the end of synchronization block so the
             * lock can be relinquished.
             */
            notify();
        }
    }
}




