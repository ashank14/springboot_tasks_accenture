package spring_tasks.spring_project.dto;


import java.time.LocalDate;
import java.util.List;


public record GoogleApiResponseDTO(String id, String title, List<String> author, LocalDate publishedDate){}
