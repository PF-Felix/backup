package gof.observer;

/**
 * 观察者模式
 */
public class Client {
    public static void main(String[] args) {
        PeoplesDaily peoplesDaily = new PeoplesDaily();
        GlobalTimes globalTimes = new GlobalTimes();

        System.out.println("1");
        Person student1 = new Student();
        student1.rss(peoplesDaily);

        System.out.println("2");
        Person student2 = new Student();
        student2.rss(peoplesDaily);
        student2.rss(globalTimes);

        System.out.println("3");
        Person teacher = new Teacher();
        teacher.rss(peoplesDaily);

        System.out.println("4");
        Person person = new Person();
        person.rss(globalTimes);

        peoplesDaily.publish("4月新刊发布");
        globalTimes.publish("4月新刊发布");

        System.out.println("5");
        teacher.rssReverse(peoplesDaily);
        teacher.rssReverse(globalTimes);

        peoplesDaily.publish("5月新刊发布");
        globalTimes.publish("5月新刊发布");
    }
}
