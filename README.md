# Java-multithreading

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