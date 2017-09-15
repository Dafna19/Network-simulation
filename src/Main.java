/**
 * моделирование синхрон. и асинхр. системы M|D|1
 */

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        double[] lambda = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        for (double l : lambda) {
            async(l);
            sync(l);
        }
    }

    private static void async(double lambda) {
        int M = 100000; //кол-во сообщений
        ArrayList<Double> time = new ArrayList<>(); //время между появлениями сообщений
        ArrayList<Double> delays = new ArrayList<>(); //время пребывания сообщений в сист
        delays.add(1.0); //первое сообщ без очереди передаётся
        double D = 1; // общая задержка
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

        ArrayList<Integer> countN = new ArrayList<>(Math.round(fullTime + 1)); //счетчик кол-ва сообщ в сист в единицу времени
        for (int i = 0; i < Math.round(fullTime + 1); i++) countN.add(i, 0);

        double start = 0, end;
        for (int i = 0; i < M; i++) {
            double x = time.get(i);
            start += x;
            end = start + delays.get(i);
            for (int k = new Double(Math.ceil(start)).intValue(); k < Math.ceil(end); k++)
                try {
                    countN.set(k, countN.get(k) + 1); // увеличиваем счетчик
                } catch (Exception e) { // если вышли за границы массива
                    for (int s = countN.size(); s < Math.ceil(end); s++) // увеличиваем размер
                        countN.add(s, 0);
                    countN.set(k, countN.get(k) + 1); // теперь уже увеличиваем счетчик
                }

        }

        double d_th = (2 - lambda) / (2 * (1 - lambda));
        double N_th = lambda * d_th;
        double N = 0;
        for (Integer i : countN) N += i;

        System.out.println("\nAsync system. Lambda = " + lambda);
        System.out.println("d (theory) = " + d_th);
        System.out.println("d (pract) = " + D / M);

        System.out.println("N (theory) = " + N_th);
        System.out.println("N (pract) = " + N / countN.size());


    }

    private static void sync(double lambda) {
        int M = 100000; //кол-во сообщений
        ArrayList<Double> time = new ArrayList<>(); //время между появлениями сообщений
        ArrayList<Double> delays = new ArrayList<>(); //время пребывания сообщений в сист
        float fullTime = 0;
        double D = 1; // общая задержка

        for (int i = 0; i < M; i++) {
            double x = -Math.log(Math.random()) / lambda;
            time.add(x);
            fullTime += x;
        }

        ArrayList<Integer> countN = new ArrayList<>(Math.round(fullTime + 1)); //счетчик кол-ва сообщ в сист в единицу времени
        for (int i = 0; i < Math.round(fullTime + 1); i++) countN.add(i, 0);

        delays.add(1 + Math.ceil(time.get(0)) - time.get(0));//обработка 1го сообщ
        int w = new Double(Math.ceil(time.get(0))).intValue();
        countN.set(w, countN.get(w) + 1); // увеличиваем счетчик - 1е сообщ только в одно окно попадает

        double start = time.get(0);
        for (int i = 1; i < M; i++) {
            double x = time.get(i), d = delays.get(i - 1);
            start += x;
            if (x < d) //если пред. сообщение еще не отправилось
                delays.add(d - x + 1); // вот столько система ещё будет занята
            else
                delays.add(1 + Math.ceil(start) - start); // учитывается ожидание начала окна
            D += delays.get(i);

            double end = start + delays.get(i);
            for (int k = new Double(Math.ceil(start)).intValue(); k < Math.ceil(end); k++)
                try {
                    countN.set(k, countN.get(k) + 1); // увеличиваем счетчик
                } catch (Exception e) { // если вышли за границы массива
                    for (int s = countN.size(); s < Math.ceil(end); s++) // увеличиваем размер
                        countN.add(s, 0);
                    countN.set(k, countN.get(k) + 1); // теперь уже увеличиваем счетчик
                }
        }

        double d_th = (2 - lambda) / (2 * (1 - lambda)) + 0.5;
        double N_th = (lambda * (2 - lambda)) / (2 * (1 - lambda));
        double N = 0;
        for (Integer i : countN) N += i;

        System.out.println("\nSync system. Lambda = " + lambda);
        System.out.println("d (theory) = " + d_th);
        System.out.println("d (pract) = " + D / M);

        System.out.println("N (theory) = " + N_th);
        System.out.println("N (pract) = " + N / countN.size());
    }

    private static int I(int N) {
        if (N > 0)
            return 1;
        return 0;
    }
}
