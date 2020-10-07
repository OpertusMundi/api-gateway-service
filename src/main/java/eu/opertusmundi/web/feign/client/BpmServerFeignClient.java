package eu.opertusmundi.web.feign.client;

import java.util.List;

import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto;
import org.camunda.bpm.engine.rest.dto.message.MessageCorrelationResultWithVariableDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceWithVariablesDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.web.feign.client.config.BpmServerFeignClientConfiguration;

/**
 * Feign client for Camunda BPM server.
 *
 * @see: https://github.com/camunda/camunda-rest-client-spring-boot
 *
 */
@FeignClient(
    name = "${opertusmundi.feign.bpm-server.name}",
    url = "${opertusmundi.feign.bpm-server.url}",
    configuration = BpmServerFeignClientConfiguration.class
)
public interface BpmServerFeignClient {

    /**
     * Starts process instance by key
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/process-definition/post-start-process-instance/
     */
    @PostMapping(value = "/process-definition/key/{key}/start", consumes = "application/json")
    ProcessInstanceWithVariablesDto startProcessByKey(
        @PathVariable("key") String processDefinitionKey,
        StartProcessInstanceDto startProcessInstance
    );

    /**
     * Correlates message
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/message/
     */
    @PostMapping(value = "/message", consumes = "application/json")
    List<MessageCorrelationResultWithVariableDto> correlateMessage(CorrelationMessageDto correlationMessage);


    /**
     * Retrieves the list of process definitions
     *
     * @see https://docs.camunda.org/manual/latest/reference/rest/process-definition/get-query/
     */
    @GetMapping(value = "/process-definition", consumes = "application/json")
    List<ProcessDefinitionDto> getProcessDefinitions(
        @SpringQueryMap ProcessDefinitionQueryDto query,
        @RequestParam("firstResult") int firstResult,
        @RequestParam("maxResults") int maxResults
    );

}
