/**
 * моделирование синхрон. и асинхр. системы M|D|1
 */

import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        async();
    }

    private static void async() {
        double lambda = 0.6;
        int M = 10; //кол-во сообщений
        ArrayList<Double> time = new ArrayList<>(); //время между появлениями сообщений
        ArrayList<Double> delays = new ArrayList<>(); //время пребывания сообщений в сист
        delays.add(1.0); //первое сообщ без очереди передаётся
        double D = 1;
        float fullTime = 0;

        for (int i = 0; i < M; i++) {
            double x = -Math.log(Math.random()) / lambda;
            time.add(x);
            fullTime += x;
        }
//        System.out.println("time: " + time);

        for (int i = 1; i < M; i++) {
            double x = time.get(i), d = delays.get(i - 1);
            if (x < d) //если пред. сообщение еще не отправилось
                delays.add(d - x + 1); // вот столько система ещё будет занята
            else
                delays.add(1.0);
            D += delays.get(i);
        }
//        System.out.println("delays: " + delays);

        int[] countN = new int[Math.round(fullTime+1)]; //счетчик кол-ва сообщ в сист в единицу времени
        Arrays.fill(countN, 0);
        double start = 0, end;
        for (int i = 0; i < M; i++) {
            double x = time.get(i);
            start += x;
            end = start + delays.get(i);
            for (int k = new Double(Math.ceil(start)).intValue(); k < Math.ceil(end); k++)
                try {
                    countN[k]++;
                } catch (Exception e){
                    System.out.println(e.toString());
                    System.out.println("countN size = " + countN.length);
                    System.out.println("k = " + k);
                    System.out.println("start = " + start);
                    System.out.println("end = " + end);
                    System.out.println("fullTime = " + fullTime);
                }

        }

        for (int i = 0; i < countN.length; i++)       System.out.print(" ["+i+"] "+ countN[i]);
        System.out.println();
        double d_th = (2 - lambda) / (2 * (1 - lambda));
        double N_th = lambda * d_th;

        System.out.println("d (theor) = " + d_th);
        System.out.println("d (pract) = " + D / M);

        System.out.println("N (theor) = " + N_th);


    }
}
