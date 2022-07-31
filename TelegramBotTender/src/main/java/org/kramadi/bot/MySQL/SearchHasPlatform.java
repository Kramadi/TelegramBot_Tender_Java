package org.kramadi.bot.MySQL;

import javax.persistence.*;


@Table(name = "search_has_platform")
@Entity
public class SearchHasPlatform {
    @EmbeddedId
    private SearchHasPlatformId id;

    public SearchHasPlatformId getId() {
        return id;
    }

    public void setId(SearchHasPlatformId id) {
        this.id = id;
    }
}
