package quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    private static final Logger LOG = LoggerFactory.getLogger(AlertRabbit.class.getName());

    public static String getProperties(String key) {
        Properties config = new Properties();
        try (InputStream in = AlertRabbit
                                .class.getClassLoader()
                                .getResourceAsStream("rabbit.properties")) {
            config.load(in);
        } catch (IOException io) {
            LOG.error("I/O exception", io);
        }
        return config.getProperty(key);
    }

    public static int ifValidGetTime(String key) {
        int value = 0;
        try {
            value = Integer.parseInt(getProperties(key));
        } catch (NumberFormatException nfe) {
            LOG.error("Check the values in rabbit.properties, by key - rabbit.interval");
        }
        return value;
    }

    public static void main(String[] args) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(ifValidGetTime("rabbit.interval"))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            LOG.error("Stack exception: ", se);
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext jobExecutionContext) {
            System.out.println("Rabbit runs here ...");
        }
    }
}
