/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.api.jsonrpc.controller;

import io.nuls.api.config.Config;
import io.nuls.api.config.Context;
import io.nuls.api.resources.manager.BeanCopierManager;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.block.BlockService;
import io.nuls.base.api.provider.block.facade.BlockHeaderData;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByHashReq;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByHeightReq;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByLastHeightReq;
import io.nuls.base.data.Block;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.Parameters;
import io.nuls.core.rpc.model.ResponseData;
import io.nuls.core.rpc.model.TypeDescriptor;
import io.nuls.model.dto.block.BlockDto;
import io.nuls.model.dto.block.BlockHeaderDto;
import io.nuls.model.jsonrpc.RpcResult;
import io.nuls.rpctools.BlockTools;
import io.nuls.utils.Log;
import io.nuls.utils.ResultUtil;
import io.nuls.utils.VerifyUtils;
import io.nuls.v2.model.annotation.Api;
import io.nuls.v2.model.annotation.ApiOperation;
import io.nuls.v2.model.annotation.ApiType;

import java.util.List;

/**
 * @author Niels
 */
@Controller
@Api(type = ApiType.JSONRPC)
public class BlockController {

    BlockService blockService = ServiceManager.get(BlockService.class);
    @Autowired
    private Config config;
    @Autowired
    BlockTools blockTools;

    @RpcMethod("getHeaderByHeight")
    @ApiOperation(description = "根据区块高度查询区块头", order = 201)
    @Parameters({
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
        @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = long.class), parameterDes = "区块高度")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = BlockHeaderDto.class))
    public RpcResult getHeaderByHeight(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        long height;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        try {
            height = Long.parseLong("" + params.get(1));
        } catch (Exception e) {
            return RpcResult.paramError("[height] is invalid");
        }

        if (height < 0) {
            return RpcResult.paramError("[height] is invalid");
        }
        GetBlockHeaderByHeightReq req = new GetBlockHeaderByHeightReq(height);
        req.setChainId(config.getChainId());
        Result<BlockHeaderData> result = blockService.getBlockHeaderByHeight(req);
        if(result.isSuccess()) {
            BlockHeaderData data = result.getData();
            BlockHeaderDto dto = new BlockHeaderDto();
            BeanCopierManager.beanCopier(data, dto);
            return RpcResult.success(dto);
        }
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("getHeaderByHash")
    @ApiOperation(description = "根据区块hash查询区块头", order = 202)
    @Parameters({
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
        @Parameter(parameterName = "hash", parameterDes = "区块hash")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = BlockHeaderDto.class))
    public RpcResult getHeaderByHash(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String hash;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        try {
            hash = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[hash] is invalid");
        }
        if (StringUtils.isBlank(hash)) {
            return RpcResult.paramError("[hash] is required");
        }

        GetBlockHeaderByHashReq req = new GetBlockHeaderByHashReq(hash);
        req.setChainId(config.getChainId());
        Result<BlockHeaderData> result = blockService.getBlockHeaderByHash(req);
        if(result.isSuccess()) {
            BlockHeaderData data = result.getData();
            BlockHeaderDto dto = new BlockHeaderDto();
            BeanCopierManager.beanCopier(data, dto);
            return RpcResult.success(dto);
        }
        return ResultUtil.getJsonRpcResult(result);
    }


    @RpcMethod("getBestBlockHeader")
    @ApiOperation(description = "查询最新区块头信息", order = 203)
    @Parameters({
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = BlockHeaderDto.class))
    public RpcResult getBestBlockHeader(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        GetBlockHeaderByLastHeightReq req = new GetBlockHeaderByLastHeightReq();
        req.setChainId(config.getChainId());
        Result<BlockHeaderData> result = blockService.getBlockHeaderByLastHeight(req);
        if(result.isSuccess()) {
            BlockHeaderData data = result.getData();
            BlockHeaderDto dto = new BlockHeaderDto();
            BeanCopierManager.beanCopier(data, dto);
            return RpcResult.success(dto);
        }
        return ResultUtil.getJsonRpcResult(result);
    }


    @RpcMethod("getBestBlock")
    @ApiOperation(description = "查询最新区块，包含区块打包的所有交易信息，此接口返回数据量较多，谨慎调用", order = 204)
    @Parameters({
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = BlockDto.class))
    public RpcResult getBestBlock(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        Result<Block> result = blockTools.getBestBlock(Context.getChainId());
        if(result.isSuccess()) {
            Block data = result.getData();
            try {
                BlockDto dto = new BlockDto(data);
                BeanCopierManager.beanCopier(data, dto);
                return RpcResult.success(dto);
            } catch (NulsException e) {
                Log.error(e);
                return ResultUtil.getNulsExceptionJsonRpcResult(e);
            }
        }
        return ResultUtil.getJsonRpcResult(result);
    }


    @RpcMethod("getBlockByHeight")
    @ApiOperation(description = "根据区块高度查询区块，包含区块打包的所有交易信息，此接口返回数据量较多，谨慎调用", order = 205)
    @Parameters({
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
        @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = long.class), parameterDes = "区块高度")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = BlockDto.class))
    public RpcResult getBlockByHeight(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        long height;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            height = Long.parseLong("" + params.get(1));
        } catch (Exception e) {
            return RpcResult.paramError("[height] is invalid");
        }
        if (height < 0) {
            return RpcResult.paramError("[height] is invalid");
        }

        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        Result<Block> result = blockTools.getBlockByHeight(config.getChainId(), height);
        if(result.isSuccess()) {
            Block data = result.getData();
            try {
                BlockDto dto = new BlockDto(data);
                BeanCopierManager.beanCopier(data, dto);
                return RpcResult.success(dto);
            } catch (NulsException e) {
                Log.error(e);
                return ResultUtil.getNulsExceptionJsonRpcResult(e);
            }
        }
        return ResultUtil.getJsonRpcResult(result);
    }


    @RpcMethod("getBlockByHash")
    @ApiOperation(description = "根据区块hash查询区块，包含区块打包的所有交易信息，此接口返回数据量较多，谨慎调用", order = 206)
    @Parameters({
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
        @Parameter(parameterName = "hash", parameterDes = "区块hash")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = BlockDto.class))
    public RpcResult getBlockByHash(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String hash;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            hash = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[hash] is invalid");
        }
        if (StringUtils.isBlank(hash)) {
            return RpcResult.paramError("[hash] is required");
        }

        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        Result<Block> result = blockTools.getBlockByHash(config.getChainId(), hash);
        if(result.isSuccess()) {
            Block data = result.getData();
            try {
                BlockDto dto = new BlockDto(data);
                BeanCopierManager.beanCopier(data, dto);
                return RpcResult.success(dto);
            } catch (NulsException e) {
                Log.error(e);
                return ResultUtil.getNulsExceptionJsonRpcResult(e);
            }
        }
        return ResultUtil.getJsonRpcResult(result);
    }

}
