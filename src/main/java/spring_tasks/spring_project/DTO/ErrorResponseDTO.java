package spring_tasks.spring_project.DTO;

public class ErrorResponseDTO {

    public String message;
    public int status;

    public ErrorResponseDTO(String message, int status){
        this.message=message;
        this.status=status;
    }
}
