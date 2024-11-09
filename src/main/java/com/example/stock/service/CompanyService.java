package com.example.stock.service;

import com.example.stock.model.Company;
import com.example.stock.persist.entity.CompanyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CompanyService {
    Company save(String ticker);

    Company storeCompanyAndDividend(String ticker);

    Page<CompanyEntity> getAllCompany(Pageable pageable);

    List<String> getCompanyNamesByKeyword(String keyword);

    void addAutocompleteKeyword(String keyword);

    List<String> autocomplete(String keyword);

    String deleteCompany(String ticker);
}