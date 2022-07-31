package org.kramadi.bot.MySQL;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable

public class SearchHasPlatformId implements Serializable {
    private static final long serialVersionUID = -2574065717491802709L;
    @Column(name = "search_id", nullable = false)
    private Integer searchId;
    @Column(name = "platform_id", nullable = false)
    private Integer platformId;

    public Integer getPlatformId() {
        return platformId;
    }

    public void setPlatformId(Integer platformId) {
        this.platformId = platformId;
    }

    public Integer getSearchId() {
        return searchId;
    }

    public void setSearchId(Integer searchId) {
        this.searchId = searchId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchId, platformId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchHasPlatformId entity = (SearchHasPlatformId) o;
        return Objects.equals(this.searchId, entity.searchId) &&
                Objects.equals(this.platformId, entity.platformId);
    }
}