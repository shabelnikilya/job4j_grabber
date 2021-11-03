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

    public static Properties getProperties() {
        Properties config = new Properties();
        try (InputStream in = AlertRabbit
                .class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            config.load(in);
        } catch (IOException io) {
            LOG.error("I/O exception", io);
        }
        return config;
    }

    public static Connection initConnection(Properties properties) throws ClassNotFoundException, SQLException {
        Class.forName(properties.getProperty("jdbc.driver"));
        return DriverManager.getConnection(properties.getProperty("jdbc.url"),
                properties.getProperty("jdbc.username"),
                properties.getProperty("jdbc.password"));
    }

    public static void main(String[] args) {
        Properties properties = getProperties();
        try (Connection connection = initConnection(properties)) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("connection", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(properties.getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(Integer.parseInt(properties.getProperty("main.time")));
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