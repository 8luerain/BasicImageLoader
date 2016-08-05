package com.example.net;

import java.util.Arrays;
import java.util.Scanner;
import static java.lang.System.*;

/**
 * Project: ImageLoader.
 * Data: 2016/8/3.
 * Created by 8luerain.
 * Contact:<a href="mailto:8luerain@gmail.com">Contact_me_now</a>
 */
public class section1 {
    public static void main(String[] args) {
        String s = "abcd";
        int[] scr = new int[]{1, 2, 3, 4, 5};
        int[] ints = Arrays.copyOf(scr, 10);
        for (int i : ints) {
            System.out.print(i);
            out.println();
        }

        int[] fix = new int[1];
        fix = new int[]{1, 2, 3};
        System.out.println("fix array size[" + fix.length + "]");
        System.out.println("fix array" + Arrays.toString(fix));

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            System.out.println(scanner.nextLine());
        }
    }
}
