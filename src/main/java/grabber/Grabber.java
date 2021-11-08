package grabber;

import html.SqlRuParse;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import utils.DateTimeParser;
import utils.SqlRuDateTimeParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {

    private final Properties cfg = new Properties();

    public Store store() {
        return new PsqlStore(cfg);
    }

    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    public void cfg() throws IOException {
        try (InputStream in = Grabber
                .class.getClassLoader()
                .getResourceAsStream("app.properties")) {
            cfg.load(in);
        }
    }

    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(Integer.parseInt((cfg.getProperty("time"))))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public static class GrabJob implements Job {

        @Override
        public void execute(JobExecutionContext context) {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
                List<Post> parseSqlRu = parse.list("https://www.sql.ru/forum/job-offers");
                List<Post> postStoreSql = store.getAll();
                parseSqlRu.stream()
                        .filter(x -> x.getTitle().toLowerCase().contains("java")
                                && !x.getTitle().toLowerCase().contains("script"))
                        .filter(x -> {
                            boolean rsl = true;
                            for (Post p : postStoreSql) {
                                if (p.getLink().equals(x.getLink())) {
                                    rsl = false;
                                    break;
                                }
                            }
                            return rsl;
                        })
                        .forEach(store::save);
        }

        public static void main(String[] args) throws IOException, SchedulerException {
            Grabber grab = new Grabber();
            grab.cfg();
            Scheduler scheduler = grab.scheduler();
            Store store = grab.store();
            DateTimeParser dateTimeParser = new SqlRuDateTimeParser();
            grab.init(new SqlRuParse(dateTimeParser), store, scheduler);
        }
    }
}
