package org.example;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello and welcome!");
        Test test = new Test();
        int i = 0;
        while (true) {
            int j = i + 1;
            System.out.println(String.format("%d + %d = ", i, j) + test.sum(i, j));
            i++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("exception.....");
            }
        }

    }
}