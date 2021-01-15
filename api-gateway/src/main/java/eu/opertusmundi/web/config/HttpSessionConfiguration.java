package eu.opertusmundi.web.config;

import org.springframework.context.annotation.Profile;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

@Profile({ "production", "development" })
@EnableJdbcHttpSession(tableName = "web.spring_session")
public class HttpSessionConfiguration {

}
