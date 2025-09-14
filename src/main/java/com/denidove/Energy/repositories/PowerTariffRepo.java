package com.denidove.Energy.repositories;

import com.denidove.Energy.entities.PowerTariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PowerTariffRepo extends JpaRepository<PowerTariff, Long> {

}
