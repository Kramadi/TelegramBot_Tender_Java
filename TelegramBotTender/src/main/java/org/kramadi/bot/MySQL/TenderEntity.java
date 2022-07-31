package org.kramadi.bot.MySQL;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "tender", schema = "bot", catalog = "")
public class TenderEntity extends DataEntity {
    private int  id;
    private Date foundDate;
    private Date updateDate;
    private String subject;
    private String organization;
    private String price;
    private String status;
    private String startDate;
    private String endDate;
    private String url;
    private String tenderId;
    private SearchEntity searchBySearchId;

    @Id
    @Column(name = "id", nullable = false) public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "found_date", nullable = false) public Date getFoundDate() {
        return foundDate;
    }

    public void setFoundDate(Date foundDate) {
        this.foundDate = foundDate;
    }

    @Basic
    @Column(name = "update_date", nullable = true)
    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @Basic
    @Column(name = "subject", nullable = false, length = 500)
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Basic
    @Column(name = "organization", nullable = false, length = 150)
    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    @Basic
    @Column(name = "price", nullable = true, length = 50)
    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Basic
    @Column(name = "status", nullable = true, length = 20)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Basic
    @Column(name = "start_date", nullable = true)
    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    @Basic
    @Column(name = "end_date", nullable = true)
    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @Basic
    @Column(name = "url", nullable = false, length = 100)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public void setTenderId(String tenderId) {
        this.tenderId = tenderId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TenderEntity that = (TenderEntity) o;

        if (id != that.id) return false;
        if (foundDate != null ? !foundDate.equals(that.foundDate) : that.foundDate != null) return false;
        if (updateDate != null ? !updateDate.equals(that.updateDate) : that.updateDate != null) return false;
        if (subject != null ? !subject.equals(that.subject) : that.subject != null) return false;
        if (organization != null ? !organization.equals(that.organization) : that.organization != null) return false;
        if (price != null ? !price.equals(that.price) : that.price != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (startDate != null ? !startDate.equals(that.startDate) : that.startDate != null) return false;
        if (endDate != null ? !endDate.equals(that.endDate) : that.endDate != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (tenderId != null ? !tenderId.equals(that.tenderId) : that.tenderId != null) return false;


        return true;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (foundDate != null ? foundDate.hashCode() :   0);
        result = 31 * result + (updateDate != null ? updateDate.hashCode() : 0);
        result = 31 * result + (subject != null ? subject.hashCode() : 0);
        result = 31 * result + (organization != null ? organization.hashCode() : 0);
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (tenderId != null ? tenderId.hashCode() : 0);
        return result;
    }


    @ManyToOne
    @JoinColumn(name = "search_id", referencedColumnName = "id", nullable = false)
    public SearchEntity getSearchBySearchId() {
        return searchBySearchId;
    }

    public void setSearchBySearchId(SearchEntity searchBySearchId) {
        this.searchBySearchId = searchBySearchId;

    }
}
