package sandbox.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import sandbox.pojo.Book;
import sandbox.utils.ConnectionHelper;

public class BookSimpleDao {

	public List<Book> findAll() {
		String sql = "SELECT * FROM book";

		List<Book> list = new ArrayList<Book>();

		try (Connection conn = ConnectionHelper.getInstance().getConnection()) {
			Statement stat = conn.createStatement();
			try (ResultSet rs = stat.executeQuery(sql)) {
				while (rs.next()) {
					list.add(processRow(rs));
				}
			}
			conn.commit();

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return list;
	}

	public List<Book> findByName(String name) {
		String sql = "SELECT * FROM book as b WHERE UPPER(name) LIKE ? ORDER BY name";

		List<Book> list = new ArrayList<Book>();

		try (Connection conn = ConnectionHelper.getInstance().getConnection()) {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, "%" + name.toUpperCase() + "%");
			try (ResultSet rs = ps.executeQuery(sql)) {
				while (rs.next()) {
					list.add(processRow(rs));
				}
			}
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		return list;
	}

	public Book findById(int id) {
		String sql = "SELECT * FROM book WHERE id = ?";

		Book book = null;

		try (Connection conn = ConnectionHelper.getInstance().getConnection()) {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					book = processRow(rs);
				}
			}
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		return book;
	}

	public Book save(Book book)
	{
		return book.getId().compareTo(BigDecimal.ZERO) > 0 ? update(book) : create(book);
	} 

	public sandbox.pojo.Book create(Book book) {
		//String sql = "INSERT INTO book (id, name, author) VALUES (book_id_seq.netxval, ?, ?)";
		String sql = "INSERT INTO book (name, author) VALUES (?, ?)";

		try (Connection conn = ConnectionHelper.getInstance().getConnection()) {
			PreparedStatement ps = conn.prepareStatement(sql, new String[] { "ID" });
			ps.setString(1, book.getName());
			ps.setString(2, book.getAuthor());
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				rs.next();
				BigDecimal id = rs.getBigDecimal(1);
				book.setId(id);
			}
			conn.commit();

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		return book;
	}

	public Book update(Book book) {
		String sql = "UPDATE book SET name=?, author=? WHERE id=?";

		try (Connection conn = ConnectionHelper.getInstance().getConnection()) {
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, book.getName());
				ps.setString(2, book.getAuthor());
				ps.setBigDecimal(3, book.getId());
				ps.executeUpdate();
			}
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	return book;
}

	public boolean remove(int id) {
		String sql = "DELETE FROM book WHERE id=?";

		try (Connection conn = ConnectionHelper.getInstance().getConnection()) {
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, id);
			int count = ps.executeUpdate();
			conn.commit();
			return count == 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}			
	}

	protected Book processRow(ResultSet rs) throws SQLException {
		Book book = new Book();
		book.setId(rs.getBigDecimal("id"));
		book.setName(rs.getString("name"));
		book.setAuthor(rs.getString("author"));
		return book;
	}
}
