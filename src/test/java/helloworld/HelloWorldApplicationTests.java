package helloworld;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HelloWorldApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testMethod(){
        String s = "a b  c";
        String[] arr = s.trim().split(" ");
        for (String word : arr) {
            System.out.println(word);
        }
    }

}
