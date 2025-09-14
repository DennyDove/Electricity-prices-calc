package com.denidove.Energy.repositories;

import com.denidove.Energy.entities.TransPeaks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransPeaksRepo extends JpaRepository<TransPeaks, Long> {

}
