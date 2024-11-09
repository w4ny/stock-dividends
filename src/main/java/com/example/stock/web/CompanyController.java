package com.example.stock.web;

import com.example.stock.model.Company;
import com.example.stock.model.constants.CacheKey;
import com.example.stock.persist.entity.CompanyEntity;
import com.example.stock.service.CompanyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/company")
@AllArgsConstructor
@Slf4j
public class CompanyController {

    private final CompanyService companyService;
    private final CacheManager redisCacheManager;

    @GetMapping("/autocomplete")
    public ResponseEntity<?> autoComplete(@RequestParam String keyword) {
        List<String> keywordList = this.companyService.getCompanyNamesByKeyword(keyword);
        return ResponseEntity.ok(keywordList);
    }

    @GetMapping
    @PreAuthorize("hasRole('READ')")
    public ResponseEntity<?> searchCompany(@PageableDefault Pageable pageable) {
        Page<CompanyEntity> companyEntities = this.companyService.getAllCompany(pageable);
        return ResponseEntity.ok(companyEntities);
    }

    /**
     * 회사 및 배당금 정보 추가
     *
     * @param request
     * @return
     */
    @PostMapping
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<?> addCompany(@RequestBody Company request) {
        // 요청이 들어온 순간을 기록
        log.info("CompanyController: Add Company started with -> {}", request.getTicker().trim());

        String ticker = request.getTicker().trim();
        if (ObjectUtils.isEmpty(ticker)) {
            log.error("CompanyController: Add Company failed. -> Ticker is empty");
            return ResponseEntity.badRequest().body("Ticker cannot be empty");
        }

        Company savedCompany = this.companyService.save(ticker);
        this.companyService.addAutocompleteKeyword(savedCompany.getName());

        log.info("CompanyController: Add Company completed with ticker -> {}", ticker);

        return ResponseEntity.ok(savedCompany);
    }

    @DeleteMapping("/{ticker}")
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<?> deleteCompany(@PathVariable String ticker) {
        String companyName = this.companyService.deleteCompany(ticker);
        this.clearFinanceCache(companyName);
        return ResponseEntity.ok(companyName);
    }

    private void clearFinanceCache(String companyName) {
        this.redisCacheManager.getCache(CacheKey.KEY_FINANCE).evict(companyName);
    }
}