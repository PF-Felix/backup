package gof.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * 环球时报
 */
public class GlobalTimes implements INewspaper {

    private static final List<Person> people = new ArrayList<>();

    @Override
    public void addPerson(Person person){
        people.add(person);
        System.out.println("hello 欢迎小主      ------" + name());
    }

    @Override
    public void removePerson(Person person){
        if (people.remove(person)) {
            System.out.println("再见咯，欢迎再来      ------" + name());
        }
    }

    @Override
    public void publish(String message){
        people.forEach(e-> e.listen(name() + message));
    }

    @Override
    public String name() {
        return "【环球时报】";
    }
}
