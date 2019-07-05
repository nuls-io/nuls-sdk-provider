/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.api.resources;

import io.nuls.api.config.Config;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.ledger.LedgerProvider;
import io.nuls.base.api.provider.ledger.facade.AccountBalanceInfo;
import io.nuls.base.api.provider.ledger.facade.GetBalanceReq;
import io.nuls.base.api.provider.transaction.TransferService;
import io.nuls.base.api.provider.transaction.facade.TransferReq;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.model.*;
import io.nuls.model.ErrorData;
import io.nuls.model.RpcClientResult;
import io.nuls.model.dto.AccountBalanceDto;
import io.nuls.model.dto.TransactionDto;
import io.nuls.model.form.TransferForm;
import io.nuls.rpctools.TransactionTools;
import io.nuls.utils.ResultUtil;
import io.nuls.v2.model.annotation.Api;
import io.nuls.v2.model.annotation.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2019-06-27
 */
@Path("/api/accountledger")
@Component
@Api
public class AccountLedgerResource {

    @Autowired
    Config config;

    TransferService transferService = ServiceManager.get(TransferService.class);
    LedgerProvider ledgerProvider = ServiceManager.get(LedgerProvider.class);
    @Autowired
    TransactionTools transactionTools;

    @POST
    @Path("/transfer")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "单笔转账")
    @Parameters({
            @Parameter(parameterName = "单笔转账", parameterDes = "单笔转账表单", requestType = @TypeDescriptor(value = TransferForm.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "交易hash")
    }))
    public RpcClientResult transfer(TransferForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        TransferReq.TransferReqBuilder builder =
                new TransferReq.TransferReqBuilder(config.getChainId(),config.getAssetsId())
                        .addForm(form.getAddress(), form.getPassword(), form.getAmount())
                        .addTo(form.getToAddress(), form.getAmount());
        Result<String> result = transferService.transfer(builder.build());
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if(clientResult.isSuccess()) {
            return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @GET
    @Path("/balance/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "查询账户余额")
    @Parameters({
            @Parameter(parameterName = "address", requestType = @TypeDescriptor(value = String.class), parameterDes = "账户地址")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = AccountBalanceDto.class))
    public RpcClientResult getBalance(@PathParam("address") String address) {
        if (address == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "address is empty"));
        }
        Integer assetChainId = config.getChainId();
        Integer assetId = config.getAssetsId();
        GetBalanceReq req = new GetBalanceReq(assetId,assetChainId,address);
        req.setChainId(config.getChainId());
        Result<AccountBalanceInfo> result = ledgerProvider.getBalance(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if(clientResult.isSuccess()) {
            clientResult.setData(new AccountBalanceDto((AccountBalanceInfo) clientResult.getData()));
        }
        return clientResult;
    }

    @GET
    @Path("/tx/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "根据hash获取交易，先查未确认，查不到再查已确认")
    @Parameters({
            @Parameter(parameterName = "hash", requestType = @TypeDescriptor(value = String.class), parameterDes = "交易hash")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = TransactionDto.class))
    public RpcClientResult getTx(@PathParam("hash") String hash) {
        if (hash == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "hash is empty"));
        }
        Result<TransactionDto> result = transactionTools.getTx(config.getChainId(), hash);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        return clientResult;
    }

}
