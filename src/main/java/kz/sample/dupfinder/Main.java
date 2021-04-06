package kz.sample.dupfinder;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
@Slf4j
public class Main implements CommandLineRunner {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage:\n" +
                    "java -jar dupfinder.jar path_to_dir");
            return;
        }
        SpringApplication application = new SpringApplication(Main.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }

    @Override
    public void run(String... args) throws Exception {

        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        LongTaskTimer timer = LongTaskTimer.builder("finder").register(meterRegistry);
        LongTaskTimer.Sample sample = timer.start();

        String initialDir = args[0];

        DupFinder dupFinder = new DupFinder(initialDir);
        dupFinder.searchForDuplicates();

        long elapsed = sample.stop();
        log.info("Processing time {}[ms]", TimeUnit.NANOSECONDS.toMillis(elapsed));

        System.out.println("See results in the log");

    }


}

