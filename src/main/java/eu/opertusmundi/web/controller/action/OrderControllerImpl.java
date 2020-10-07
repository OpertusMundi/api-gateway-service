package eu.opertusmundi.web.controller.action;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.web.feign.client.BpmServerFeignClient;
import eu.opertusmundi.web.model.order.OrderDto;

//@RestController
public class OrderControllerImpl implements OrderController {

    private static final String OrderProcessDefinitionKey = "order-process";

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BpmServerFeignClient bpmServerClient;

    @Override
    public RestResponse<?> create(@RequestBody OrderDto order) {
        try {
            final StartProcessInstanceDto options = new StartProcessInstanceDto();

            order.setId(UUID.randomUUID());

            final Map<String, VariableValueDto> variables = new HashMap<String, VariableValueDto>();

            final VariableValueDto idVariable = new VariableValueDto();
            idVariable.setValue(order.getId().toString());
            idVariable.setType("String");
            variables.put("order-id", idVariable);

            final VariableValueDto jsonVariable = new VariableValueDto();
            jsonVariable.setValue(this.objectMapper.writeValueAsString(order));
            jsonVariable.setType("String");
            variables.put("order-payload",jsonVariable);

            final VariableValueDto amountVariable = new VariableValueDto();
            amountVariable.setValue(order.getItems().stream().mapToLong(i -> i.getPrice()).sum());
            amountVariable.setType("Long");
            variables.put("amount", amountVariable);

            options.setBusinessKey(String.format("order-%s", order.getId().toString()));
            options.setVariables(variables);
            options.setWithVariablesInReturn(true);

            this.bpmServerClient.startProcessByKey(OrderProcessDefinitionKey, options);

            return RestResponse.result(order);
        } catch(final JsonProcessingException e) {
            return RestResponse.error(BasicMessageCode.InternalServerError, "Operation has failed");
        }
    }

}
