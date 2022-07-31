package org.kramadi.bot.MySQL;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "search", schema = "bot")
public class SearchEntity extends DataEntity {
    private int id;
    private String name;
    private Date creationDate;
    private Date lastSearchDate;
    private String keyword;
    private Integer dayInterval;
    private byte state;
    private PlatformEntity platformByPlatformId;
    private UserEntity userByUserId;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "name", nullable = true, length = 100)

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "creation_date", nullable = false)
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Basic
    @Column(name = "last_search_date", nullable = true)
    public Date getLastSearchDate() {
        return lastSearchDate;
    }

    public void setLastSearchDate(Date lastSearchDate) {
        this.lastSearchDate = lastSearchDate;
    }

    @Basic
    @Column(name = "keyword", nullable = false, length = 100)
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Basic
    @Column(name = "`interval`", nullable = true)
    public Integer getDayInterval() {
        return dayInterval;
    }

    public void setDayInterval(Integer dayInterval) {
        this.dayInterval = dayInterval;
    }

    @Basic
    @Column(name = "state", nullable = false)
    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchEntity that = (SearchEntity) o;

        if (id != that.id) return false;
        if (state != that.state) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (creationDate != null ? !creationDate.equals(that.creationDate) : that.creationDate != null) return false;
        if (lastSearchDate != null ? !lastSearchDate.equals(that.lastSearchDate) : that.lastSearchDate != null) return false;
        if (keyword != null ? !keyword.equals(that.keyword) : that.keyword != null) return false;
        if (dayInterval != null ? !dayInterval.equals(that.dayInterval) : that.dayInterval != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (lastSearchDate != null ? lastSearchDate.hashCode() : 0);
        result = 31 * result + (keyword != null ? keyword.hashCode() : 0);
        result = 31 * result + (dayInterval != null ? dayInterval.hashCode() : 0);
        result = 31 * result + (int) state; return result;


    }


    @ManyToOne
    @JoinColumn(name = "platform_id", referencedColumnName = "id", nullable = false)
    public PlatformEntity getPlatformByPlatformId() {
        return platformByPlatformId;
    }

    public void setPlatformByPlatformId(PlatformEntity platformByPlatformId)
    {
        this.platformByPlatformId = platformByPlatformId;
    }

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    public UserEntity getUserByUserId() {
        return userByUserId;
    }

    public void setUserByUserId(UserEntity userByUserId) {
        this.userByUserId = userByUserId;
    }
}
