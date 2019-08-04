package com.ztuo.bc.wallet.util;

import com.ztuo.bc.wallet.entity.Account;

public interface AccountReplayListener {

    void replay(Account account);
}
