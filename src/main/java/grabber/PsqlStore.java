package grabber;

import html.SqlRuParse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.DateTimeParser;
import utils.SqlRuDateTimeParser;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(PsqlStore.class.getName());

    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
            this.cnn = DriverManager.getConnection(cfg.getProperty("jdbc.url"),
                    cfg.getProperty("jdbc.username"),
                    cfg.getProperty("jdbc.password"));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void save(Post post) {
        try (var statement = cnn.prepareStatement(
                "insert into post(name, text, link, created) values (?, ?, ?, ?);",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception in method - safe", e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (var statement = cnn.prepareStatement("select * from post;")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(createdPost(resultSet));
                }
            }
        } catch (Exception e) {
            LOG.error("Exception in method - getAll", e);
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post findByPost = null;
        try (var statement = cnn.prepareStatement(
                "select name,  link, text, created from post where id = ?"
        )) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    findByPost = createdPost(resultSet);
                }
            }
        } catch (Exception e) {
            LOG.error("Exception in method - findByid", e);
        }
        return findByPost;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    public static Post createdPost(ResultSet in) throws  SQLException {
        return new Post(
                in.getInt("id"),
                in.getString("name"),
                in.getString("link"),
                in.getString("text"),
                in.getTimestamp("created").toLocalDateTime()
        );
    }
}
