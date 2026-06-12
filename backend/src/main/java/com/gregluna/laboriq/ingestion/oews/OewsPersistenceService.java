package com.gregluna.laboriq.ingestion.oews;

import com.gregluna.laboriq.occupation.Occupation;
import com.gregluna.laboriq.occupation.OccupationEmployment;
import com.gregluna.laboriq.occupation.OccupationEmploymentRepository;
import com.gregluna.laboriq.occupation.OccupationEntityFactory;
import com.gregluna.laboriq.occupation.OccupationRepository;
import com.gregluna.laboriq.occupation.OccupationWage;
import com.gregluna.laboriq.occupation.OccupationWageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OewsPersistenceService {

    private static final String SOURCE = "BLS_OEWS";

    private final OccupationRepository occupationRepository;
    private final OccupationWageRepository wageRepository;
    private final OccupationEmploymentRepository employmentRepository;

    @Transactional
    public int upsertAll(List<OewsRow> rows, int year) {
        short yr = (short) year;
        int count = 0;
        for (OewsRow row : rows) {
            upsertRow(row, yr);
            count++;
        }
        log.info("Upserted {} occupation records for year {}", count, year);
        return count;
    }

    private void upsertRow(OewsRow row, short year) {
        Occupation occ = upsertOccupation(row);
        upsertWage(occ, row, year);
        upsertEmployment(occ, row, year);
    }

    private Occupation upsertOccupation(OewsRow row) {
        Occupation occ = occupationRepository.findBySocCode(row.occCode())
                .orElseGet(() -> new Occupation(row.occCode(), row.occTitle()));
        occ.setTitle(row.occTitle());
        occ.getSourceMetadata().put("source", SOURCE);
        occ.getSourceMetadata().put("occGroup", row.occGroup());
        return occupationRepository.save(occ);
    }

    private void upsertWage(Occupation occ, OewsRow row, short year) {
        OccupationWage wage = wageRepository
                .findByOccupationSocCodeAndYear(occ.getSocCode(), year)
                .orElseGet(OccupationEntityFactory::blankWage);
        wage.setOccupation(occ);
        wage.setYear(year);
        wage.setMeanWage(row.aMean());
        wage.setMedianWage(row.aMedian());
        wage.setP10Wage(row.aPct10());
        wage.setP25Wage(row.aPct25());
        wage.setP75Wage(row.aPct75());
        wage.setP90Wage(row.aPct90());
        wageRepository.save(wage);
    }

    private void upsertEmployment(Occupation occ, OewsRow row, short year) {
        if (row.totEmp() == null) return;
        OccupationEmployment emp = employmentRepository
                .findByOccupationSocCodeAndYear(occ.getSocCode(), year)
                .orElseGet(OccupationEntityFactory::blankEmployment);
        emp.setOccupation(occ);
        emp.setYear(year);
        emp.setEmploymentCount(row.totEmp());
        employmentRepository.save(emp);
    }
}
