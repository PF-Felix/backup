package gof.observer;

public class Teacher extends Person {
    @Override
    public void rss(INewspaper INewspaper) {
        INewspaper.addPerson(this);
    }
}
