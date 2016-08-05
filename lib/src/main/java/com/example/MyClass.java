package com.example;

public class MyClass {

    public static void main(String[] arg) {
//        System.out.printf("hello android studio!");
//        new TestThread(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("i am from runadle");
//            }
//        }).start();


//        Sort.simpleSort(Sort.org);
//        Sort.insertSort(new int[]{-5, -10, 100, 30, 20, 4, 46, 7, 9});
//        Sort.bubbleSort(new int[]{-5, -10, 100, 30, 20, 4, 46, 7, 9});
        Sort.fastSort(new int[]{-5, -10, 100, 30, 20, 4, 46, 7, 9});
//        revertString();
    }


    static class TestThread extends Thread {
        public TestThread(Runnable target) {
            super(target);
        }

        @Override
        public void run() {
            System.out.println("i am from thead");
            super.run();

        }
    }

    public static class Sort {

        public static void simpleSort(int[] org) {
            int tmp;
            int minxPosition = 0;
            for (int i = 0; i < org.length; i++) {
                tmp = org[i];
                for (int j = i; j < org.length; j++) {
                    if (org[j] <= tmp) {
                        tmp = org[j];
                        minxPosition = j;
                    }
                }
                org[minxPosition] = org[i];
                org[i] = tmp;
            }
            print(org);
        }

        public static void bubbleSort(int[] org) {
            for (int i = org.length - 1; i >= 0; i--) {
                for (int j = 0; j < i; j++) {
                    int tmp = org[j];
                    if (org[j] > org[j + 1]) {
                        org[j] = org[j + 1];
                        org[j + 1] = tmp;
                    }
                }
            }
            print(org);
        }

        public static void fastSort(int[] org) {
            inherate(org, 0, org.length - 1);
            print(org);
        }

        private static void inherate(int[] org, int low, int high) {
            if (low < high) {
                int middle = getMiddle(org, low, high);
                inherate(org, low, middle - 1);
                inherate(org, middle + 1, high);
            }
        }

        private static int getMiddle(int[] org, int low, int high) {
            int tmp = org[0];
            while (low < high) {
                while (low < high && org[high] >= tmp) {
                    high--;
                }
                org[low] = org[high];
                while (low < high && org[low] <= tmp) {
                    low++;
                }
                org[high] = org[low];
            }
            org[low] = tmp;
            return low;
        }

        public static void insertSort(int[] org) {
            int current;
            for (int i = 1; i < org.length; i++) {
                current = org[i];
                int j = i - 1;
                for (; j >= 0 && org[j] > current; j--) {
                    org[j + 1] = org[j];
                }
                org[j + 1] = current;
            }

            int k = 0;
            int l = org.length - k - 1;
            for (; k != l; ) {
                int tmp = org[k];
                org[k] = org[l];
                org[l] = tmp;
                k++;
                l = org.length - 1 - k;
            }
            print(org);
        }
    }

    static void revertString() {
        String s = "abcdefg";
        StringBuilder builder = new StringBuilder();
        for (int i = s.length() - 1; i >= 0; i--) {
            builder.append(s.charAt(i));
        }
        System.out.println(builder.toString());
    }


    static void print(int[] org) {
        StringBuilder builder = new StringBuilder();
        for (int idx : org) {
            builder.append(idx + " ");
        }
        System.out.println(builder.toString());
    }
}
