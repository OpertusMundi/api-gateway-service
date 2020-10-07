package eu.opertusmundi.web.jackson.joda.money;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class MoneyJson {

    @Getter
    @Setter
    private String currency;

    @Getter
    @Setter
    private BigDecimal amount;

}
