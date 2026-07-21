package boets.be.nbts.admin.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table(schema = "admin", name = "api_counter")
@Data
@EqualsAndHashCode
public class ApiCounterEntity {
    @Id
    private Integer id;
    private LocalDate date;
    private Integer counter;
}
