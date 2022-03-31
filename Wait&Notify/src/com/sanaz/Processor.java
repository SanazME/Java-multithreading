package com.sanaz;

import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

public class Processor {

    private LinkedList<Integer> list = new LinkedList<Integer>();
    private final int LIMIT = 10;
    private Object lock = new Object();

    public void produce() throws InterruptedException{
        /**
         * Producer adds items to the list (shared data store)
         */

        int value = 0;
        Random random = new Random();

        while (true) {

            /**
             * code accessing the shared data has to be inside sync block
             */
            synchronized (lock){
                /**
                 * Usually we surround wait() in a loop to check for a condition
                 * before waking up the thread again
                 */
                while (list.size() == LIMIT) {
                    // add wait to the object we lock
                    lock.wait();
                }
                list.add(value++);
                lock.notify(); // it wakes up the other thread
            }
            Thread.sleep(random.nextInt(1000));
        }
    }

    public void consume() throws InterruptedException {

        Random random = new Random();

        while (true) {

            synchronized (lock){

                while (list.size() == 0) {
                    lock.wait();
                }

                System.out.print("List size is: " + list.size());
                int value = list.removeFirst();
                System.out.println("; value is: " + value);
                lock.notifyAll();
            }

            // sleep on average 500 ms
            Thread.sleep(random.nextInt(1000));
        }
    }
}




