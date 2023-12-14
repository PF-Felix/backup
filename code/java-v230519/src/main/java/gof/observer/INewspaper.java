package gof.observer;

/**
 * 被订阅者：报纸
 */
public interface INewspaper {
    /**
     * 报纸名称
     */
    String name();

    /**
     * 订阅
     */
    void addPerson(Person person);

    /**
     * 取消订阅
     */
    void removePerson(Person person);

    /**
     * 新刊发布
     */
    void publish(String message);
}
