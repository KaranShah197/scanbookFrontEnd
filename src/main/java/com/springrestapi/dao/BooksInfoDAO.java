package com.springrestapi.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springrestapi.model.BooksInfo;
import com.springrestapi.repository.BooksRepository;

import java.util.List;

@Service
public class BooksInfoDAO { /*Service class used to process the reqquests */
	
	@Autowired
	BooksRepository booksrepo;
	
	/*To save a books */
	public BooksInfo save(BooksInfo bks) {
		return booksrepo.save(bks);
	}
	
	/* delete a book */
	public void delete(BooksInfo bks) {
		booksrepo.delete(bks);
	}
	
	/*get a book by barcode */
	public BooksInfo findOne(Long barCode) {
		/* @Query("SELECT * FROM booksdetails bd WHERE bd.bar_code = ?") */
		return booksrepo.findOne(barCode);
	}
	
	/*Search all books */
	public List<BooksInfo> findAll(){
		return booksrepo.findAll();
	}	
}
