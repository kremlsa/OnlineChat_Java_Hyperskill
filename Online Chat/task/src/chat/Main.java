package chat;

import java.util.Scanner;

public class Main {
    public static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        while(sc.hasNextLine()) {
            String input = sc.nextLine();
            if (input.contains("sent")) {
                System.out.println(input.replaceFirst(" sent", ":"));
            }
        }
    }
}