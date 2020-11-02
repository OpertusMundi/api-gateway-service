package eu.opertusmundi.web.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.web.model.EnumEventLevel;
import eu.opertusmundi.web.model.EventRecord;

@Entity(name = "Event")
@Table(schema = "logging", name = "log4j_message")
public class EventEntity {

    @Id
    @Column(name = "id", updatable = false)
    @SequenceGenerator(sequenceName = "log4j_message_id_seq", name = "log4j_message_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "log4j_message_id_seq", strategy = GenerationType.SEQUENCE)
    long id = -1L;

    @NotNull
    @Column(name = "`application`", nullable = false)
    String application;

    @Column(name = "generated")
    ZonedDateTime generated;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "`level`")
    EnumEventLevel level;

    @Column(name = "`message`")
    String message;

    @Column(name = "`throwable`")
    String throwable;

    @Column(name = "`logger`")
    String logger;

    @Column(name = "`client_address`")
    String clientAddress;

    @Column(name = "`username`")
    String userName;

    public EventEntity() {
    }

    public long getId() {
        return this.id;
    }

    public String getApplication() {
        return this.application;
    }

    public ZonedDateTime getGenerated() {
        return this.generated;
    }

    public EnumEventLevel getLevel() {
        return this.level;
    }

    public String getMessage() {
        return this.message;
    }

    public String getThrowable() {
        return this.throwable;
    }

    public String getLogger() {
        return this.logger;
    }

    public String getClientAddress() {
        return this.clientAddress;
    }

    public String getUserName() {
        return this.userName;
    }

    public EventRecord toEventRecord() {
        final EventRecord record = new EventRecord();

        record.setClientAddress(this.clientAddress);
        record.setCreatedOn(this.generated);
        record.setLevel(this.level);
        record.setMessage(this.message);
        record.setModule(this.application);
        record.setUserName(this.userName);

        return record;
    }

}
