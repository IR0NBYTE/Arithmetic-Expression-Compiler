import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.Stack;

public class Compiler {
    private static String expression;

    public static void main(String[] args) throws SyntaxeError, FileNotFoundException {
        Scanner sc = new Scanner(System.in);
        System.out.print(">>> ");
        expression = sc.nextLine();
        if (!checkSyntaxe(expression))
            throw new SyntaxeError("Syntaxe Error !");
        File file = new File("Assembly.txt");
        FileOutputStream fos = new FileOutputStream(file);
        PrintStream ps = new PrintStream(fos);
        System.setOut(ps);
        startAsm();
        doJob(expression);
        exitSyscall();
    }

    private static void doJob(String expression) {
        Stack<Integer> saver = new Stack<Integer>();
        Stack<Character> operations = new Stack<Character>();

        int counter = 0;
        boolean flag = true;
        for(int i = 0; i < expression.length(); i++) {
            if (Character.isDigit(expression.charAt(i))) {
                saver.push(expression.charAt(i) - '0');
            } else {
                if (expression.charAt(i) == '+' || expression.charAt(i) == '-') {
                    if (expression.charAt(i) == '-' && Character.isDigit(expression.charAt(i + 1))) {
                        saver.push(-(expression.charAt(i + 1) - '0'));
                        i++;
                        operations.push('+');
                    } else
                        operations.push(expression.charAt(i));
                }
                else
                    if (expression.charAt(i) == ')') {
                        char oper = operations.pop();
                        int element2 = saver.pop();
                        int element1 = saver.pop();
                        if (oper == '+') {
                            addAsm(element1, element2, flag);
                            element2 += element1;
                            flag = false;
                            saver.push(element2);
                        }
                        if (oper == '-') {
                            subAsm(element1, element2, flag);
                            element1 -= element2;
                            flag = false;
                            saver.push(element1);
                        }
                    }
            }
        }
        while(!operations.empty()) {
            char oper = operations.pop();
            int element2 = saver.pop();
            int element1 = saver.pop();
            if (oper == '+') {
                addAsm(element1, element2, flag);
                element2 += element1;
                flag = false;
                saver.push(element2);
            }
            if (oper == '-') {
                subAsm(element1, element2, flag);
                element1 -= element2;
                flag = false;
                saver.push(element1);
            }

        }
        int ans = saver.pop();
    }



    private static boolean checkSyntaxe(String expression) {
        int counterNum = 0;
        int counterOper = 0;
        int bracketCounter = 0;

        for(int i = 0; i < expression.length(); i++) {
            if (expression.charAt(i) >= '0' && expression.charAt(i) <= '9')
                counterNum++;
            else
                if (expression.charAt(i) == '+' || expression.charAt(i) == '-')
                    counterOper++;
                else
                    if (expression.charAt(i) == ')')
                        bracketCounter--;
                    else
                        if (expression.charAt(i) == '(')
                            bracketCounter++;
                        else
                            return false;

            if (bracketCounter < 0)
                return false;
        }
        for(int i = 0; i < expression.length() - 1; i++) {
            if (expression.charAt(i) == expression.charAt(i + 1) && expression.charAt(i) != ')' && expression.charAt(i) != '(')
                return false;
        }
        for(int i = 1; i < expression.length(); i++) {
            if ( (expression.charAt(i) == '+' || expression.charAt(i) == '-') && !Character.isDigit(expression.charAt(i - 1)) && expression.charAt(i - 1) != ')')
                return false;
        }
        if (bracketCounter != 0)
            return false;
        if (counterNum - counterOper != 1)
            return false;
        return true;
    }


    private static void addAsm(int saved, int num, boolean flag) {
        if (flag) {
            emitline("mov $" + saved + ",%eax");
            emitline("mov $eax,$ebx");
            emitline("mov $" + num + ",$eax");
            emitline("add $ebx,$eax");
            emitline("neg $eax");
        } else {
            emitline("mov $eax,$ecx");
            emitline("mov $" + saved + ",%eax");
            emitline("mov $eax,$ebx");
            emitline("mov $" + num + ",$eax");
            emitline("add $ebx,$eax");
            emitline("add $ecx,%eax");
            emitline("neg $eax");
        }
    }
    private static void subAsm(int num1, int num2, boolean flag) {
        if (flag) {
            emitline("mov $" + num2 + ",%eax");
            emitline("mov $eax,$ebx");
            emitline("mov $" + num1 + ",$eax");
            emitline("sub $ebx,$eax");
        } else {
            emitline("mov $eax,$ecx");
            emitline("mov $" + num2 + ",%eax");
            emitline("mov $eax,$ebx");
            emitline("mov $" + num1 + ",$eax");
            emitline("sub $ebx,$eax");
            emitline("add $ecx,%eax");
        }
    }
    private static void emitline(String s) {
        System.out.println("    " + s);
    }

    private static void startAsm() {
        System.out.println(".globl _start\n.start:");
    }
    private static void exitSyscall() {
        emitline("mov $1,$eax");
        emitline("mov $0,$ebx");
        emitline("int 0x80");
    }

}

class SyntaxeError extends Exception {
    SyntaxeError(String reason) { super(reason); }
}