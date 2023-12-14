package gof.observer;

/**
 * 订阅者
 */
public class Person {
    /**
     * 订阅
     */
    public void rss(INewspaper INewspaper) {
        INewspaper.addPerson(this);
    };

    /**
     * 解除订阅
     */
    public void rssReverse(INewspaper INewspaper) {
        INewspaper.removePerson(this);
    };

    /**
     * 新刊发布接收消息
     */
    public void listen(String message) {
        System.out.println(message);
    }
}
