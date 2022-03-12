package grabber;

import html.SqlRuParse;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import utils.DateTimeParser;
import utils.SqlRuDateTimeParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.*;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {

    private static final Properties CFG = new Properties();

    public Store store() {
        return new PsqlStore(CFG);
    }

    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    public void loadCfg() throws IOException {
        try (InputStream in = Grabber
                .class.getClassLoader()
                .getResourceAsStream("app.properties")) {
            CFG.load(in);
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
                .withIntervalInSeconds(Integer.parseInt((CFG.getProperty("time"))))
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
            List<Post> parseSqlRu = parse.list(CFG.getProperty("parse.site"));
            List<Post> postStoreSql = store.getAll();
            parseSqlRu.stream()
                    .filter(x -> x.getTitle().toLowerCase().contains("java")
                            && !x.getTitle().toLowerCase().contains("javascript"))
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
    }

        public static void main(String[] args) throws IOException, SchedulerException {
            Grabber grab = new Grabber();
            grab.loadCfg();
            Scheduler scheduler = grab.scheduler();
            Store store = grab.store();
            DateTimeParser dateTimeParser = new SqlRuDateTimeParser();
            grab.init(new SqlRuParse(dateTimeParser), store, scheduler);
            grab.web(store);
        }

    private void web(Store store) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(Integer.parseInt(CFG.getProperty("port")))) {
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    try (OutputStream out = socket.getOutputStream()) {
                        out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                        for (Post post : store.getAll()) {
                            out.write(post.toString().getBytes(Charset.forName("Windows-1251")));
                            out.write(System.lineSeparator().getBytes());
                        }
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
