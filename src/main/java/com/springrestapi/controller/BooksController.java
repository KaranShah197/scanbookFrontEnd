package com.springrestapi.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.BooksRequestInitializer;
import com.google.api.services.books.Books.Volumes.List;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;

import com.springrestapi.dao.BooksInfoDAO;
import com.springrestapi.model.BooksInfo;

@Controller
@RestController
@RequestMapping("/books")
public class BooksController { /*Controller class here we define all the necessary routing for API and URL mappings */
	
	@Autowired
	BooksInfoDAO booksDao;	 
	
	private static final String APPLICATION_NAME = "";
	
	/*1) To Save an Book*/
	@PostMapping("/add")
	public BooksInfo createBook(@Valid @RequestBody BooksInfo bks) {
		return booksDao.save(bks);
	}
	
	/*2) delete a book by ISBN*/
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<BooksInfo> deleteBook(@PathVariable(value = "id") Long barCode) {
		
		BooksInfo bks = booksDao.findOne(barCode);
		if(bks == null) {
			return ResponseEntity.notFound().build();
		}
		booksDao.delete(bks);
		
		return ResponseEntity.ok().build();		
	}
	
	/*3) Get Book By ISBN */
	@GetMapping("/search/{id}")
	public ResponseEntity<BooksInfo> getBookByBarCode(@PathVariable(value="id") Long barCode) throws Exception{
		BooksInfo bks = booksDao.findOne(barCode);
		if(bks == null) {
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
			BooksController bc = new BooksController();
			String bkArr[] = bc.queryGoogleBooks(jsonFactory, barCode);
			if(bkArr[1].isEmpty()){
				return ResponseEntity.notFound().build();
			}else {
				/*BooksInfo newBook = new BooksInfo(barCode, bkArr[0], bkArr[1], bkArr[2], 0 );*/
				BooksInfo newBook = new BooksInfo();
				newBook.barCode = barCode;
				newBook.title = bkArr[0];
				newBook.author = bkArr[1];
				newBook.numberOfPages = Long.parseLong(bkArr[2]);
				newBook.readFlag = 0;
				
				booksDao.save(newBook);
				
				return ResponseEntity.ok().body(newBook);
			}
		}
		return ResponseEntity.ok().body(bks);
	}
	
	/*4) update notes and read status */
	@PostMapping("/update/{id}")
	public ResponseEntity<BooksInfo> updateBook(@PathVariable(value = "id") Long barCode, @Valid @RequestBody String notes) {
		
		BooksInfo bks = booksDao.findOne(barCode);
		if(bks == null) {
			return ResponseEntity.notFound().build();
		}
		bks.setNotes(notes.split("&")[0].split("=")[1]);
		bks.setReadFlag(Integer.parseInt(notes.split("&")[1].split("=")[1]));
		
		BooksInfo updateBook = booksDao.save(bks);
		
		return ResponseEntity.ok().body(updateBook);
	}
		
	/*get all books*/
	@GetMapping("/allbooks")
	public List getAllBooks(){
		return (List) booksDao.findAll();
	}
	
	
	private String[] queryGoogleBooks(JsonFactory jsonFactory, Long barCode) throws Exception {
	    ClientCredentials.errorIfNotSpecified();
	    
	    String booksArr[] = new String[5];
	    // Set up Books client.
	    final Books books = new Books.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, null)
	        .setApplicationName(APPLICATION_NAME)
	        .setGoogleClientRequestInitializer(new BooksRequestInitializer(ClientCredentials.API_KEY))
	        .build();
	    
	    // Set query string and filter only Google eBooks.
	    System.out.println("Query: [" + barCode + "]");
	    List volumesList = books.volumes().list(barCode.toString());
	    volumesList.setFilter("ebooks");

	    // Execute the query.
	    Volumes volumes = volumesList.execute();
	    if (volumes.getTotalItems() == 0 || volumes.getItems() == null) {
	      System.out.println("No matches found.");
	      return booksArr;
	    }

	    // Output results.
	    for (Volume volume : volumes.getItems()) {
		      Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();
		      System.out.println("Start==========");
		      
		      // Title.
		      if(!volumeInfo.getTitle().isEmpty()) {
		    	  System.out.println("Title: " + volumeInfo.getTitle());
		    	  booksArr[0] = volumeInfo.getTitle();
		      }else {
		    	  booksArr[1] = "No Title";
		      }
		      
		      // Author(s)
		      java.util.List<String> authors = volumeInfo.getAuthors();
		      if (authors != null && !authors.isEmpty()) {
		    	  System.out.println("Author(s): " + volumeInfo.getAuthors().toString());
		    	  booksArr[1] = volumeInfo.getAuthors().toString();
		      }else {
		    	  booksArr[1] = "No Author Fouund";
		      }
		      System.out.println("==========End");
		      
		      //Pages
		      if(volumeInfo.getPageCount() != null) {
		    	  System.out.println("Author(s): " + volumeInfo.getPageCount());
		    	  booksArr[2] = volumeInfo.getPageCount().toString();
		      }else {
		    	  System.out.println("tester");
		    	  booksArr[2] = "0";
		      }
	    }
	    System.out.println("==========End");		
		return booksArr;
		
	  }

}
