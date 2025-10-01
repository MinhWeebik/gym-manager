package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.Classes;
import com.ringme.cms.model.gym.CoinDiscountTier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoinDiscountTierRepository extends JpaRepository<CoinDiscountTier, Long> {
    @Query(value = "SELECT * FROM coin_discount_tier " +
            "WHERE status != 0 " +
            "ORDER BY min_coins ASC",
            countQuery = "SELECT count(*) FROM coin_discount_tier " +
                    "WHERE status != 0 ", nativeQuery = true)
    Page<CoinDiscountTier> getAll(Pageable pageable);

    @Query(value= "SELECT * FROM coin_discount_tier WHERE status != 0 ORDER BY min_coins DESC", nativeQuery = true)
    List<CoinDiscountTier> getAll();
}
