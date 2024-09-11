package jimlind.filmlinkd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

import java.util.Timer;
import java.util.TimerTask;

@Slf4j
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        log.info("Starting Things Up!");
        SpringApplication.run(Main.class, args);

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                System.exit(-1);
            }
        };
        timer.schedule(task, 15*60*1000);
    }
}
