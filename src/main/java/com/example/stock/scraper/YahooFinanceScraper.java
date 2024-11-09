package com.example.stock.scraper;

import com.example.stock.model.Company;
import com.example.stock.model.Dividend;
import com.example.stock.model.ScrapedResult;
import com.example.stock.model.constants.Month;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class YahooFinanceScraper implements Scraper {

    private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";
    private static final long START_TIME = 86400;   //60 * 60 * 24 (하루를 초로 표현)

    @Override
    public ScrapedResult scrap(Company company) {
        ScrapedResult scrapedResult = new ScrapedResult();
        scrapedResult.setCompany(company);

        try {
            long now = System.currentTimeMillis() / 1000;
            String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);

            log.info("Scraping data for company: {} from URL: {}", company.getTicker(), url);

            // 타임아웃 10초 설정
            Connection connection = Jsoup.connect(url).timeout(10_000);
            Document document = connection.get();

            Elements parsingDivs = document.getElementsByAttributeValue("data-test", "historical-prices");
            if (parsingDivs.isEmpty()) {
                log.error("No historical prices found for company: {}", company.getTicker());
                throw new RuntimeException("리스트에 값이 없습니다.");
            }
            Element tableElement = parsingDivs.get(0);

            Element tBody;
            if (tableElement.children().size() >= 2) {
                log.error("Insufficient table data for company: {}", company.getTicker());
                tBody = tableElement.children().get(1);
            } else {
                throw new RuntimeException("테이블의 값이 부족합니다.");
            }

            List<Dividend> dividends = new ArrayList<>();
            for (Element e : tBody.children()) {
                String text = e.text();
                if (!text.endsWith("Dividend")) {
                    continue;
                }

                String[] splits = text.split(" ");
                int month = Month.strToNumber(splits[0]);
                if (month < 0) {
                    log.error("Unexpected month value: {} for company: {}", splits[0], company.getTicker());
                    throw new RuntimeException("예상치 못한 월 값 ->" + splits[0]);
                }

                int day = Integer.parseInt(splits[1].replace(",", ""));
                int year = Integer.parseInt(splits[2]);
                String dividend = splits[3];

                dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividend));
            }

            log.info("Successfully scraped {} dividends for company: {}", dividends.size(), company.getTicker());
            scrapedResult.setDividends(dividends);

        } catch (IOException e) {
            log.error("IOException occurred while scraping data for company: {} - {}", company.getTicker(), e.getMessage());
            // IOException 발생 시 예외 처리
            throw new RuntimeException("배당금 데이터를 가져오는 중 오류 발생: " + e.getMessage());
        }

        return scrapedResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker) {
        String url = String.format(SUMMARY_URL, ticker, ticker);

        try {
            // 타임아웃 10초 설정
            Document document = Jsoup.connect(url).timeout(10_000).get();
            Element titleElement = document.getElementsByTag("h1").get(0);
            String title = titleElement.text().split("\\(")[0].trim();

            return Company.builder()
                    .ticker(ticker)
                    .name(title)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Error while scraping company data for ticker : " + ticker, e);
        }
    }
}