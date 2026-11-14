
package pl.wsb.biblioteka.loan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.wsb.biblioteka.loan.entity.Loan;

import java.util.List;

public interface LoanRepository
        extends JpaRepository<Loan, Long> {

    // LAB 03 - wyszukiwanie wypozyczen po nazwisku
    List<Loan> findByReader_LastName(String lastName);

}
