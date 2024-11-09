package com.example.stock.service;

import com.example.stock.model.ScrapedResult;

public interface FinanceService {
    ScrapedResult getDividendByCompanyName(String companyName);
}
