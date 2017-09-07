/**
 * моделирование синхрон. и асинхр. системы M|D|1
 */

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        sync();
    }

    public static void sync() {
        double lambda = 0.5;
        int M = 10; //кол-во сообщений
        ArrayList<Double> time = new ArrayList<>(); //время между появлениями сообщений

        for (int i = 0; i < M; i++) {
            double x = - Math.log(Math.random())/lambda;
            time.add(x);
        }
        System.out.println("time: " + time);


    }
}
