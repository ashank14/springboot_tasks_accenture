package spring_tasks.spring_project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring_tasks.spring_project.models.Book;

public interface BookRepository extends JpaRepository<Book,Integer> {

}
