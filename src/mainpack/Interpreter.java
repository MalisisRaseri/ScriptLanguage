package mainpack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Interpreter {
    private final Map<String, String > variables = new HashMap<>();


    public Interpreter(File file) throws IOException {
        read(file);

        System.out.println("-----------------");
        // массив с переменными и их значениями
        for (Map.Entry<String ,String> kv : variables.entrySet()) {
            System.out.println(kv.getKey() + ' ' + kv.getValue());
        }
    }

    // чтение файла
    private void read(File file) throws IOException {
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            String s;
            while ((s = br.readLine()) != null) {
                if (s.isEmpty()) continue;

                StringBuilder word = new StringBuilder();
                char[] charArray = s.toCharArray();
                Character curChar;
                for (int i = 0; i < s.length(); i++) {
                    curChar = charArray[i];

                    if (curChar == null)
                        continue;

                    // если комментарий, то пропускаем строку
                    if (charArray[0] == '#')
                        break;

                    if (String.valueOf(word).equals(Commands.print.toString())) {
                        printString(s.substring(i + 1));
                        break;
                    }

                    if (String.valueOf(word).equals(Commands.set.toString())) {
                        saveSetValue(s.substring(i + 1));
                        break;
                    }

                    if (String.valueOf(word).equals(Commands.input.toString())) {
                        inputValue(s.substring(i + 1));
                        break;
                    }

                    // сформируем ключевое слово
                    if (Character.isLetter(curChar)) {
                        word.append(curChar);
                        if (i != s.length() - 1) continue;
                    }
                }
            }
        }
    }


    // ключевое слово set
    private void saveSetValue(String s) {
        String value = "";
        boolean fVal = false;
        char[] charArray = s.toCharArray();
        Character curChar;
        StringBuilder word = new StringBuilder(),
                wordName = new StringBuilder(),
                finalStr = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            curChar = charArray[i];
            // формируем значение переменной
            if (fVal) {

                if (curChar.equals('='))
                    continue;

                if (curChar.equals('$') || Character.isLetter(curChar)
                        || Character.isDigit(curChar) || curChar.equals('_')) {
                    word.append(curChar);
                    if (!String.valueOf(word).contains("$"))
                        value = String.valueOf(word);
                    if (i!=s.length()) continue;
                }

                if (Character.isWhitespace(curChar)) {
                    if (String.valueOf(word).equals(""))
                        continue;

                    if (!String.valueOf(word).contains("$")) {
                        finalStr.append(word);
                        value = String.valueOf(word);
                        word.delete(0,word.length());
                        continue;
                    }

                    if (variables.containsKey(String.valueOf(word)))
                        finalStr.append(variables.get(String.valueOf(word)));
                    else {
                        MainClass.systemExit("Unknown variable " + word + "!!!");
                    }
                    if (!String.valueOf(word).contains("$"))
                        value = String.valueOf(word);
                    word.delete(0, word.length());
                    continue;
                }

                if (curChar.equals('-') || curChar.equals('+'))
                    finalStr.append(curChar);

                if (Character.isDigit(curChar)) {
                    value = value + curChar;
                    if (i!=s.length()) continue;
                }
            }

            // формируем название переменной
            if (Character.isLetter(curChar) || curChar.equals('$') ||
                    Character.isDigit(curChar) || curChar.equals('_')) {
                wordName.append(curChar);
            } else {
                fVal = true;
                continue;
            }
        }

        finalStr.append(value);
        value = String.valueOf(calculate(String.valueOf(finalStr)));


        // положим в коллекцию переменную со значением
        if (!value.equals(""))
            variables.put(String.valueOf(wordName), value);
    }




    public static int calculate(String str) {
        StringBuilder operandsList = new StringBuilder(),
                strForCalc = new StringBuilder();
        char curChar;
        Deque<Integer> stack = new ArrayDeque<>();;
        StringTokenizer st;

        // сформируем две строки с операндами и числами
        for (int i = 0; i < str.length(); i++) {
            curChar = str.charAt(i);
            if (curChar=='-' || curChar=='+') {
                while (operandsList.length() > 0) {
                    strForCalc.append(" ");
                    break;
                }
                strForCalc.append(" ");
                operandsList.append(curChar);
            }   else {
                strForCalc.append(curChar);
            }
        }

        // добавим операнды к числам в обратном порядке
        while (operandsList.length() > 0) {
            strForCalc.append(" ").append(operandsList.substring(operandsList.length()-1));
            operandsList.setLength(operandsList.length()-1);
        }

        // посчитаем выражение
        int val1,
                val2;
        String curStr;
        st = new StringTokenizer(strForCalc.toString());
        while(st.hasMoreTokens()) {
            curStr = st.nextToken().trim();
            if (curStr.length() == 1) {
                val2 = stack.pop();
                val1 = stack.pop();
                switch (curStr.charAt(0)) {
                    case '+':
                        val1 += val2;
                        break;
                    case '-':
                        val1 -= val2;
                        break;
                    default:
                }
                stack.push(val1);
            } else {
                try {
                    val1 = Integer.parseInt(curStr);
                    stack.push(val1);
                } catch (NumberFormatException e) {
                    MainClass.systemExit("Not correct string for calculate.");
                }
            }
        }
        return stack.pop();
    }


    // ключевое слово print
    private void printString(String s) {
        StringBuilder word = new StringBuilder(),
                finalStr = new StringBuilder();
        char[] charArray = s.toCharArray();
        Character curChar;
        int fScob = 0;
        for (int i = 0; i < s.length(); i++) {
            curChar = charArray[i];

            if (i==s.length()-1) {
                if (!curChar.equals('"'))
                    word.append(curChar);
                if (variables.containsKey(String.valueOf(word)))
                    System.out.print(variables.get(String.valueOf(word)));
                else
                    System.out.print(word);
                break;
            }

            // если первая скобка, то печатаем слово и ставим флаг
            if (curChar.equals('\"')) {
                if (!Character.isWhitespace(curChar)) {
                    if (variables.containsKey(String.valueOf(word)))
                        System.out.print(variables.get(String.valueOf(word)));
                    else
                        System.out.print(word);
                    word.delete(0,word.length());
                }
                fScob++;
                if (fScob==1)
                    continue;
            }

            // создаем слово внутри скобок
            if (fScob == 1) {
                word.append(curChar);
                continue;
            }

            // формируем финальную строку, выходим из скобок
            if (fScob == 2) {
                fScob=0;
                finalStr.append(word);

                word.delete(0, word.length());
                continue;
            }

            // формируем текущее слово
            if (Character.isLetter(curChar) || curChar.equals('$')
                    || Character.isDigit(curChar) || curChar.equals('_')) {
                word.append(curChar);
            }
        }
        // печать резузьтата
        System.out.println(finalStr);
    }


    // ключевое слово input
    private void inputValue(String s) {
        System.out.println(s);
        StringBuilder word = new StringBuilder();
        int flag=0;
        int newValue = 0;
        char[] charArray = s.toCharArray();
        Character curChar;
        for (int i = 0; i < s.length(); i++) {
            curChar = charArray[i];

            if (curChar.equals('\"')) {
                flag++;
                continue;
            }

            if (flag == 1) {
                word.append(curChar);
            }

            if (flag == 2) {
                flag=0;
                System.out.print(word);
                newValue= readToConsole();
                word.delete(0, word.length());
                continue;
            }

            if (flag == 0 && (Character.isLetter(curChar) || Character.isDigit(curChar)
                    || curChar.equals('$') || curChar.equals('_'))) {
                word.append(curChar);
                if (i==s.length()-1) {
                    variables.put(word.toString(), String.valueOf(newValue));
                }
            }
        }
    }


    private Integer readToConsole() {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            return Integer.parseInt(scanner.next());
        }
        return 0;
    }
}
