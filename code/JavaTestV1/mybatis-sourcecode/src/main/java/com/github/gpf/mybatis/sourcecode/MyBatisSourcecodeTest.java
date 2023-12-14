package com.github.gpf.mybatis.sourcecode;

import com.github.gpf.mybatis.sourcecode.dao.UserDao;
import com.github.pagehelper.PageHelper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import com.github.gpf.mybatis.sourcecode.entity.User;

import java.io.InputStream;
import java.util.List;

/**
 * 帮助阅读源码的类
 */
public class MyBatisSourcecodeTest {
    public static void main(String[] args) throws Exception {
        //获取配置文件
        InputStream in = Resources.getResourceAsStream("com/github/gpf/mybatis/sourcecode/mybatis-config.xml");
        //解析配置文件并获取SqlSessionFactory对象
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(in);
        //根据SqlSessionFactory对象获取SqlSession对象
        SqlSession sqlSession = factory.openSession();
        //通过SqlSession中提供的API方法来操作数据库
        UserDao mapper = sqlSession.getMapper(UserDao.class);
        for (int i = 0; i < 5; i++) {
            System.out.println(mapper.insert(User.builder().name("Tom" + System.currentTimeMillis()).age(10).build()));
            System.out.println(mapper.insert(User.builder().name("Tom" + System.currentTimeMillis()).age(10).build()));
            System.out.println(mapper.insert(User.builder().name("Tom" + System.currentTimeMillis()).age(10).build()));
            System.out.println(mapper.insert(User.builder().name("Tom" + System.currentTimeMillis()).age(10).build()));
            System.out.println(mapper.insert(User.builder().name("Tom" + System.currentTimeMillis()).age(10).build()));
        }
        System.out.println(mapper.queryList());

        PageHelper.startPage(1, 2);
        List<User> users = mapper.queryList();
        System.out.println(users.toString());
        //关闭会话
        sqlSession.close();
    }
}
