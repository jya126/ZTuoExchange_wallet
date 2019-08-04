package com.ztuo.bc.wallet.config;


import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.entity.WatcherSetting;
import com.ztuo.bc.wallet.entity.Coin;
import com.ztuo.bc.wallet.entity.WatcherSetting;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置币种参数
 */
@Configuration
@ConditionalOnProperty(name = "coin.name")
public class CoinConfig {

    @Bean
    @ConfigurationProperties(prefix = "coin")
    public Coin getCoin(){
        Coin coin = new Coin();
        return coin;
    }

    @Bean
    @ConfigurationProperties(prefix = "watcher")
    public WatcherSetting getWatcherSetting(){
        WatcherSetting setting = new WatcherSetting();
        return setting;
    }

}
