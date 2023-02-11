package eu.opertusmundi.web.controller.api;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.account.ServiceUsageSummaryDto;
import eu.opertusmundi.common.service.ServiceUsageSummaryService;

@RestController("ApiServiceUsageSummaryController")
@RequestMapping(path = "/api/service-usage", produces = MediaType.APPLICATION_JSON_VALUE)
public class ServiceUsageSummaryController
{
    @Autowired
    private ServiceUsageSummaryService serviceUsageSummaryService;
    
    @GetMapping(path = "/summary/{serviceKey}/{year}/{month}")
    public ServiceUsageSummaryDto getSummary(
        @PathVariable(name = "serviceKey") String serviceKeyAsString,
        @PathVariable int year,
        @PathVariable int month)
    {
        final UUID serviceKey = UUID.fromString(serviceKeyAsString); 
        return serviceUsageSummaryService.findOneByServiceKeyAndMonthOfYear(serviceKey, year, month)
            .orElse(null);
    }
}
