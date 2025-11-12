import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class FileCounterParallel {

    static class FileCountTask extends RecursiveTask<Integer> {
        private final File dir;
        private final String extension;

        public FileCountTask(File dir, String extension) {
            this.dir = dir;
            this.extension = extension;
        }

        @Override
        protected Integer compute() {
            int count = 0;
            List<FileCountTask> subtasks = new ArrayList<>();

            File[] files = dir.listFiles();
            if (files == null) return 0;
            for (File file : files) {
                if (file.isDirectory()) {
                    subtasks.add(new FileCountTask(file, extension));
                } else if (file.isFile() && file.getName().toLowerCase().endsWith(extension)) {
                    count++;
                }
            }
            if (!subtasks.isEmpty()) {
                invokeAll(subtasks);
                for (FileCountTask t : subtasks) {
                    count += t.join();
                }
            }
            return count;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ForkJoinPool pool = new ForkJoinPool();
        System.out.println("📁 Програма для підрахунку файлів певного формату");
        System.out.println("Команди: 'back' — змінити директорію, 'exit' — завершити.\n");
        File currentDir = null;
        while (true) {
            // Якщо директорію ще не задано
            if (currentDir == null) {
                System.out.print("Введіть шлях до директорії: ");
                String path = sc.nextLine().trim();
                if (path.equalsIgnoreCase("exit")) break;

                File directory = new File(path);
                if (!directory.exists() || !directory.isDirectory()) {
                    System.out.println("❌ Помилка: директорія не існує або недоступна.\n");
                    continue;
                }

                currentDir = directory;
                System.out.println("✅ Обрана директорія: " + currentDir.getAbsolutePath() + "\n");
            }

            // Коли директорія задана
            System.out.print("Введіть розширення файлів (наприклад, .pdf): ");
            String ext = sc.nextLine().trim().toLowerCase();

            if (ext.equalsIgnoreCase("exit")) break;
            if (ext.equalsIgnoreCase("back")) {
                currentDir = null; // повертаємось до вибору директорії
                System.out.println("↩️ Повернення до вибору директорії.\n");
                continue;
            }

            if (!ext.startsWith(".")) {
                System.out.println("⚠️ Формат має починатися з крапки (наприклад, .txt). Спробуйте ще раз.\n");
                continue;
            }
            long start = System.nanoTime();
            int count = pool.invoke(new FileCountTask(currentDir, ext));
            long end = System.nanoTime();

            System.out.println("\n📊 Кількість знайдених файлів з розширенням '" + ext + "': " + count);
            System.out.printf("⏱ Час виконання: %.3f мс%n%n", (end - start) / 1_000_000.0);
        }
        System.out.println("\n👋 Програма завершена. Гарного дня!");
        sc.close();
    }
}