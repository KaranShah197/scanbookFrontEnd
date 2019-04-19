package com.springrestapi.repository;

import com.springrestapi.model.BooksInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BooksRepository extends JpaRepository<BooksInfo, Long>{

}
