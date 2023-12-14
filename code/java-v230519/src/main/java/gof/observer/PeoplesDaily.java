package gof.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * 人民日报
 */
public class PeoplesDaily implements INewspaper {

    private static final List<Person> people = new ArrayList<>();

    @Override
    public void addPerson(Person person){
        people.add(person);
        System.out.println("欢迎您订阅" + name());
    }

    @Override
    public void removePerson(Person person){
        if (people.remove(person)) {
            System.out.println("欢迎您再次订阅本报      ------" + name());
        }
    }

    @Override
    public void publish(String message){
        people.forEach(e-> e.listen(name() + message));
    }

    @Override
    public String name() {
        return "【人民日报】";
    }
}
