package proj.research.colortools.util;


import org.junit.Test;


public class ColorUtilsTest {

    @Test
    public void testPost(){
        String res = Local.post("http://www.baidu.com", "{}");
        System.out.println(res);
    }

}
