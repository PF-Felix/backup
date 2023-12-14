package com.github.gpf.spring.mvc.sourcecode;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

@RequestMapping("/test")
@RestController
public class TestController {

    @InitBinder
    public void a(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    @ModelAttribute("name")
    public String a(String name) {
        return "Tom";
    }

    @ModelAttribute("age")
    public int a() {
        return 18;
    }

    /**
     * <a href="http://localhost:8080/spring_mvc_sourcecode_war_exploded/test/test1?date=2022-03-22&name=jenny&age=20">测试链接</a>
     */
    @GetMapping("/test1")
    public String test1(Date date, String name, @ModelAttribute("age") int age, Model model) {
        return date.toString() + "<br/>" + name + "<br/>" + age + "<br/>" + model.toString();
    }
}
