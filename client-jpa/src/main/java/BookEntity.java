import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="BOOK")
public class BookEntity {
    @Id
    @Column(name="id")
    BigDecimal id;

    @Column(name="name")
    String name;

    @Column(name="author")
    String author;
    
    public BookEntity(BigDecimal id, String name, String author) {
        this.id = id;
        this.name = name;
        this.author = author;
    }

    public BookEntity() {
    }

    public BigDecimal getId() {
        return id;
    }

    public void setId(BigDecimal id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
