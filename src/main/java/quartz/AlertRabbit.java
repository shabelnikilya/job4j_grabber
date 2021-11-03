package quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
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
        int value = -1;
        try {
            value = Integer.parseInt(getProperties(key));
        } catch (NumberFormatException nfe) {
            LOG.error("Check the values in rabbit.properties, by key - rabbit.interval");
        }
        return value;
    }

    public static Connection initConnection() throws ClassNotFoundException, SQLException {
        Class.forName(getProperties("jdbc.driver"));
        return DriverManager.getConnection(getProperties("jdbc.url"),
                getProperties("jdbc.username"),
                getProperties("jdbc.password"));
    }

    public static void main(String[] args) {
        try (Connection connection = initConnection()) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("connection", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(ifValidGetTime("rabbit.interval"))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(ifValidGetTime("main.time"));
            scheduler.shutdown();
        } catch (SchedulerException | InterruptedException
                | SQLException | ClassNotFoundException e) {
            LOG.error("Stack exception: ", e);
        }
    }

    public static class Rabbit implements Job {

        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try (PreparedStatement st = connection.prepareStatement("insert into rabbit(created_date) values (?);")) {
                st.setTimestamp(1,  new Timestamp(System.currentTimeMillis()));
                st.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}