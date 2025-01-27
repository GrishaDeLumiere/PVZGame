package game;

import java.util.concurrent.LinkedBlockingQueue;

public class MainThreadManager {

    private static final Thread mainThread = Thread.currentThread(); 
    private static final LinkedBlockingQueue<Runnable> mainThreadQueue = new LinkedBlockingQueue<>(); 
    
    public static void executeInMainThread(Runnable task) {
        if (isMainThread()) {
            task.run();
        } else {
            addTaskToMainThread(task);
        }
    }

    // Проверка, является ли текущий поток основным
    public static boolean isMainThread() {
        return Thread.currentThread() == mainThread;
    }

    // Получение основного потока
    public static Thread getMainThread() {
        return mainThread;
    }

    // Добавление задачи в очередь основного потока
    private static void addTaskToMainThread(Runnable task) {
        mainThreadQueue.add(task);
    }

    // Метод для обработки задач, находящихся в очереди
    public static void processMainThreadQueue() {
        while (!mainThreadQueue.isEmpty()) {
            Runnable task = mainThreadQueue.poll();
            if (task != null) {
                task.run();
            }
        }
    }
}