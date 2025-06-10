package spring_tasks.spring_project.dto;

import java.time.LocalDate;


public record BookResponseDTO( int id, String title, String author, LocalDate publishedDate){}
