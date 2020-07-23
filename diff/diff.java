import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import javax.swing.filechooser.FileSystemView;


public class GetDistinctByLine {
    public static void main(String[] args) {
        File outFile = new File(FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath().concat(File.separator).concat("out.txt"));
        List<String> list_1, list_2;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please input file path 1:");
        File file_1 = new File(scanner.nextLine());
        System.out.println("Please input file path 2:");
        File file_2 = new File(scanner.nextLine());
        scanner.close();
        if (file_1.exists() && file_2.exists()) {
            list_1 = getStringContext(file_1);
            list_2 = getStringContext(file_2);
            ArrayList<String> list = new ArrayList<>();
            if (list_1 != null && list_2 != null) {
                list_1.stream().filter(list_2::contains).forEach(list::add);
                list_1.stream().filter(s -> !list_2.contains(s)).forEach(s -> list.add(file_1.getName().concat("\t>>>>>>>>>>>>>> \t").concat(s)));
                list_2.stream().filter(s -> !list_1.contains(s)).forEach(s -> list.add(file_2.getName().concat("\t>>>>>>>>>>>>>> \t").concat(s)));
                if (writeContext(outFile, list)) {
                    System.out.println("Success output to ".concat(outFile.getAbsolutePath()).concat("."));
                    System.exit(0);
                }
            }
        }
        System.out.println("Error.");
    }

    private static List<String> getStringContext(File file) {
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                List<String> list = new ArrayList<>();
                String temp;
                while ((temp = reader.readLine()) != null) {
                    list.add(temp);
                }
                return list;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static boolean writeContext(File file, List<String> content) {
        if (file != null && !content.isEmpty()) {
            try (BufferedWriter write = new BufferedWriter(new FileWriter(file))) {
                for (String s : content) {
                    write.write(s);
                    write.newLine();
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
