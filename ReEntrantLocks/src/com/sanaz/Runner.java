package com.sanaz;

import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Runner {

    private int count = 0;
    private Lock lock = new ReentrantLock();
    private Condition cond = lock.newCondition();

    private void increment(){
        for (int i=0; i<10000; i++){
            count++;
        }
    }

    public void firstThread() throws InterruptedException {
        lock.lock();

        /**
         * It unlocks that lock and another thread can get in there and lock it.
         */
        cond.await();

        System.out.println("Woken up!");

        try{
            increment();
        } finally {
            lock.unlock();
        }
    }

    public void secondThread() throws InterruptedException {
        Thread.sleep(1000);
        lock.lock();
        // wait for the next line to be pressed
        System.out.println("Press the return key");
        new Scanner(System.in).nextLine();
        System.out.println("Got return key");

        /**
         * it wakes up the first thread
         */
        cond.signal();

        try{
            increment();
        } finally {
            lock.unlock();
        }
    }

    public void finished() {
        System.out.println("Count is: " + count);
    }
}
