package com.example.warehouse;


import java.util.Queue;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Warehouse {
    private static final int MAX_QUEUE_SIZE = 200;
    private static final int MIN_PRODUCER_THRESHOLD = 80;
    private static final Scanner scanner = new Scanner(System.in);

    private Queue<Integer> queue = new LinkedList<>();
    private Lock lock = new ReentrantLock();

    private volatile boolean running = true;

    public void produce(String producerName) {
        while (running) {
            try {
                lock.lock();
                if (queue.size() >= MAX_QUEUE_SIZE) {
                    System.out.println(producerName + " спит, очередь полна.");
                    lock.unlock();
                    waitUntilNotFull();
                    continue;
                }

                int случайноеЧисло = new Random().nextInt(100) + 1;
                queue.offer(случайноеЧисло);
                System.out.println(producerName + " произвел: " + случайноеЧисло + ", размер очереди: " + queue.size());

                if (queue.size() <= MIN_PRODUCER_THRESHOLD) {
                    System.out.println("Пробуждаются производители!");
                    lock.unlock();
                    waitUntilNotFull();
                } else {
                    lock.unlock();
                }

                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Переустанавливаем флаг прерывания
                System.err.println("Производитель прерван.");
                return;
            }
        }
    }

    public void consume(String consumerName) {
        while (running || !queue.isEmpty()) {
            try {
                lock.lock();
                while (queue.isEmpty() && running) {
                    System.out.println(consumerName + " спит, очередь пуста.");
                    lock.unlock();
                    waitUntilNotEmpty();
                    lock.lock();
                }

                if (queue.isEmpty()) {
                    break; // Если очередь пуста и программа завершается, прерываем цикл
                }

                int потребленноеЧисло = queue.poll();
                System.out.println(consumerName + " потребил: " + потребленноеЧисло + ", размер очереди: " + queue.size());

                lock.unlock();
                Thread.sleep(700);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Переустанавливаем флаг прерывания
                System.err.println("Потребитель прерван.");
                return;
            }
        }
    }

    private void waitUntilNotFull() throws InterruptedException {
        while (queue.size() >= MAX_QUEUE_SIZE && running) {
            Thread.sleep(1000);
        }
    }

    private void waitUntilNotEmpty() throws InterruptedException {
        while (queue.isEmpty() && running) {
            Thread.sleep(1000);
        }
    }

    public void stopSimulation() {
        running = false;
    }

    public void runSimulation() {
        Thread producer1 = new Thread(() -> produce("Производитель 1"));
        Thread producer2 = new Thread(() -> produce("Производитель 2"));
        Thread producer3 = new Thread(() -> produce("Производитель 3"));

        Thread consumer1 = new Thread(() -> consume("Потребитель 1"));
        Thread consumer2 = new Thread(() -> consume("Потребитель 2"));

        producer1.start();
        producer2.start();
        producer3.start();
        consumer1.start();
        consumer2.start();

        System.out.println("Нажмите 'q' для завершения программы.");

        while (running) {
            if (scanner.hasNextLine() && scanner.nextLine().equalsIgnoreCase("q")) {
                stopSimulation();
                break;
            }
        }

        // Прерываем производителей
        producer1.interrupt();
        producer2.interrupt();
        producer3.interrupt();

        // Ждем завершения работы производителей
        try {
            producer1.join();
            producer2.join();
            producer3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Ждем завершения работы потребителей
        try {
            consumer1.join();
            consumer2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Симуляция завершена.");
    }
}

public class WarehouseSimulation {
    public static void main(String[] args) {
        Warehouse warehouse = new Warehouse();
        warehouse.runSimulation();
    }
}
