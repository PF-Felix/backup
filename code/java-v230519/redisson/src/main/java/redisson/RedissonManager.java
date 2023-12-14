package redisson;

import org.redisson.Redisson;
import org.redisson.config.Config;

public class RedissonManager {
    private static final Config config = new Config();
    private static final Redisson redisson;

    static{
        config.useSingleServer().setAddress("127.0.0.1:6379");
        redisson = (Redisson) Redisson.create(config);
    }

    public static Redisson getRedisson(){
        return redisson;
    }
}
