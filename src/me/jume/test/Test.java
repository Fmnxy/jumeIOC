package me.jume.test;

import me.jume.domain.Dept;
import me.jume.ioc.ApplicationContext;

public class Test {
    public static void main(String[] args) {
        ApplicationContext ctx = new ApplicationContext("beans.xml");
        Dept dept = (Dept) ctx.getBean("dept");
        Dept dept1 = (Dept) ctx.getBean("dept");
        System.out.println(dept==dept1);
    }
}
