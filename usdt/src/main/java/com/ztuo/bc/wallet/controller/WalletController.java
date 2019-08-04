package com.ztuo.bc.wallet.controller;

import com.ztuo.bc.wallet.config.JsonrpcClient;
import com.ztuo.bc.wallet.entity.Account;
import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.service.AccountService;
import com.ztuo.bc.wallet.service.DepositService;
import com.ztuo.bc.wallet.util.MessageResult;
import com.ztuo.bc.wallet.entity.Account;
import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.service.AccountService;
import com.ztuo.bc.wallet.util.MessageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/rpc")
public class WalletController {
    @Autowired
    private JsonrpcClient rpcClient;
    @Autowired
    private Coin coin;
    private Logger logger = LoggerFactory.getLogger(WalletController.class);
    @Autowired
    private AccountService accountService;

    @GetMapping("address/{account}")
    public MessageResult getNewAddress(@PathVariable String account){
        logger.info("create new address :"+account);
        String address = rpcClient.getNewAddress(account);
        accountService.saveOne(account,address);
        MessageResult result = new MessageResult(0,"success");
        result.setData(address);
        return result;
    }


    @GetMapping("withdraw")
    public MessageResult withdraw(String address, BigDecimal amount,BigDecimal fee){
        logger.info("withdraw:address={},amount={},fee={}",address,amount,fee);
        try {
            String txid = rpcClient.omniSend(coin.getWithdrawAddress(),address,amount);
            MessageResult result = new MessageResult(0,"success");
            result.setData(txid);
            return result;
        }
        catch (Exception e){
            e.printStackTrace();
            return MessageResult.error(500,"error:"+e.getMessage());
        }
    }

    @GetMapping("transfer")
    public MessageResult transfer(String address, BigDecimal amount){
        logger.info("transfer:address={},amount={},fee={}",address,amount);
        try {
            List<Account>  accounts = accountService.findAll();
            BigDecimal transferedAmt = BigDecimal.ZERO;
            for(Account account:accounts){
                if(account.getAddress().equalsIgnoreCase(address))continue;
                BigDecimal btcFee = rpcClient.getAddressBalance(account.getAddress());
                if(btcFee.compareTo(coin.getDefaultMinerFee()) < 0){
                    logger.info("地址{}矿工费不足，最小为{},当前为{}",account.getAddress(),coin.getDefaultMinerFee(),btcFee);
                    continue;
                }
                BigDecimal availAmt = rpcClient.omniGetBalance(account.getAddress());

                if(availAmt.compareTo(amount.subtract(transferedAmt)) > 0){
                    availAmt = amount.subtract(transferedAmt);
                }
                if(availAmt.compareTo(BigDecimal.ZERO)<=0){
                    continue;
                }
                //String txid = rpcClient.omniSend(account.getAddress(),address,amount.toPlainString());
                String txid = rpcClient.omniSend(account.getAddress(),address,availAmt);
                if(txid != null) {
                    System.out.println("fromAddress"+account.getAddress()+",txid:"+txid);
                    transferedAmt = transferedAmt.add(availAmt);
                }
                if(transferedAmt.compareTo(amount) >= 0){
                    break;
                }
            }
            MessageResult result = new MessageResult(0,"success");
            result.setData(transferedAmt);
            return result;
        }
        catch (Exception e){
            e.printStackTrace();
            return MessageResult.error(500,"error:"+e.getMessage());
        }
    }

    @GetMapping("transfer-from-address")
    public MessageResult transferFromAddress(String fromAddress,String address, BigDecimal amount,BigDecimal fee){
        logger.info("transfer:fromeAddress={},address={},amount={},fee={}",fromAddress,address,amount,fee);
        try {
            BigDecimal transferedAmt = BigDecimal.ZERO;
            if(fromAddress.equalsIgnoreCase(address)) return MessageResult.error(500,"转入转出地址不能一样");
            BigDecimal availAmt = rpcClient.omniGetBalance(fromAddress);
            if(availAmt.compareTo(amount) > 0){
                availAmt = amount;
            }
            String txid = rpcClient.omniSend(fromAddress,address,amount);
            if(txid != null) {
                System.out.println("fromAddress"+fromAddress+",txid:"+txid);
                transferedAmt = transferedAmt.add(availAmt);
            }
            MessageResult result = new MessageResult(0,"success");
            result.setData(transferedAmt);
            return result;
        }
        catch (Exception e){
            e.printStackTrace();
            return MessageResult.error(500,"error:"+e.getMessage());
        }
    }

    @GetMapping("balance")
    public MessageResult balance(){
        BigDecimal amount = BigDecimal.ZERO;
        try {
            List<Account> accounts = accountService.findAll();
            for(int i =1;i<accounts.size();i++){
                amount = amount.add(rpcClient.omniGetBalance(accounts.get(i).getAddress()));
            }
            MessageResult result = new MessageResult(0,"success");
            result.setData(amount);
            return result;
        }
        catch (Exception e){
            e.printStackTrace();
            return MessageResult.error(500,"error:"+e.getMessage());
        }
    }


    @GetMapping("balance/{address}")
    public MessageResult balance(@PathVariable String address){
        try {
            BigDecimal balance = rpcClient.omniGetBalance(address);
            MessageResult result = new MessageResult(0,"success");
            result.setData(balance);
            return result;
        }
        catch (Exception e){
            e.printStackTrace();
            return MessageResult.error(500,"error:"+e.getMessage());
        }
    }

}
