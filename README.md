# Java-multithreading

# Table of Contents
1. [Ways to Start a Thread](#ways-to-start-a-thread)
2. [Basic Thread Synchornization](#basic-thread-synchornization)
3. [Multiple Locks; Using Synchronized Code Blocks](#multiple-locks-using-synchronized-code-blocks)
4. [Thread Pools](#thread-pools)
5. [Countdown Latches](#countdown-latches)
6. [Producer-Consumer pattern](#producer-consumer-pattern)
7. [Wait and Notify](#wait-and-notify)
8. [Low-Level Producer-Consumer (Low-Level Synchronization)](#low-level-producer-consumer-low-level-synchronization)
9. [Re-entrant Locks](#re-entrant-locks)

## Ways to Start a Thread 
- There are two way to start a thread in java.
- Thread is a like a separate OS process which can run concurrently with other threads.

1. Extend `Thread` class and overrides method `run`. we define a `Runner` class as an extension of `Thread` class
- to run the application we need to run `runner1.start()`. It's important not to call `run` method we override here because if call `runner1.run()` it runs the code but **inside the main thread of the application**. With `runner1.start()` it tell the **Thread class to go and look for the `run()` method and to run that in its own special thread**.
```java
class Runner extends Thread {

    @Override
    public void run() {
        for (int i=0; i<10; i++){
            System.out.println("Hello " + i);

            try {
                Thread.sleep(100); // to slow the loop, sleep every 100 ms
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public class App {

    public static void main(String[] args) {
        Runner runner1 = new Runner();
        runner1.start();

        Runner runner2 = new Runner();
        runner2.start();

    }
}
```
- we see both runner1 and runner2 run the loop interleavely/simultanously.

2. Implement `Runnable` and pass it to the constructor of the Thread class. `Runnable` is an interface which has only one method: `run()`.
- Then we create a new instance of `Thread` and pass in an instance of our Runner class there.
```java
class Runner implements Runnable {

    @Override
    public void run() {
        for (int i=0; i<10; i++){
            System.out.println("Hello " + i);

            try {
                Thread.sleep(100); // to slow the loop, sleep every 100 ms
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public class App {

    public static void main(String[] args) {
        Thread t1 = new Thread(new Runner());
        Thread t2 = new Thread(new Runner());

        t1.start();
        t2.start();

    }
}

```
 
- sometimes, we only need to run one method inside a thread and creating a whole class seems to be a lot of hassle. So we can use anonymous class which is faster and we don't need to creat a separate class for it.
```java
public class App {

    public static void main(String[] args) {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=0; i<10; i++){
                    System.out.println("Hello " + i);

                    try {
                        Thread.sleep(100); // to slow the loop, sleep every 100 ms
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        t1.start();
    }
}

```

## Basic Thread Synchornization
- There are two problems with threads sharing the same data:
  1. Data being cached
  2. Threads interleaving
  
### 1. Data being cached (volatile keyword)
- In the code below, we defined a boolean running variable and a method shutdown to set the variable to false. 

- The `main()` is running in a main thread and when we call `proc1.start()` the code inside run() is run inside a separate thread than the main (method) thread.
- When we call `proc1.shutdown()` that runs inside the main (method) thread which is different from the thread running the code in proc1.start()
- Now both threads are accessing that `running` variable and the second one is changing its value. 
- Sometimes Java for optimization, the thread that running the code might cache the value of that variable and might not see the change in that value.

- the thread running that loop might cache the value of `running` variable and so it might neve see the change in that value done by other thread **because that code does not live inside this thread.**

- to prevent that issue we use `volatile` keyword for that variable. That gaurantee the code works on all systems.
- **`volatile` prevents threads from caching variables when they're not chaned within that thread**.
- 
- To change a variable from another thread, you should either:
  1. use `volatile`
  2. or use some kind of **thread synchornization**

```java
class Processor extends Thread {

    private volatile boolean running = true;

    @Override
    public void run() {

        while(running){
            System.out.println("Hello");

            try {
                Thread.sleep(100); // pause for 100 ms
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdown(){
        running = false;
    }
}


public class App {

    public static void main(String[] args) {

        Processor proc1 = new Processor();
        proc1.start();

        System.out.println("Press return to stop");
        Scanner scanner = new Scanner(System.in); // scans input line and detects the new line
        scanner.nextLine(); // it pauses the execution of the main thread until we hit the return key

        proc1.shutdown();

    }
}
```

- For thread synchronization, we create two threads inside the doWork method and we stat the two threads, now wihtout using `t1.join()` the result of the count might not be 20,000 because in the Main (mehtod) thread, the two threads are spawned/started immediately and then the system print out which might return 0 and then those loops are started in each thread.
- To avoid that, we use `t1.join()` to wait for each thread to die and finish work.
- Still, if we run it multiple times, we might get different answers because the operation `count ++` that seems to be an atomic operation (done in one step) is actually done in 3 steps!. it is equivalent of `count = count + 1`
  1. get value of count
  2. add 1 to that 
  3. store back into count

- so some increments might be skipped because both threads read the value because of **interleaving problem**. One thread might increment twice before the other thread can increment the count. Or both thread read the value and increment to the same value.
- To avoid it, we want to make sure that when a thread reads the value of increments and stops it, no other thread can get at it and change it.

```java
public class App {
    private int count = 0;

    public static void main(String[] args) {
        App app = new App();
        app.doWork();
    }

    public void doWork(){
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=0; i<10000; i++){
                    count ++;
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=0; i<10000; i++){
                    count ++;
                }
            }
        });

        t1.start();
        t2.start();


        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Count is: " + count);
    }
}
```

- The simplest solution when we can only an integer parameter is to make an **AtomicInteger**. **AtomicInteger is a specialized class that allows you to a increment your count variable in one step (atomic).**

- More general way is to define a method for incrementing the count and use `synchronized` keyword for that method.

- **every object in java has an intrinsic lock / monitor lock / mutex**. if you call a synchronized method of an object (in our case we're calling synchronized increment method of object App), you have to acquire the intrinsic lock before you can call it.
- 
- Only one thread can acquire the intrinsic lock of an object at a time. If one thread acquires the intrinsic lock of object App and runs that method and if the second thread tries to call the same method, the second thread has to wait until the first thread release the intrinsic lock by that method finishes executing.
- 
- **Every object has one intrinsic lock and only one thread can acquire it at a given time. A method marked `synchronized` can only be called acquiring the intrinsic lock**.
- 
- with synchronized, you don't have to use `volatile` because synchronized guarantees that all threads can see the current state of a variable.
```java
public class App {

    private int count=0;

    public synchronized void increment(){
        count ++;
    }

    public static void main(String[] args) {
        App app = new App();
        app.doWork();
    }

    public void doWork(){
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=0; i<10000; i++){
                    increment();
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=0; i<10000; i++){
                    increment();
                }
            }
        });

        t1.start();
        t2.start();


        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Count is: " + count);
    }
}
```

## Multiple Locks; Using Synchronized Code Blocks
- we have our main app which call Worker.main() method and Worker class has two lists and two stages and do work on those lists. Let's imagine that `stageOne()` method in Worker class does some calculations which we show by sleeping the thread `Thread.sleep(1) // 1 s` to slow the program. For example that can represents pinging a machine which takes some time and then adding that ping time: `list1.add(random.nextInt(100))`.

<details>
  <summary> Multiple locks: </summary>

```java
public class Worker {

    private Random random = new Random();

    private List<Integer> list1 = new ArrayList<Integer>();
    private List<Integer> list2 = new ArrayList<Integer>();

    public void stageOne(){
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        list1.add(random.nextInt(100));
    }

    public void stageTwo(){
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        list2.add(random.nextInt(100));
    }

    public void process(){
        for (int i=0; i<1000; i++){
            stageOne();
            stageTwo();
        }
    }

    public void main(){
        System.out.println("Starting ... ");

        long start = System.currentTimeMillis();

        process();

        long end = System.currentTimeMillis();

        System.out.println("Time take: " + (end - start));
        System.out.println("List1: " + list1.size() + "; list2: " + list2.size());
    }
}

public class App {

  public static void main(String[] args) {
    new Worker().main();
  }
}

```

</details>

- so in the above scenario, we might be processing a file or pinging a machine and in the `process` method it loops 1000 times and each time, it calls stageOne and stageTwo. The output time is about 2-3 s because each stage take 1 ms and it repeats 1000 times so the overall of two processes are about 2000 ms.
```java
Starting ... 
Time take: 2661
List1: 1000; list2: 1000
```

- we can speed up the process by running them on multiple threads. So define two threads that run process method and we see that now the size of lists are different that 1000 each because of thread interleaving problem:

<details>
  <summary> click to expand:</summary>

```java
public class Worker {

    private Random random = new Random();

    private List<Integer> list1 = new ArrayList<Integer>();
    private List<Integer> list2 = new ArrayList<Integer>();

    public void stageOne(){
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        list1.add(random.nextInt(100));
    }

    public void stageTwo(){
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        list2.add(random.nextInt(100));
    }

    public void process(){
        for (int i=0; i<1000; i++){
            stageOne();
            stageTwo();
        }
    }

    public void main(){
        System.out.println("Starting ... ");

        long start = System.currentTimeMillis();
        // thread 1
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                process();
            }
        });

        // thread 2
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                process();
            }
        });

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();

        System.out.println("Time take: " + (end - start));
        System.out.println("List1: " + list1.size() + "; list2: " + list2.size());
    }
}
```
</details>


output and the time takes is still about that 2 s:
```java
Starting ... 
Time take: 2674
List1: 1808; list2: 1831
```

- we have to make stageOne and stageTwo methods `synchronized` by adding that keyword and running it now takes double time while the size of arrays are correct:
```java
Starting ... 
Time take: 5390
List1: 2000; list2: 2000
```

- The reason is when we call synchronized keyword, it acquire the intrinsic lock of `Worker` object and so if one thread runs `stageOne` method, the second try can not run that method and has to wait till the lock is released. The same for `stageTwo` method which is the behavior we want BUT **the problem is there is only ONE lock for `Worker` object so if one thread runns stageOne method, another thread has to wait to run stageTwo method even though those two methods are INDEPENDENT and not sharing the same data and can be run on two different threads at the same time**.

<details>
  <summary> we can do this by creating separate lock objects (new objects) and synchronizing locks separately (adding synchronized block): </summary>

```java
public class Worker {

    private Random random = new Random();

    private Object lock1 = new Object();
    private Object lock2 = new Object();

    private List<Integer> list1 = new ArrayList<Integer>();
    private List<Integer> list2 = new ArrayList<Integer>();

    public void stageOne(){

        synchronized (lock1) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            list1.add(random.nextInt(100));
        }
    }

    public void stageTwo(){

        synchronized (lock2) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            list2.add(random.nextInt(100));
        }
    }

    public void process(){
        for (int i=0; i<1000; i++){
            stageOne();
            stageTwo();
        }
    }

    public void main(){
        System.out.println("Starting ... ");

        long start = System.currentTimeMillis();
        // thread 1
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                process();
            }
        });

        // thread 2
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                process();
            }
        });

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();

        System.out.println("Time take: " + (end - start));
        System.out.println("List1: " + list1.size() + "; list2: " + list2.size());
    }
}
```
</details>


- Now it correctly locks each method indepent of the other method and each method can be run on different threads simultaneously while the for a given method, only one thread can access it at a time. The output:
```java
Starting ... 
Time take: 2842
List1: 2000; list2: 2000
```

## Thread Pools
- Thread pools are way of managing lots of threads. There is a lot of overhead in starting a thread and by recycling threads in a thread pool we avoid that overhead.
- To create multiple threads, we can either use `Thread` class or use `ExecutorService` as below:
```java
// create a thread pool of 2 workers
ExecutorService executor = Executors.newFixedThreadPool(2);

// passing tasks to each of those workers (executors)
for (int i=0; i<5; i++){
    executor.submit(new Processor(i));
}

/**
 * once the threads are done processing, shut them down, it waits till all the threads are finished processing
 * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be accepted. 
 * Invocation has no additional effect if already shut down.
 * This method does not wait for previously submitted tasks to complete execution. Use  awaitTermination to do that.
 */ 
executor.shutdown();

/**
 * Blocks until all tasks have completed execution after a shutdown request, or the timeout occurs, 
 * or the current thread is interrupted, whichever happens first.
 */
executor.awaitTermination(1, TimeUnit.DAYS); // wait for one day
```

- This is a thread pool of 2 workers (executors) to do tasks. ExecutorService is a managerial thread that handles those threads.

```java
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
```

- The output:
- When a task is completed in a thread another task is started in that same thread:
```java
All tasks submitted.
Starting 0
Starting 1
Completed 0
Completed 1
Starting 2
Starting 3
Completed 3
Completed 2
Starting 4
Completed 4
All tasks completed.
```

## Countdown Latches
- `CountDowmLatch` class lets you count down from a number that you specify. It lets one or more threads wait until the latch reaches 0. One or more thread can count down the latch and when it's zero then one or more threads that are waiting on the latch can proceed.
- Until the count reaches zero, actions in a thread prior to calling `countDown()` happen-before actions following a successful return from a corresponding `await()` in another thread.

- 

```java
class Processor implements Runnable {

    private CountDownLatch latch;

    public Processor(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void run() {
        System.out.println("Started.");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /**
         * after the pervious process is done, we count down latch
         * it is also thread-safe so different threads counting this down
         * won't cause interleaving issues.
         */
        latch.countDown();

    }
}

public class App {

    public static void main(String[] args) {
        CountDownLatch latch = new CountDownLatch(3);

        // create 3 threads
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Each processor is assigned to a thread, we can have more 100 processors
        // Each processor has latch and counts it down to zero
        for (int i=0; i<3; i++){
            executor.submit(new Processor(latch));
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Completed.");
    }
}

```

## Producer-Consumer pattern
- `BlockingQueue` data structure is FIFO queue: A Queue that additionally supports operations that wait for the queue to become non-empty when retrieving an element, and wait for space to become available in the queue when storing an element.
  
- `BlockingQueue` methods come in four forms, with different ways of handling operations that cannot be satisfied immediately, but may be satisfied at some point in the future: 
1. one throws an exception, 
2. the second returns a special value (either null or false, depending on the operation), 
3. the third blocks the current thread indefinitely until the operation can succeed, and the 
4. fourth blocks for only a given maximum time limit before giving up. 

These methods are summarized in the following table:

Attempt | Throws exception | Special value | Blocks | Times out
--- |------------------|---------------|--------|---
Insert | add(e)           | offer(e)      | put(e) | offer(e, time, unit) 
Remove | remove()         | poll()        | take() |poll(time, unit) 
Examine | element()        | peek()        | not applicable    | not applicable 

- BlockingQueue implementations are designed to be used primarily for producer-consumer queues, but additionally support the Collection interface. So, for example, it is possible to remove an arbitrary element from a queue using remove(x). However, such operations are in general not performed very efficiently, and are intended for only occasional use, such as when a queued message is cancelled.
- 
- BlockingQueue implementations are thread-safe. All queuing methods achieve their effects atomically using internal locks or other forms of concurrency control. However, the bulk Collection operations addAll, containsAll, retainAll and removeAll are not necessarily performed atomically unless specified otherwise in an implementation. So it is possible, for example, for addAll(c) to fail (throwing an exception) after adding only some of the elements.
- 
- A BlockingQueue does not intrinsically support any kind of "close" or "shutdown" operation to indicate that no more items will be added. The needs and usage of such features tend to be implementation-dependent. For example, a common tactic is for producers to insert special end-of-stream or poison objects, that are interpreted accordingly when taken by consumers.
-
- Memory consistency effects: As with other concurrent collections, actions in a thread prior to placing an object into a BlockingQueue happen-before actions subsequent to the access or removal of that element from the BlockingQueue in another thread.
- 
- Usage example, based on a typical producer-consumer scenario. Note that a BlockingQueue can safely be used with multiple producers and multiple consumers.
```java
class Producer implements Runnable {     
    private final BlockingQueue queue;     
    Producer(BlockingQueue q) { queue = q; }     
  
    public void run() {       
        try {         
            while (true) { queue.put(produce()); }       
        } catch (InterruptedException ex) 
        { ... handle ...}     
    }     
    
    Object produce() { ... }   
}

class Consumer implements Runnable {     
    private final BlockingQueue queue;     
    Consumer(BlockingQueue q) { queue = q; }     
  
    public void run() {
        try {
            while (true) { consume(queue.take());
            }
        } catch (InterruptedException ex)
            { ... handle ...}     
    }     
    
    void consume(Object x) { ... }   
}

class Setup {
    void main() {
        BlockingQueue q = new SomeQueueImplementation();
        Producer p = new Producer(q);
        Consumer c1 = new Consumer(q);
        Consumer c2 = new Consumer(q);
        new Thread(p).start();
        new Thread(c1).start();
        new Thread(c2).start();
    }
}
```

<details>
  <summary>Here is out example:</summary>

```java
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

```
</details>

## Wait and Notify
- Prodcuer and Consumer are high-level synchronization using BlockingQueue from concurrent library which is thread safe.
- Wait and Notify are low-level thread synchronization technique in case we need it.
- In the following example we define a Processor class with produce and consume methods and then we run each method inside a different thread.
- When using synchronized block on `this` on the class object itself. So for example here, `this` is the Processor object and this block does not run until it acquire the intrinsic lock of object Processor:
```java
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
```
<details>
  <summary>Inside the main app, we create thread and run those methods:</summary>

```java
public class App {

  public static void main(String[] args) {

    final Processor processor = new Processor();

    Thread t1 = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          processor.produce();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });

    Thread t2 = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          processor.consume();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });

    t1.start();
    t2.start();

    try {
      t1.join();
      t2.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }


  }
}
```
</details>

- Every object (since inheriting from `Object` class) has `wait() or wait(timeout)` method. It is a method of `Object` class.
- `wait()` it waits and it does not consume system resources. **You can `wait()` and `notify()` only call it within synchronized code blocks**. It hands over control of the lock that the synchronized block is locked on.
- At the line `wait()` the synchronized block will lose control of the lock.

- `wait(timeout, nanos)` : Causes the current thread to wait until it is awakened, typically by being notified or interrupted, or until a certain amount of real time has elapsed.
  The current thread must own this object's monitor lock. 
  
- This method causes the current thread (referred to here as T) to place itself in the wait set for this object and then to relinquish any and all synchronization claims on this object. Note that only the locks on this object are relinquished; any other objects on which the current thread may be synchronized remain locked while the thread waits. 
- Thread T then becomes disabled for thread scheduling purposes and lies dormant until one of the following occurs:
1. Some other thread invokes the `notify` method for this object and thread T happens to be arbitrarily chosen as the thread to be awakened. 
2. Some other thread invokes the `notifyAll` method for this object. 
3. Some other thread interrupts thread T. 
4. The specified amount of real time has elapsed, more or less. The amount of real time, in nanoseconds, is given by the expression 1000000 * timeoutMillis + nanos. If timeoutMillis and nanos are both zero, then real time is not taken into consideration and the thread waits until awakened by one of the other causes. 
5. Thread T is awakened spuriously. (See below.)

## Low-Level Producer-Consumer (Low-Level Synchronization)
- We usually put `lock.wait()` in a while loop to check a condition before waking up that thread again.
- the lock keeps alternating between these two threads of producer and consumer. Producer adds values to list as fast as it can and everytime it notifies consumer, consumer has a random delay and remove item from list and notify back producer. 
- We can see in results that no integer values are duplicated or lost (all in order) because of synch interleacving issue.
- we can add delay also to producer to slow down the adding item to list:
<details>
  <summary>wait-notify (low-level synchronization for producer-consumer pattern):</summary>

```java
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
          // sleep on average 500 ms
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
```
</details>

## Re-entrant Locks
- Re-entrant locks are another technique replaces synchronization block.
- In the Runner class we have defined firstThread, secondThread and finished methods and in the main Applicaiton, we run the firstThread and secondThread in two different threads at the same time and after those threads are finished it calls finished method in the main app thread.
- In the Runner class, we define a count varianle and a method to increment that count variable. We call that increment method in both of first and second threads. 
- We can use synchronized block but instead, we use `ReentrantLock()`. ReentrantLock means that once a thread had acquired this lock and once thread is locked this lock, it can lock it again if it wants to and the lock keeps count of how many times it's been locked and then you have to unlock it by the same number of times.
- Now adding lock and unlock like below is not good:
```java
public void firstThread() throws InterruptedException {
        lock.lock();
        increment();
        lock.unlock();
    }
```
because if `increment()` throws an exception, the `lock.unlock()` will never be called. We should always add increment() inside catch and try and add `lock.unlock()` inside `finally` block to make sure that it is always called:
```java
public void firstThread() throws InterruptedException {
    lock.lock();
    try{
        increment();
    } finally {
        lock.unlock();
    }
}
```
<details>
  <summary> The example with reentrant lock:</summary>

```java
public class Runner {

    private int count = 0;
    private Lock lock = new ReentrantLock();

    private void increment(){
        for (int i=0; i<10000; i++){
            count++;
        }
    }

    public void firstThread() throws InterruptedException {
        lock.lock();

        try{
            increment();
        } finally {
            lock.unlock();
        }
    }

    public void secondThread() throws InterruptedException {
        lock.lock();

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


public class App {

  public static void main(String[] args) throws InterruptedException {

    final Runner runner = new Runner();

    Thread t1 = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          runner.firstThread();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });

    Thread t2 = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          runner.secondThread();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });

    t1.start();
    t2.start();

    t1.join();
    t2.join();

    runner.finished();
  }
}

```

</details>

- The equivalent of wait and notify for reentrant locks: Every Object in java had a wait and notify because they are method of the Object class. The names of method for ReentrantLock is different though:
  - await instead of wait : It unlocks that lock and another thread can get in there and lock it.
  - signal instead of notify
- We need to the `Condition` object from the lock we're locking on. After gotting lock for the current thread, we can only call `await` or `signal` on that lock.
```java
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

```
- When calling `cond.signal()` to wake up the waiting thread (other thread), it's not enough and the other thread should also be able to acquire the lock so we need to always call `lock.unlock()` after we call `cond.signal()`. That thread must then re-acquire the lock before returning from await.