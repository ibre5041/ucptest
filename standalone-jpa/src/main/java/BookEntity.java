import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@org.hibernate.annotations.NamedQuery(
		name = "Last Book"
		, query = "from BookEntity where id = (select max(id) from BookEntity)"
		)
@org.hibernate.annotations.NamedQuery(
		name = "Last Book slow"
		, query = "from BookEntity where id = (select max(id) from BookEntity) and SLOW_NUMBER(10) = 10"
		, timeout = 5
		)
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
    
    @Override
	public String toString() {
		return "Book [id=" + this.id + ", name=" + this.name + ", author=" + this.author + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BookEntity other = (BookEntity) obj;
		if (author == null) {
			if (other.author != null)
				return false;
		} else if (!author.equals(other.author))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}    
}
