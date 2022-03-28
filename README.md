# Java-multithreading

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