package sandbox.dbcp.rest;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import sandbox.dao.BookSimpleDao;
import sandbox.pojo.Book;

/*
URL: http://localhost:8080/tomcat-dbcp-rest/books

Method 	URL 	Action
	GET 	/api/books 					Retrieve all books
	GET 	/api/books/search/Chateau 	Search for books with ‘Chateau’ in their name
	GET 	/api/books/10 				Retrieve book with id == 10
	POST 	/api/books 					Add a new book
	PUT 	/api/books/10 				Update book with id == 10
	DELETE 	/api/books/10 				Delete book with id == 10
*/

@ApplicationScoped
@Path("books")
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
