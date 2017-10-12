/**
 * моделирование синхрон. и асинхр. системы M|D|1
 * + лаба3 - доступ с разделением времени (Time Division Multiple Access)
 */

import java.io.IOException;
import java.util.ArrayList;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

public class Main {
    public static void main(String[] args) throws IOException {
        double[] lambda = {0.01, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.99};
        int[] subscribers = {1, 2, 3, 4, 5, 7, 9, 10, 20, 50};
        double[][] data = new double[subscribers.length + 1][lambda.length];
        int j = 0;
        XYChart chart = new XYChart(700, 600);
        XYChart chartN = new XYChart(700, 600);
        double[][] dataN = new double[subscribers.length + 1][lambda.length];

        for (double l : lambda) {
            //подаем одинаковый поток
            int count = 100000;
            double x = 0;
            ArrayList<Double> time = new ArrayList<>();
            ArrayList<ArrayList<Message>> times = new ArrayList<>();
            for (int k = 0; k < subscribers.length; k++)
                times.add(new ArrayList<Message>());

            for (int i = 0; i < count; i++) { //создание потока
                x = -Math.log(Math.random()) / l;
                time.add(x);
                for (int k = 0; k < subscribers.length; k++) {
                    int M = subscribers[k];
                    int a = Math.round(new Double((M - 1) * Math.random()).floatValue());
                    times.get(k).add(new Message(x, a));
                }
            }

            dataN[0][j] = sync(l, time, count).N;
            for (int k = 0; k < subscribers.length; k++) {
                int M = subscribers[k];
                Data temp = TDMA(l, M, times.get(k), count);
                data[k + 1][j] = temp.delay;
                dataN[k + 1][j] = temp.N;
            }
            j++;
        }

        //график задержки
        /*
        chart.addSeries("sync system", lambda, data[0]);
        for (int k = 0; k < subscribers.length; k++)
            chart.addSeries("TDMA, M = " + subscribers[k], lambda, data[k + 1]);
        chart.setTitle("delays");
        chart.setXAxisTitle("Lambda");
        chart.setYAxisTitle("d");
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setYAxisLogarithmic(true);
        BitmapEncoder.saveBitmap(chart, "delays", BitmapEncoder.BitmapFormat.BMP);
        new SwingWrapper(chart).displayChart();// Show it
        */
        //график среднего кол-ва сообщений
        chartN.addSeries("sync system", lambda, dataN[0]);
        for (int k = 0; k < subscribers.length; k++)
            chartN.addSeries("TDMA, M = " + subscribers[k], lambda, dataN[k + 1]);
        chartN.setTitle("Messages");
        chartN.setXAxisTitle("Lambda");
        chartN.setYAxisTitle("N");
        chartN.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chartN.getStyler().setYAxisLogarithmic(true);
        BitmapEncoder.saveBitmap(chartN, "N", BitmapEncoder.BitmapFormat.BMP);
        new SwingWrapper(chartN).displayChart();
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

    private static Data sync(double lambda, ArrayList<Double> t, int count) {
        int M = count; //кол-во сообщений
        ArrayList<Double> time = t;//new ArrayList<>(); //время между появлениями сообщений
        ArrayList<Double> delays = new ArrayList<>(); //время пребывания сообщений в сист
        float fullTime = 0;
        double D = 1; // общая задержка

        for (int i = 0; i < M; i++) {
//            double x = -Math.log(Math.random()) / lambda;
//            time.add(x);
            double x = time.get(i);//временно!!!
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

//        System.out.println("\nSync system. Lambda = " + lambda);
//        System.out.println("d (theory) = " + d_th);
//        System.out.println("d (pract) = " + D / M);

//        System.out.println("N (theory) = " + N_th);
//        System.out.println("N (pract) = " + N / countN.size());
        return new Data(D / M, N / countN.size());
    }

    //доступ с разделением времени, M -кол-во абонентов
    private static Data TDMA(double lambda, int M, ArrayList<Message> t, int count) {

        int Mes = count; //кол-во сообщений
        ArrayList<Message> time = t;//new ArrayList<>(); //время между появлениями сообщений
        ArrayList<Double> delays = new ArrayList<>(); //время пребывания сообщений в сист
        double[] end = new double[M]; //время выхода последнего сообщения для каждого Абонента
        double N = 0; //кол-во сообщ. в системе


       /* for (int i = 0; i < Mes; i++) {
            double x = -Math.log(Math.random()) / lambda; // промежуток времени перед этим сообщением
            int a = Math.round(new Double((M - 1) * Math.random()).floatValue()); // номер абонента, у которого это сообщение
            time.add(new Message(x, a));
        }*/
//        System.out.println("time: " + time);

        double start = 0;
        // считаем задержку
        for (int i = 0; i < Mes; i++) {
            double x = time.get(i).time, d, D1, D2;
            int target = time.get(i).subscriber; //номер нужного нам окна
            int window; // номер текущего окна

            start += x;
            D1 = Math.ceil(start) - start;
            if (D1 == 0) D1 = 1; //почти невозможная ситуация
            window = new Double(Math.floor(start)).intValue() % M;
            if (window >= target) {
                D2 = M - 1 - window + target;
            } else { //if (window < target) {
                D2 = target - 1 - window;
            }
            d = D1 + D2 + 1; // общая задержка
            if ((start + d) <= end[target]) //если нужное окно занято
                d = end[target] + M - start;
            delays.add(d);

            end[target] = start + d;
            N = N + Math.ceil(d); //+= сколько занимает окон

//            System.out.println(i);
//            System.out.println("start = " + start);
//            System.out.println("window № " + window);
//            System.out.println("target = " + target);
//            System.out.println("end = " + end[window]);
//            System.out.println("d = " + d);
//            System.out.println();
        }
//        System.out.println("delays: " + delays);

        double D = 0;
        for (Double d : delays) D += d;
//        System.out.println("d (pract) = " + D / Mes);
        double cur, max = 0;
        for (int i = 0; i < M; i++) {
            cur = end[i];
            if (cur > max) max = cur; //number of frames
        }
        N = N / max; //среднее кол-во сообщений в системе

        return new Data(D / Mes, N);
    }


    private static class Message {
        double time; //время между появлениями сообщений
        int subscriber; //номер абонента

        Message(double d, int l) {
            time = d;
            subscriber = l;
        }

        public String toString() {
            return ("<" + time + ", " + subscriber + ">");
        }
    }

    //для возращаемых значений
    private static class Data {
        double delay;
        double N;

        Data(double delay, double N) {
            this.delay = delay;
            this.N = N;
        }
    }
}
