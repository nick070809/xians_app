package org.nick;

import org.kx.util.FileUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Hello world!
 * nohup /opt/taobao/java/bin/java -jar /home/-------.skx/xian_app.jar > nohup.out 2>&1 &
 * sudo -u root kiil -9 89277
 */
@SpringBootApplication
@RestController
//@ImportResource(locations = {"spring.hsf.xml"})
public class ServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }






    @RequestMapping("/hi")
    public String home() {
        return "hi,bb891008";
    }


    @RequestMapping("/hi2")
    public String hi2() throws IOException {
//getResource("/").toString()
        return FileUtil.readFile(ServiceApplication.class.getResource("/").toString()+"static/main.css");
    }



    @RequestMapping("/shutdown")
    public String shutdown() {
        System.exit(-1);
        return "shutdown";
    }




}
