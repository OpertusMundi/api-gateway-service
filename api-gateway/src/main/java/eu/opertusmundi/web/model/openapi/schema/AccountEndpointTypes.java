package eu.opertusmundi.web.model.openapi.schema;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.AccountClientDto;
import eu.opertusmundi.common.model.account.AccountDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class AccountEndpointTypes {

    public static class AccountResponse extends RestResponse<AccountDto> {

    }


    public static class AccountClientResponse extends RestResponse<AccountClientDto> {

    }

    @Schema(description = "Vendor account collection response")
    public static class AccountCollectionResponse extends RestResponse<PageResultDto<AccountDto>> {

    }

    @Schema(description = "Account client collection response")
    public static class AccountClientCollectionResponse extends RestResponse<PageResultDto<AccountClientDto>> {

    }

}
