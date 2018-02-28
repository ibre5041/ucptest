package sandbox.rest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import sandbox.pojo.RequestObj;
import sandbox.pojo.ResponseObj;
import sandbox.utils.BookSimpleDao;
import sandbox.ws.UCPHelper;
import sandbox.pojo.Book;
/*
Method 	URL 	Action
	GET 	/api/books 					Retrieve all books
	GET 	/api/books/search/Chateau 	Search for books with ‘Chateau’ in their name
	GET 	/api/books/10 				Retrieve book with id == 10
	POST 	/api/books 					Add a new book
	PUT 	/api/books/10 				Update book with id == 10
	DELETE 	/api/books/10 				Delete book with id == 10
*/

@Path("/books")
public class BookServer {

	BookSimpleDao helper = new BookSimpleDao();

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public List<Book> findAll() {
		return helper.findAll();
	}

	@GET @Path("search/{query}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public List<Book> findByName(@PathParam("query") String query) {
		return helper.findByName(query);
	}

	@GET @Path("{id}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Book findById(@PathParam("id") String id) {
		return helper.findById(Integer.parseInt(id));
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Book create(Book Book) {
		return helper.create(Book);
	}

	@PUT @Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Book update(Book book) {
		return helper.update(book);
	}

	@DELETE @Path("{id}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public void remove(@PathParam("id") int id) {
		helper.remove(id);
	}
}
