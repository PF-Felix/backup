package gof.observer;

public class Student extends Person {
    @Override
    public void rss(INewspaper INewspaper) {
        INewspaper.addPerson(this);
    }
}
