package mainpack;

import java.io.File;
import java.io.IOException;

public class MainClass {
    public static void main(String[] args) throws IOException {

  // проверка на вызов программы без параметров
        if (args.length == 0) {
            System.out.println("Specify the file");
            return;
        }
        args = new String[]{"example.txt"};
        String filePath = args[0];
        File file = new File(filePath);

       if (!file.isFile()) {
            System.out.println("File \"" + args[0] + "\" doesn't exist or can't be read.");
            System.exit(1);
        }
        new Interpreter(new File(args[0]));
    }

    public static void systemExit(String str) {
        System.out.println(str);
        System.exit(1);
    }
}
