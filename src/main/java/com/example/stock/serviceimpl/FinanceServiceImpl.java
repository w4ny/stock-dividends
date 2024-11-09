package com.example.stock.serviceimpl;

import com.example.stock.exception.impl.NoCompanyException;
import com.example.stock.model.Company;
import com.example.stock.model.Dividend;
import com.example.stock.model.ScrapedResult;
import com.example.stock.model.constants.CacheKey;
import com.example.stock.persist.CompanyRepository;
import com.example.stock.persist.DividendRepository;
import com.example.stock.persist.entity.CompanyEntity;
import com.example.stock.persist.entity.DividendEntity;
import com.example.stock.service.FinanceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceServiceImpl implements FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    @Override
    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName) {
        log.info("search company -> " + companyName);

        //1. 회사명을 기준으로 회사 정보를 조회
        CompanyEntity companyEntity = this.companyRepository.findByName(companyName)
                .orElseThrow(NoCompanyException::new);

        //2. 조회된 회사 ID로 배당금 정보 조회
        List<DividendEntity> dividendEntities
                = this.dividendRepository.findAllByCompanyId(companyEntity.getId());

        //3. 결과 조합 후 반환
        List<Dividend> dividends = dividendEntities.stream()
                .map(e -> new Dividend(e.getDate(), e.getDividend()))
                .collect(Collectors.toList());

        return new ScrapedResult(Company.builder()
                .name(companyEntity.getName())
                .ticker(companyEntity.getTicker())
                .build(), dividends);
    }
}